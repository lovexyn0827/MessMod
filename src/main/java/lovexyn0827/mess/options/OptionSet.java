package lovexyn0827.mess.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.context.CommandContext;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.options.OptionManager.CustomOptionValidator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.command.ServerCommandSource;

public final class OptionSet {
	private static final Logger LOGGER = LogManager.getLogger();
	static final OptionSet DEFAULT = new OptionSet();
	static final OptionSet GLOBAL = new OptionSet(
			new File(FabricLoader.getInstance().getGameDir().toString() + "/mcwmem.prop"), DEFAULT, "GLOBAL");
	/**
	 * The parent of this {@code OptionSet}, whose values are used as default values of this {@code OptionSet}.
	 * 
	 * Also, if an option in this {@code Option} has invalid value, its value will be set to the one in its parent set.
	 * 
	 * If the value of the field is {@code null} (for example in the global set), hard-coded default values are used.
	 */
	private final OptionSet parent;
	private final Properties backend = new Properties();
	@Nullable
	private final File optionFile;
	private boolean isActive;
	/**
	 * Whether to send options to clients wholly instead of one by one.
	 */
	private boolean shouldBatchOptions = false;
	
	/**
	 * {@code true} if this {@code OptionSet} is sent from a server and has no underlying local file.
	 */
	private final boolean remote;
	private final String optionSetName;
	
	/**
	 * @param parent If null, the default OptionSet will be used.
	 */
	private OptionSet(File optionFile, @Nullable OptionSet parent, String name) {
		this.optionFile = optionFile;
		this.parent = parent == null ? DEFAULT : parent;
		this.remote = optionFile == null;
		this.optionSetName = name;
		this.reload();
		this.activiate();
	}
	
	private OptionSet(Reader r, String name) {
		this.optionFile = null;
		this.parent = DEFAULT;
		this.remote = true;
		this.optionSetName = name;
		try {
			this.backend.load(r);
			this.activiate();
		} catch (IOException e) {
			LOGGER.error("Failed to load remote option set!");
			e.printStackTrace();
		}
		
		this.replaceInvalidValues();
	}
	
	/**
	 * Construct a new OptionSet using the default value of each option.
	 */
	private OptionSet() {
		this.optionFile = null;
		this.parent = null;
		this.remote = true;
		this.optionSetName = "DEFAULT";
		OptionManager.OPTIONS.forEach((name, opt) -> {
			this.backend.put(name, opt.option.defaultValue());
		});
		// Normally we don't have to check that
		//this.replaceInvalidValues();
	}

	public static OptionSet load(@NotNull File optionFile) {
		return new OptionSet(optionFile, GLOBAL, optionFile.getAbsolutePath().toString());
	}
	
	public static OptionSet fromPacket(PacketByteBuf in) {
		return new OptionSet(new StringReader(in.readString()), "REMOTE");
	}
	
	public void set(String name, String optionStr, @Nullable CommandContext<ServerCommandSource> ct) 
			throws InvalidOptionException {
		if(!OptionManager.isValidOptionName(name)) {
			LOGGER.warn("Ignored modification to non-existing option: {} = {}", name, optionStr);
			return;
		}
		
		try {
			// Validation I
			Object parsed = OptionParser.of(name).tryParse(optionStr);
			// Validation II
			CustomOptionValidator validator = OptionManager.CUSTOM_OPTION_VALIDATORS.get(name);
			if(validator != null) {
				validator.validate(parsed, ct);
			}
		} catch(InvalidOptionException e) {
			if(ct == null) {
				// Since /messcfg itself prints error message, we needn't outputs it if ct != null.
				MessMod.LOGGER.warn(e.getLocalizedMessage());
			}
			
			throw e;
		}
		
		this.backend.put(name, optionStr);
		if(this.isActive) {
			this.apply(name, ct);
		}
		
		if(!this.remote) {
			this.save();
		}
	}
	
	public void set(String name, String optionStr) throws InvalidOptionException {
		this.set(name, optionStr, null);
	}

