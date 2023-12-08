package lovexyn0827.mess.util.access;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.util.TranslatableException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class CustomNode extends Node {
	private static final WorldSavePath SAVED_NODES = WorldSavePathMixin.create("saved_accessing_paths.prop");
	private static final TreeMap<String, String> NODES_BY_NAME = new TreeMap<>();
	private static final TreeMap<String, Class<?>> COMPILED_NODES_BY_NAME = new TreeMap<>();
	public static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
	private final String name;
	private final AccessingPath backend;
	
	private CustomNode(String name, AccessingPath backend) {
		this.name = name;
		this.backend = backend;
	}

	@Override
	Object access(Object previous) throws AccessingFailureException {
		return this.backend.access(previous, this.inputType);
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) throws AccessingFailureException, InvalidLiteralException {
		return this.backend.getOutputType();
	}

	public static void define(String name, String path, boolean permanent, MinecraftServer server)
			throws CommandSyntaxException {
		if(NODES_BY_NAME.containsKey(name) || COMPILED_NODES_BY_NAME.containsKey(name)) {
			throw new TranslatableException("cmd.general.dupname");
		} else {
			// Validate the given accessing path. 
			AccessingPathArgumentType.accessingPathArg().parse(new StringReader(path));
			NODES_BY_NAME.put(name, path);
			if(permanent) {
				operateSavedNodes(server, (p) -> p.put(name, path));
			}
		}
	}
	
	public static void defineCompiled(String name, List<Class<?>> nodeTypes)
			throws CommandSyntaxException, CompilationException {
		if(!NODES_BY_NAME.containsKey(name)) {
			throw new TranslatableException("%s was not defined", name);
		} else {
			String pathStr = NODES_BY_NAME.get(name);
			AccessingPath path = AccessingPathArgumentType.accessingPathArg().parse(new StringReader(pathStr));
			COMPILED_NODES_BY_NAME.put(name, path.compile(nodeTypes, name));
		}
	}
	
	public static void reload(MinecraftServer server){
		NODES_BY_NAME.clear();
		COMPILED_NODES_BY_NAME.clear();
		operateSavedNodes(server, (p) -> p.forEach((name, path) -> NODES_BY_NAME.put((String) name, (String) path)));
	}
	
	private static void operateSavedNodes(MinecraftServer server, Consumer<Properties> operation) {
		File file = server.getSavePath(SAVED_NODES).toFile();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				TranslatableException e1 = new TranslatableException("cmd.accessingpath.filefail",
						e.getLocalizedMessage());
				throw e1;
			}
		}
		
		try(Reader reader = new FileReader(file)) {
			Properties prop = new Properties();
			try {
				prop.load(reader);
			} catch (Exception e) {
				MessMod.LOGGER.warn("Couldn't parse saved nodes: {}", e);
				e.printStackTrace();
			}
			operation.accept(prop);
			Writer writer = new FileWriter(file);
			prop.store(writer, "Saved Custom Nodes");
		} catch (Exception e) {
			TranslatableException e1 = new TranslatableException("cmd.accessingpath.filefail",
					e.getLocalizedMessage());
			throw e1;
		}
	}

	public static void undefine(String name, MinecraftServer server) {
		if(NODES_BY_NAME.containsKey(name)) {
			NODES_BY_NAME.remove(name);
			operateSavedNodes(server, (p) -> p.remove(name));
		} else {
			throw new TranslatableException("cmd.accessingpath.undef");
		}
	}
	
	@Nullable
	public static CustomNode byName(String name) throws CommandSyntaxException {
		Class<?> clazz = COMPILED_NODES_BY_NAME.get(name);
		if(clazz != null) {
			try {
				return new CustomNode(name, (CompiledPath) clazz.getConstructor().newInstance());
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
		} else {
			String pathStr = NODES_BY_NAME.get(name);
			AccessingPath path = AccessingPathArgumentType.accessingPathArg()
					.parse(new StringReader(pathStr));
			return new CustomNode(name, path);
		}
	}
	
	public String toString() {
		return this.name;
	}

	public static void listSuggestions(SuggestionsBuilder b) {
		NODES_BY_NAME.keySet().forEach(b::suggest);
	}
	
	public static String listDefinitions() {
		StringBuilder sb = new StringBuilder();
		NODES_BY_NAME.forEach((name, path) -> {
			sb.append(name);
			sb.append(": ");
			sb.append(path);
			sb.append('\n');
		});
		return sb.toString();
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			int cid = ctx.allocateSubPath(this.backend);
			InsnList insns = new InsnList();
			BytecodeHelper.appendReferenceConstantGetter(ctx, insns, "SUBPATHS", cid);
			insns.add(new InsnNode(Opcodes.SWAP));
			insns.add(new LdcInsnNode(org.objectweb.asm.Type.getType(ctx.getLastOutputClass())));
			insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, 
					org.objectweb.asm.Type.getInternalName(AccessingPath.class), 
					"access", "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Ljava/lang/Object;"));
			ctx.endNode(Object.class);
			return insns;
		};
	}
}
