package lovexyn0827.mess.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.CommandTextFieldWidget;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Lazy;

@Mixin(value = ChatScreen.class, priority = 899)
public abstract class ChatScreenMixin extends Screen {
	@Shadow
	private @Final TextFieldWidget chatField;
	
	@Shadow
	private CommandSuggestor commandSuggestor;
	
	protected ChatScreenMixin(Text title) {
		super(title);
	}

	@Redirect(
			method = "init", at = @At(
					value = "FIELD", 
					target = "chatField:Lnet/minecraft/client/gui/widget/TextFieldWidget;", 
					opcode = Opcodes.PUTFIELD
			)
	)
	private void redirectTextFieldCreation(ChatScreenMixin screen, TextFieldWidget orig) {
		if (OptionManager.smartCursorMode == CommandTextFieldWidget.CursorMode.VANILLA) {
			// vanilla path
			this.chatField = orig;
		} else {
			this.chatField = new CommandTextFieldWidget(this.textRenderer, orig.x, orig.y, 
					orig.getWidth(), orig.getHeight(), orig.getMessage(), true, 
					new Lazy<>(() -> this.commandSuggestor));
		}
	}
}
