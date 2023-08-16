package lovexyn0827.mess.mixins;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.FillCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockBox;

@Mixin(FillCommand.class)
public class FillCommandMixin {
	@Inject(method = "execute", at = @At("HEAD"))
	private static void onFillBegin(ServerCommandSource source, BlockBox range, BlockStateArgument block, 
			@Coerce Enum<?> mode, @Nullable Predicate<CachedBlockPosition> filter, CallbackInfoReturnable<Integer> cir) {
		if (OptionManager.fillHistory) {
			Entity entity = source.getEntity();
			if (entity instanceof ServerPlayerEntity) {
				((ServerPlayerEntityInterface) entity).getBlockPlacementHistory().beginOperation();
			} 
		}
	}
	
	@Inject(method = "execute", at = @At("RETURN"))
	private static void onFillFinish(ServerCommandSource source, BlockBox range, BlockStateArgument block, 
			@Coerce Enum<?> mode, @Nullable Predicate<CachedBlockPosition> filter, CallbackInfoReturnable<Integer> cir) {
		if(OptionManager.fillHistory) {
			Entity entity = source.getEntity();
			if (entity instanceof ServerPlayerEntity) {
				((ServerPlayerEntityInterface) entity).getBlockPlacementHistory().endOperation(false);
			} 
		}
	}
	
	@Inject(method = "execute", at = {
			@At(value = "INVOKE", target = "com/mojang/brigadier/exceptions/SimpleCommandExceptionType.create"
					+ "()Lcom/mojang/brigadier/exceptions/CommandSyntaxException;"), 
			@At(value = "INVOKE", target = "com/mojang/brigadier/exceptions/Dynamic2CommandExceptionType.create"
					+ "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/brigadier/exceptions/CommandSyntaxException;")})
	private static void onFillFail(ServerCommandSource source, BlockBox range, BlockStateArgument block, 
			@Coerce Enum<?> mode, @Nullable Predicate<CachedBlockPosition> filter, CallbackInfoReturnable<Integer> cir) {
		if(OptionManager.fillHistory) {
			Entity entity = source.getEntity();
			if (entity instanceof ServerPlayerEntity) {
				((ServerPlayerEntityInterface) entity).getBlockPlacementHistory().endOperation(true);
			} 
		}
	}
}