	public void save() {
		if(this.remote) {
			throw new UnsupportedOperationException("Trying to save remote option set!");
		}
		
		try (FileOutputStream in = new FileOutputStream(this.optionFile)) {
			this.backend.store(in, "MessMod Options");
		} catch (IOException e) {
			LogManager.getLogger().fatal("Failed to write mcwmem.prop!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Refresh the this {@code OptionSet}
	 */
	public void reload() {
		if(this.remote) {
			throw new UnsupportedOperationException("Trying to reload remote option set!");
		}
		
		if(this.optionFile.exists()) {
			try (FileInputStream in = new FileInputStream(this.optionFile)) {
				this.backend.load(in);
				this.replaceInvalidValues();
			} catch (IOException e) {
				MessMod.LOGGER.fatal("Failed to open mcwmem.prop, the Minecraft may crash later.");
				e.printStackTrace();
			}
		} else {
			MessMod.LOGGER.info("Couldn't find mcwmem.prop, creating a new one.");
			if(this.parent != null) {
				this.backend.clear();
				this.backend.putAll(this.parent.backend);
			} else {
				this.writeDefaults();
			}
		}
		
		OptionManager.OPTIONS.forEach((name, opt) -> {
			if(!this.backend.containsKey(name)) {
				this.backend.put(name, opt.getDefaultValue());
			}
		});
		if(this.isActive) {
			this.activiate();
		}
		
		sendOptionsToClientsIfNeeded();
		this.save();
	}
	
	private void replaceInvalidValues() {
		this.backend.entrySet().forEach((e) -> {
			String name = (String) e.getKey();
			String valStr = (String) e.getValue();
			if(!OptionManager.isValidOptionName(name)) {
				LOGGER.warn("Ignored unrecognized option: {}={}", name, valStr);
				return;
			}
			
			OptionParser<?> parser = OptionParser.of(name);
			try {
				parser.validate(valStr);
			} catch (InvalidOptionException e1) {
				String lastValid = this.parent == null ? 
						OptionManager.OPTIONS.get(name).getDefaultValue()
						: this.parent.getSerialized(name);
				e.setValue(lastValid);
			}
		});
	}

	private void sendOptionsToClientsIfNeeded() {
		if(this.isActive && MessMod.isDedicatedEnv() && MessMod.INSTANCE.getServerNetworkHandler() != null) {
			MessMod.INSTANCE.getServerNetworkHandler().sendToEveryone(this.toPacket());
		}
	}
	
	private void sendSingleOptionToClientsIfNeeded(String name, String value) {
		if(this.isActive && !this.shouldBatchOptions 
				&& MessMod.isDedicatedEnv() && MessMod.INSTANCE.getServerNetworkHandler() != null) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeString(name);
			buf.writeString(value);
			MessMod.INSTANCE
					.getServerNetworkHandler()
					.sendToEveryone(new CustomPayloadS2CPacket(Channels.OPTION_SINGLE, buf));
		}
	}

	private void writeDefaults() {
		OptionManager.OPTIONS.values().forEach((o) -> {
			this.backend.put(o.name, o.getDefaultValue());
		});
	}

	@Nullable
	public Object get(String name) {
		if(!OptionManager.isValidOptionName(name)) {
			return null;
		}
		
		try {
			return OptionParser.of(name).tryParse(this.getSerialized(name));
		} catch (InvalidOptionException e) {
			LOGGER.fatal("Unstripped invalid option: {}={}", name, this.getSerialized(name));
			throw new IllegalStateException(e);
		}
	}
	
	public String getSerialized(String name) {
		return this.backend.getProperty(name);
	}
	
	/**
	 * Set the values of underlying fields of all options to their values in this {@code OptionSet}, 
	 * and send this {@code OptionSet} to the clients.
	 */
	public void activiate() {
		this.shouldBatchOptions = true;
		OptionManager.OPTIONS.forEach((n, o) -> {
			Object val;
			try {
				val = o.parser.tryParse(this.getSerialized(n));
			} catch (InvalidOptionException e) {
				LOGGER.fatal("Unstripped invalid option: {}={}", n, this.getSerialized(n));
				throw new IllegalStateException(e);
			}
			
			o.set(val, null);
		});
		this.isActive = true;
		this.sendOptionsToClientsIfNeeded();
		this.shouldBatchOptions = false;
	}
	
	public void inactiviate() {
		this.isActive = false;
	}
	
	private void apply(String name, @Nullable CommandContext<ServerCommandSource> ct) {
		if(!OptionManager.isValidOptionName(name)) {
			throw new IllegalArgumentException("Trying to apply invalid option: " + name);
		}
		
		try {
			OptionManager.OPTIONS.get(name).set(OptionParser.of(name).tryParse(this.getSerialized(name)), ct);
			this.sendSingleOptionToClientsIfNeeded(name, this.getSerialized(name));
		} catch (InvalidOptionException e) {
			LOGGER.fatal("Unstripped invalid option: {}={}", name, this.getSerialized(name));
			throw new IllegalStateException(e);
		}
	}
	
	public CustomPayloadS2CPacket toPacket() {
		try {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			StringWriter sw = new StringWriter();
			this.backend.store(sw, "MessMod Options");
			buf.writeString(sw.toString());
			return new CustomPayloadS2CPacket(Channels.OPTIONS, buf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getReadablePathStr() {
		return this.remote ? "remote server" : this.optionFile.getAbsolutePath();
	}

	@Override
	public String toString() {
		return "OptionSet [" + this.optionSetName + "]";
	}
}
