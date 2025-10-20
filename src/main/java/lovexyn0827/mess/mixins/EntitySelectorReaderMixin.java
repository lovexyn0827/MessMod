package lovexyn0827.mess.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.EntitySelectorInterface;
import lovexyn0827.mess.fakes.EntitySelectorReaderInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import lovexyn0827.mess.util.i18n.I18N;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@Mixin(EntitySelectorReader.class)
public abstract class EntitySelectorReaderMixin implements EntitySelectorReaderInterface {
	@Shadow
	private List<Predicate<Entity>> predicates;
	@Shadow
	private int limit;
	@Shadow
    private boolean includesNonPlayers;
	@Shadow
	private BiConsumer<Vec3d, List<? extends Entity>> sorter;
	@Shadow
	private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionProvider;
	@Shadow @Final
	private StringReader reader;
	private IntRange idRange;
	private NetworkSide side;
	private Pattern typeRegex;
	private Pattern nameRegex;
	private Pattern classRegex;
	private Class<?> clazz;
	private boolean targetOnly;
	
	@Shadow
	protected abstract CompletableFuture<Suggestions> suggestOpen(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

	@Shadow
	protected abstract void readArguments();
	
	@Shadow
	protected abstract CompletableFuture<Suggestions> suggestOptionOrEnd(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);
	
	@Override
	public void setIdRange(IntRange range) {
		this.idRange = range;
	}

	@Override
	public IntRange getIdRange() {
		return this.idRange;
	}

	@Override
	public void setSide(NetworkSide side) {
		this.side = side;
	}

	@Override
	public NetworkSide getSide() {
		return this.side;
	}
	
	@Redirect(method = "read", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/command/EntitySelectorReader;build()Lnet/minecraft/command/EntitySelector;"
			)
	)
	public EntitySelector addCustomOptions(EntitySelectorReader reader){
		if(this.idRange != null) {
			this.predicates.add((e) -> this.idRange.test(e.getId()));
		}
		
		if(this.typeRegex != null) {
			this.predicates.add((e) -> {
				return this.typeRegex.matcher(EntityType.getId(e.getType()).toString()).matches();
			});
		}
		
		if(this.nameRegex != null) {
			this.predicates.add((e) -> {
				return this.nameRegex.matcher(e.getName().getString()).matches();
			});
		}
		
		if(this.classRegex != null) {
			this.predicates.add((e) -> {
				Class<?> clazz = e.getClass();
				Mapping mapping = MessMod.INSTANCE.getMapping();
				for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
					String canonicalName = mapping.namedClass(clazz.getCanonicalName());
					String simpleName = mapping.simpleNamedClass(clazz.getCanonicalName());
					if(this.classRegex.matcher(canonicalName.substring(canonicalName.lastIndexOf('.'))).matches()
							|| this.classRegex.matcher(simpleName).matches()) {
						return true;
					}
				}
				
				return false;
			});
		}
		
		if(this.clazz != null) {
			this.predicates.add(this.clazz::isInstance);
		}

		EntitySelector selector = this.build();
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			((EntitySelectorInterface) selector).setSide(this.side == null ? NetworkSide.SERVERBOUND : this.side);
		}
		
		((EntitySelectorInterface) selector).setTargetOnly(this.targetOnly);
		return selector;
	}

	@Shadow
	private EntitySelector build() {
		throw new AssertionError();
	}

	@Override
	public void setTypeRegex(Pattern typeRegex) {
		this.typeRegex = typeRegex;
	}

	@Override
	public Pattern getTypeRegex() {
		return this.typeRegex;
	}

	@Override
	public void setNameRegex(Pattern nameRegex) {
		this.nameRegex = nameRegex;
	}

	@Override
	public Pattern getNameRegex() {
		return this.nameRegex;
	}

	@Override
	public void setClassRegex(Pattern classRegex) {
		this.classRegex = classRegex;
	}

	@Override
	public Pattern getClassRegex() {
		return classRegex;
	}

	@Redirect(method = "readAtVariable", 
			at = @At(
					value = "INVOKE", 
					target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
			)
	)
	private boolean replaceIsAlive(List<Object> predicates, Object p0) {
		if(!OptionManager.allowSelectingDeadEntities) {
			return predicates.add(p0);
		} else {
			return true;
		}
	}
	
	@Inject(method = "readAtVariable", 
			at = @At(
					value = "INVOKE", 
					target = "com/mojang/brigadier/StringReader.setCursor(I)V", 
					remap = false
			), 
			locals = LocalCapture.CAPTURE_FAILHARD, 
			cancellable = true
	)
	private void addAtTSelector(CallbackInfo ci, int i, char c) {
		if(c == 't') {
			this.limit = 1;
			this.includesNonPlayers = true;
			this.sorter = EntitySelectorReader.NEAREST;
			this.predicates = new ArrayList<>();
			this.predicates.add((e) -> true);
			this.targetOnly = true;
			this.suggestionProvider = this::suggestOpen;
	        if (this.reader.canRead() && this.reader.peek() == '[') {
	            this.reader.skip();
	            this.suggestionProvider = this::suggestOptionOrEnd;
	            this.readArguments();
	        }
	        
	        ci.cancel();
		}
	}
	
	@Inject(method = "suggestSelector", at = @At("RETURN"))
	private static void suggestAtTSelector(SuggestionsBuilder builder, CallbackInfo ci) {
		builder.suggest("@t", Text.literal(I18N.translate("misc.selector.att")));
	}

	@Override
	public void setInstanceofClass(Class<?> cl) {
		this.clazz = cl;
	}

	@Override
	public Class<?> getInstanceofClass() {
		return this.clazz;
	}

	@Override
	public boolean targetOnly() {
		return this.targetOnly;
	}
}
