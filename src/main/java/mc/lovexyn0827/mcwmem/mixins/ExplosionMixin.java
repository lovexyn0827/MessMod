package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import mc.lovexyn0827.mcwmem.command.SetExplosionBlockCommand;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BlockState;
import net.minecraft.world.explosion.Explosion;

@Mixin(Explosion.class)
public class ExplosionMixin {
	@ModifyArg(method = "affectWorld",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
			index = 1)
	public BlockState replaceToCustomBlockState(BlockState blockState) {
		BlockState customBlockState = SetExplosionBlockCommand.getBlockState();
		return customBlockState == null?blockState:customBlockState;
	}
	
	@ModifyArg(method = "affectWorld",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"),
			index = 1)
	public BlockState replaceToCustomFireState(BlockState fireState) {
		BlockState customFireState = SetExplosionBlockCommand.getFireState();
		return customFireState == null?fireState:customFireState;
	}
}
