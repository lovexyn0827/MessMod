package lovexyn0827.mess.rendering;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.mixins.AbstractRedstoneGateBlockMixin;
import lovexyn0827.mess.options.EnumParser;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.CarpetUtil;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BlockInfoRenderer {
	private static final ShapeSpace BLOCK_INFO_SPACE = new ShapeSpace("block_info");
	
	private MinecraftServer server;
	
	public void initializate(MinecraftServer server) {
		this.server = server;
	}
	
	public void uninitializate() {
		this.server = null;
	}

	public void tick() {
		ShapeSender sr = MessMod.INSTANCE.shapeSender;
		if(this.server != null && sr != null) {
			boolean frozen = CarpetUtil.isTickFrozen();
			if(frozen && OptionManager.blockInfoRendererUpdateInFrozenTicks == FrozenUpdateMode.PAUSE) {
				return;
			}
			
			// Very SB 
			for(ServerPlayerEntity e : this.server.getPlayerManager().getPlayerList()) {
				if(!frozen || OptionManager.blockInfoRendererUpdateInFrozenTicks == FrozenUpdateMode.NORMALLY) {
					sr.clearSpaceFromServer(BLOCK_INFO_SPACE, e);
				}
				
				Vec3d from = new Vec3d(e.getX(), e.getEyeY(), e.getZ());
				Vec3d to = from.add(e.getRotationVector().multiply(16));
				ServerWorld serverWorld = this.server.getWorld(e.getEntityWorld().getRegistryKey());
				BlockView world = OptionManager.directChunkAccessForMessMod ? 
						((ServerWorldInterface) serverWorld).toNoChunkLoadingWorld() : serverWorld;
				ShapeType type = OptionManager.blockShapeToBeRendered;
				BlockPos pos = world.raycast(
						new RaycastContext(from, to, type.mjType, RaycastContext.FluidHandling.ANY, e)).getBlockPos();
				FluidState fluid = world.getFluidState(pos);
				long time = serverWorld.getTime();
				RegistryKey<World> worldKey = serverWorld.getRegistryKey();
				if(!fluid.isEmpty() && OptionManager.renderFluidShape) {
					Vec3d flow = fluid.getVelocity(world, pos);
					float fluidHeight = fluid.getHeight();
					String info = Float.toString(fluidHeight)  + '(' + fluid.getLevel() + ')'+ '\n' + flow;
					Box fluidBox = fluid.getShape(world, pos).getBoundingBox().offset(pos);
					sr.addShape(new RenderedBox(fluidBox, 0xFF0000FF, 0, 1, time), worldKey, BLOCK_INFO_SPACE, e);
					sr.addShape(new RenderedText(info, Vec3d.ofBottomCenter(pos).add(0, 1, 0), 0xFF0000FF, 1, time), 
							worldKey, BLOCK_INFO_SPACE, e);
					if(flow.length() != 0) {
						Vec3d displayedFlow = flow.multiply(-0.5D)
								.add(pos.getX() + 0.5D, pos.getY() + fluidHeight / 2, pos.getZ() + 0.5D);
						sr.addShape(new RenderedLine(displayedFlow, displayedFlow.add(flow), 0x000000FF, 1, time), 
								worldKey, BLOCK_INFO_SPACE, e);
					}
				} else {
					BlockState block = world.getBlockState(pos);
					VoxelShape voxels = type.getter.getFrom(block, serverWorld, pos);
					if(!voxels.isEmpty() && OptionManager.renderBlockShape) {
						voxels.getBoundingBoxes().forEach((b) -> {
							sr.addShape(new RenderedBox(b.offset(pos), 0xFF8800FF, 0, 1, time), 
									worldKey, BLOCK_INFO_SPACE, e);
						});
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
						sr.addShape(new RenderedText(info, Vec3d.ofBottomCenter(pos).add(0, 1, 0), 0x0000FFFF, 1, time), 
								worldKey, BLOCK_INFO_SPACE, e);
					}
				}
			}
		}	
	}
	
	public static enum ShapeType {
		OUTLINE(RaycastContext.ShapeType.OUTLINE, BlockState::getOutlineShape),
		SIDES(RaycastContext.ShapeType.COLLIDER, BlockState::getSidesShape),
		VISUAL(RaycastContext.ShapeType.VISUAL, (b, w, p) -> b.getCameraCollisionShape(w, p, ShapeContext.absent())),
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
		VoxelShape getFrom(BlockState block, BlockView world, BlockPos pos);
	}
}
