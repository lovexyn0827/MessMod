package lovexyn0827.mess.util;

import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lovexyn0827.mess.options.EnumParser;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public final class PulseRecorder {
	private final Object2BooleanMap<BlockPos> activationStates = new Object2BooleanOpenHashMap<>();
	private final Object2ObjectMap<BlockPos, ServerMicroTime> lastChangeTimes = new Object2ObjectOpenHashMap<>();

	public Optional<Pulse> setSignalLevel(BlockPos pos, boolean activated) {
		if(!this.activationStates.containsKey(pos)) {
			this.activationStates.put(pos, activated);
			this.lastChangeTimes.put(pos, ServerMicroTime.current());
			return Optional.empty();
		}
		
		if(this.activationStates.getBoolean(pos) ^ activated) {
			Pulse pulse = new Pulse(pos, !activated);
			this.activationStates.put(pos, activated);
			this.lastChangeTimes.put(pos, ServerMicroTime.current());
			if(OptionManager.fletchingTablePulseDetectingMode.shouldRecordPulse.test(pulse)) {
				return Optional.of(pulse);
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}

	public class Pulse {
		public final boolean positive;
		public final BlockPos pos;
		public final long length;
		public final ServerMicroTime start;
		public final ServerMicroTime end;
		
		protected Pulse(BlockPos pos, boolean positive) {
			this.positive = positive;
			this.pos = pos;
			this.start = PulseRecorder.this.lastChangeTimes.get(pos);
			this.end = ServerMicroTime.current();
			this.length = this.end.gameTime - this.start.gameTime;
		}
		
		public Text toText() {
			MutableText text = new LiteralText(positive ? "+" : "-");
			String details = String.format("From: %d#%s\nTo: %d#%s\nAt: (%d, %d, %d)", 
					this.start.gameTime, this.start.phase == null ? "?" : this.start.phase.name(), 
					this.end.gameTime, this.end.phase == null ? "?" : this.end.phase.name(), 
					pos.getX(), pos.getY(), pos.getZ());
			HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(details));
			text.formatted(Formatting.BOLD, positive ? Formatting.RED : Formatting.BLUE);
			text.append(new LiteralText(Long.toString(this.length)).formatted(Formatting.RESET).fillStyle(Style
					.EMPTY.withHoverEvent(he)));
			return text;
		}
	}
	
	public static enum Mode {
		POSITIVE((p) -> ((Pulse) p).positive), 
		NEGATIVE((p) -> !((Pulse) p).positive), 
		BOTH((p) -> true);
		
		protected final Object2BooleanFunction<Pulse> shouldRecordPulse;
		
		private Mode(Object2BooleanFunction<Pulse> shouldRecordPulse) {
			this.shouldRecordPulse = shouldRecordPulse;
		}
		
		public static class Parser extends EnumParser<Mode> {
			public Parser() {
				super(Mode.class);
			}
		}
	}
}
