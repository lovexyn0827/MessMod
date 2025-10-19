package lovexyn0827.mess.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.CommandTextFieldWidget;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Lazy;

@Mixin(value = AbstractCommandBlockScreen.class, priority = 899)
public class AbstractCommandBlockScreenMixin extends Screen {
	@Shadow
	private @Final TextFieldWidget consoleCommandTextField;
	
	@Shadow
	private CommandSuggestor commandSuggestor;

	protected AbstractCommandBlockScreenMixin(Text title) {
		super(title);
	}
	
	@Redirect(
			method = "init", at = @At(
					value = "FIELD", 
					target = "Lnet/minecraft/client/gui/screen/ingame/AbstractCommandBlockScreen;"
							+ "consoleCommandTextField:Lnet/minecraft/client/gui/widget/TextFieldWidget;", 
					opcode = Opcodes.PUTFIELD
			)
	)
	private void redirectSetTextField(AbstractCommandBlockScreen screen, TextFieldWidget orig) {
		if (OptionManager.smartCursorMode == CommandTextFieldWidget.CursorMode.VANILLA) {
			this.consoleCommandTextField = orig;
		} else {
			this.consoleCommandTextField = new CommandTextFieldWidget(
					this.textRenderer, this.width / 2 - 150, 50, 300, 20, 
					(Text)new TranslatableText("advMode.command"), false, 
					new Lazy<>(() -> this.commandSuggestor));
		}
	}
}
