package lovexyn0827.mess.fakes;

import java.util.List;

import net.minecraft.client.render.debug.DebugRenderer;

public interface DebugRendererEnableState {
	void setEnabledRenderers(List<DebugRenderer.Renderer> renderers);
	void update();
}
