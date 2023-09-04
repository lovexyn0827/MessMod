package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.data.HudDataStorage;
import lovexyn0827.mess.rendering.hud.data.RemoteHudDataStorage;

public class EntitySideBar extends EntityHud {
	public EntitySideBar(ClientHudManager clientHudManager) {
		super(clientHudManager, HudType.SIDEBAR);
		this.shouldRender = true;
	}
	
	public void render() {
		if(this.getData().size() > 0) {
			this.render("Entity Sidebar");
		}
	}
	
	@Override
	protected HudDataStorage createDataStorage(HudType type) {
		if(type != HudType.SIDEBAR) {
			throw new IllegalArgumentException();
		}
		
		if (MessMod.isDedicatedEnv()) {
			return new RemoteHudDataStorage();
		} else {
			return (HudDataStorage) MessMod.INSTANCE.getServerHudManager().sidebar;
		}
	}
}
