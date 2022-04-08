package lovexyn0827.mess.rendering;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.AbstractRedstoneGateBlockMixin;
import lovexyn0827.mess.options.EnumParser;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
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
			ShapeType type = OptionManager.blockShapeToBeRendered;
			BlockPos pos = world.raycast(new RaycastContext(from, from.add(e.getRotationVector().multiply(10)), type.mjType, RaycastContext.FluidHandling.ANY, e))
					.getBlockPos();
			FluidState fluid = world.getFluidState(pos);
			if(!fluid.isEmpty() && OptionManager.renderFluidShape) {		
				String info = Float.toString(fluid.getHeight())  + '(' + fluid.getLevel() + ')'+ '\n' + fluid.getVelocity(world, pos);
				MessMod.INSTANCE.shapeRenderer.addShape(new RenderedBox(fluid.getShape(world, pos).getBoundingBox().offset(pos), 0xFF0000FF, 0, 1), world.getRegistryKey());
				MessMod.INSTANCE.shapeRenderer.addShape(new RenderedText(info, Vec3d.ofBottomCenter(pos).add(0, 1, 0), 0x000000FF, 1), world.getRegistryKey());;
			} else {
				BlockState block = world.getBlockState(pos);
				VoxelShape voxels = type.getter.getFrom(block, world, pos);
				if(!voxels.isEmpty() && OptionManager.renderBlockShape) {
					voxels.getBoundingBoxes().forEach((b) -> MessMod.INSTANCE.shapeRenderer.addShape(new RenderedBox(b.offset(pos), 0xFF8800FF, 0, 1), world.getRegistryKey()));
				}
				
				if(block.getBlock() instanceof AbstractRedstoneGateBlock && OptionManager.renderRedstoneGateInfo) {
					AbstractRedstoneGateBlock gate = (AbstractRedstoneGateBlock) block.getBlock();
					int out;
					if(block.get(Properties.POWERED).booleanValue()) {
						out = ((AbstractRedstoneGateBlockMixin) gate).getOutputRSLevel(world, pos, block);
					} else {
						out = 0;
					}
					
					String info = "Output :" + Integer.toString(out);
					MessMod.INSTANCE.shapeRenderer.addShape(new RenderedText(info, Vec3d.ofBottomCenter(pos).add(0, 1, 0), 0x000000FF, 1), world.getRegistryKey());;
				}
			}
		}
	}
	
	public static enum ShapeType {
		OUTLINE(RaycastContext.ShapeType.OUTLINE, BlockState::getOutlineShape),
		SIDES(RaycastContext.ShapeType.COLLIDER, BlockState::getSidesShape),
		VISUAL(RaycastContext.ShapeType.VISUAL, (b, w, p) -> b.getVisualShape(w, p, null)),
		RAYCAST(RaycastContext.ShapeType.OUTLINE, BlockState::getRaycastShape),
		COLLISION(RaycastContext.ShapeType.COLLIDER, BlockState::getCollisionShape);
		
		public final RaycastContext.ShapeType mjType;
		public final ShapeGetter getter;
		
		private ShapeType(RaycastContext.ShapeType mjType, ShapeGetter getter) {
			this.mjType = mjType;
			this.getter = getter;
		}
		
		public static class Parser extends EnumParser<ShapeType> {
			public Parser() {
				super(ShapeType.class);
			}
		}
	}
	
	interface ShapeGetter {
		VoxelShape getFrom(BlockState block, World world, BlockPos pos);
	}
}
