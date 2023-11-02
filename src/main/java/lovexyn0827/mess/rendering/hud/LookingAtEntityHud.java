package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import net.minecraft.client.util.math.MatrixStack;

public class LookingAtEntityHud extends EntityHud {
	public LookingAtEntityHud(ClientHudManager clientHudManager) {
		super(clientHudManager, HudType.TARGET);
	}
	
	public void render() {
		String entityInfo;
		if(this.getData().get(BuiltinHudInfo.ID) != null) {
			entityInfo = "("+getData().get(BuiltinHudInfo.ID) + "," + 
					getData().get(BuiltinHudInfo.NAME) + "," + 
					getData().get(BuiltinHudInfo.AGE) + ")";
		} else {
			entityInfo = "[Null]";
		}
		
		String describe = "Target" + entityInfo;
		if(this.shouldRender) this.render(new MatrixStack(), describe);
	}
}
