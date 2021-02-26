package mc.lovexyn0827.mcwmem.rendering;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.command.CommandUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;

public class ExplosionRenderer {
	public int lifeTime;
	private MinecraftServer server;
	
	public ExplosionRenderer(MinecraftServer server) {
		this.server = server;
		this.lifeTime = Integer.parseInt(MCWMEMod.INSTANCE.getOption("entityExplosionRayLife"));
	}
	
	public void renderRay(Vec3d from,Vec3d to, boolean rayBlocked) {
		String command = "/script run draw_shape('line',"+this.lifeTime+",'from',l("+from.x+","+from.y+","+from.z+"),'to',l("+to.x+","+to.y+","+to.z+")";
		if(rayBlocked) {
			command += ",'color',"+0x31f38b+")";
		}else {
			command += ")";
		}
		this.server.getCommandManager().execute(CommandUtil.noreplyPlayerSources(), command);
	}
}
