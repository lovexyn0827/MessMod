package lovexyn0827.mess.mixins;

import java.util.Collection;
import java.util.Collections;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.fakes.MinecraftClientInterface;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.entity.Entity;

@Mixin(ClientCommandSource.class)
public class ClientCommandSourceMixin {
	@Shadow
	private @Final MinecraftClient client;
	
	@Inject(method = "getEntitySuggestions", at = @At("HEAD"), cancellable = true)
	private void tryUseIndependentlyPickedEntity(CallbackInfoReturnable<Collection<String>> cir) {
		if(OptionManager.independentEntityPickerForInfomation) {
			Entity e = ((MinecraftClientInterface) this.client).getTargetForCommandSuggestions();
			cir.setReturnValue(e == null ? Collections.emptyList() : Collections.singleton(e.getUuidAsString()));
			cir.cancel();
		}
	}
}
