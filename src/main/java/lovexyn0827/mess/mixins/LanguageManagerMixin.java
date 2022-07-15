package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {
	@Inject(method = "setLanguage(Lnet/minecraft/client/resource/language/LanguageDefinition;)V",  at = @At("HEAD"))
	private void updateLanguage(LanguageDefinition ld, CallbackInfo ci) {
		if("-FOLLOW_SYSTEM_SETTINGS-".equals(OptionManager.language)) {
			I18N.setLanguage(null, false);
		}
	}
}
