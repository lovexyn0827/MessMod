package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class LookingAtEntityHud extends EntityHud {
	public Entity lastLookingAtEntity;
	
	public LookingAtEntityHud(ClientHudManager clientHudManager, HudType type) {
		super(clientHudManager, type);
	}
	
	public void render() {
		String entityInfo = "[Null]";
		if(getData().get(BuiltinHudInfo.ID.getName()) != null) {
			entityInfo = "("+getData().get(BuiltinHudInfo.ID.getName()) + "," + 
					getData().get(BuiltinHudInfo.NAME.getName()) + "," + 
					getData().get(BuiltinHudInfo.AGE.getName()) + ")";
		}
		
		String describe = "Target" + entityInfo;
		if(this.shouldRender) this.render(new MatrixStack(), describe);
	}

	public void addField(Class<?> cl, String string) {
		this.getData();
		// XXX
	}
}
