package mc.lovexyn0827.mcwmem.rendering;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BlockInfoRenderer {
	private MinecraftServer server;
	
	public void initializate(MinecraftServer server) {
		this.server = server;
	}
	
	public void uninitializate() {
		this.server = null;
	}

	@SuppressWarnings("resource")
	public void tick() {
		if(this.server != null) {
			// Very SB 
			Entity e = MinecraftClient.getInstance().cameraEntity;
			if(e == null) {
				e = MinecraftClient.getInstance().player;
			}
			if(e == null) return;
			Vec3d from = new Vec3d(e.getX(), e.getEyeY(), e.getZ());
			World world = this.server.getWorld(e.getEntityWorld().getRegistryKey());
			BlockPos pos = world.raycast(new RaycastContext(from, from.add(e.getRotationVector().multiply(10)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, e))
					.getBlockPos();
			FluidState fluid = world.getFluidState(pos);
			if(!fluid.isEmpty() && MCWMEMod.INSTANCE.getBooleanOption("renderFluidShape")) {		
				String info = Float.toString(fluid.getHeight())  + '(' + fluid.getLevel() + ')'+ '\n' + fluid.getVelocity(world, pos);
				MCWMEMod.INSTANCE.shapeRenderer.addShape(new RenderedBox(fluid.getShape(world, pos).getBoundingBox().offset(pos), 0xFF0000FF, 0, 1), world.getRegistryKey());
				MCWMEMod.INSTANCE.shapeRenderer.addShape(new RenderedText(info, Vec3d.ofBottomCenter(pos).add(0, 1, 0), 0x000000FF, 1), world.getRegistryKey());;
			} else {
				BlockState block = world.getBlockState(pos);
				VoxelShape voxels = block.getCollisionShape(world, pos);
				if(!voxels.isEmpty() && MCWMEMod.INSTANCE.getBooleanOption("renderBlockShape")) {
					voxels.getBoundingBoxes().forEach((b) -> MCWMEMod.INSTANCE.shapeRenderer.addShape(new RenderedBox(b.offset(pos), 0xFF8800FF, 0, 1), world.getRegistryKey()));
				}
			}
		}
	}
}
