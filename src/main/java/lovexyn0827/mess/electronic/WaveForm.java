package lovexyn0827.mess.electronic;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.ServerMicroTime;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import lovexyn0827.mess.util.phase.TickingPhase.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO: oscillating at a single stage
public final class WaveForm {
	private final int length;
	private final int startModLength;
	
	/**
	 * Non-empty, ascending list of stages with output
	 */
	private final List<WaveForm.Stage> stages;
	
	private WaveForm(int length, int startModLength, List<WaveForm.Stage> stages) {
		this.length = length;
		this.startModLength = startModLength;
		this.stages = stages;
	}

	public void register(World targetWorld, BlockPos pos) {
		for (WaveForm.Stage stg : this.stages) {
			stg.register(targetWorld, pos, this);
		}
		
		WaveGenerator.register(targetWorld, pos, this);
	}
	
	public void unregister() {
		this.stages.forEach(Stage::unregister);
	}
	
	private WaveForm.Stage getStageAt(int tickMod, ServerTickingPhase phase) {
		int left = 0;
		int right = this.stages.size() - 1;
		while (left <= right) {
			int mid = (left + right) / 2;
			WaveForm.Stage midStg = this.stages.get(mid);
			if (midStg.contains(tickMod, phase)) {
				return midStg;
			} else if (midStg.after(tickMod, phase)) {
				right = mid - 1;
			} else {
				left = mid + 1;
			}
		}
		
		return null;
	}
	
	private int getTickMod(long tick) {
		return (int) ((tick + this.startModLength + this.length) % this.length);
	}
	
	public int getCurrentLevel() {
		ServerMicroTime now = ServerMicroTime.current();
		WaveForm.Stage curStg = this.getStageAt(this.getTickMod(now.gameTime), now.phase);
		if (curStg == null) {
			return 0;
		} else {
			return curStg.level;
		}
	}

	/**
	 * (+5NTU=>+3TE)L15S (!14NTU=>!16NTU)L7T
	 * @param in
	 * @return
	 * @throws CommandSyntaxException 
	 */
	public static WaveForm parse(StringReader in) throws CommandSyntaxException {
		if (in.peek() == 'S') {
			in.skip();
			return parseSimpleMode(in);
		} else {
			return parseStandardMode(in);
		}
	}
	
	private static WaveForm parseStandardMode(StringReader in) throws CommandSyntaxException {
		Integer offset = null;
		if (in.peek() == '+' || in.peek() == '-') {
			in.skip();
			offset = in.readInt();
		}
		
		in.skipWhitespace();
		List<WaveForm.Stage> stages = new ArrayList<>();
		WaveForm.Stage prev = null;
		while (in.canRead()) {
			prev = Stage.parseStandardMode(prev, in);
			stages.add(prev);
			in.skipWhitespace();
		}
		
		if (stages.isEmpty()) {
			throw new TranslatableException("cmd.wavegen.err.empty");
		}
		
		int len = stages.get(stages.size() - 1).toTick + 1;
		if (offset == null) {
			offset = (int) (MessMod.INSTANCE.getGameTime() % len);
		} 
		
		if (offset >= len) {
			throw new TranslatableException("cmd.wavegen.err.largeoffset", offset, len);
		}
		
		return new WaveForm(len, offset >= 0 ? offset : len - offset, stages);
	}
	
	private static WaveForm parseSimpleMode(StringReader in) throws CommandSyntaxException {
		WaveForm.Stage prev = null;
		List<WaveForm.Stage> stages = new ArrayList<>();
		while (in.canRead()) {
			prev = Stage.parseSimpleMode(prev, in);
			stages.add(prev);
			in.skipWhitespace();
		}
		
		int len = stages.get(stages.size() - 1).toTick + 1;
		int offset = (int) (MessMod.INSTANCE.getGameTime() % len);
		return new WaveForm(len, offset, stages);
	}
	
	public static void appendSuggestions(SuggestionsBuilder sb) throws CommandSyntaxException {
		StringReader in = new StringReader(sb.getRemaining());
		if (!in.canRead()) {
			sb.suggest("S").suggest("(").suggest("+").suggest("-");
			return;
		}
		
		if (in.peek() == 'S') {
			in.skip();
			appendSimpleModeSuggentions(in, sb);
		} else {
			appendStandardModeSuggestions(in, sb);
		}
	}
	
	private static void appendStandardModeSuggestions(StringReader in, SuggestionsBuilder sb) 
			throws CommandSyntaxException {
		in.setCursor(in.getString().lastIndexOf(' ') + 1);
		Stage.appendStandardModeSuggestions(in, sb);
	}

	private static void appendSimpleModeSuggentions(StringReader in, SuggestionsBuilder sb) {
		if (!in.canRead() || Character.isDigit(in.getRemaining().charAt(in.getRemainingLength() - 1))) {
			sb.suggest("H").suggest("L");
		}
	}

	private static final class Stage implements Comparable<WaveForm.Stage> {
		private final int fromTick;
		private final ServerTickingPhase fromPhase;
		private final int toTick;
		private final ServerTickingPhase toPhase;
		protected final int level;
		private final boolean suppressesOnUpdates;
		private final boolean suppressesOffUpdates;
		private Event onBlockUpdater;
		private Event offBlockUpdater;
		
		public Stage(int fromTick, ServerTickingPhase fromPhase, int toTick, ServerTickingPhase toPhase,
				int level, boolean suppressesOnUpdates, boolean suppressesOffUpdates) {
			this.fromTick = fromTick;
			this.fromPhase = fromPhase;
			this.toTick = toTick;
			this.toPhase = toPhase;
			this.level = level;
			this.suppressesOnUpdates = suppressesOnUpdates;
			this.suppressesOffUpdates = suppressesOffUpdates;
		}

		private static long flatten(int tickMod, ServerTickingPhase phase) {
			// Warning: We can do this only if the number of available phases is less than 16
			return tickMod << 4 | phase.ordinal();
		}
		
		protected boolean contains(int tickMod, ServerTickingPhase phase) {
			long tickModFlat = flatten(tickMod, phase);
			return tickModFlat >= flatten(this.fromTick, this.fromPhase) 
					&& tickModFlat < flatten(this.toTick, this.toPhase);
		}
		
		protected boolean after(int tickMod, ServerTickingPhase phase) {
			long tickModFlat = flatten(tickMod, phase);
			return tickModFlat < flatten(this.fromTick, this.fromPhase);
		}

		protected void register(World targetWorld, BlockPos pos, WaveForm wave) {
			this.onBlockUpdater = (phase, world) -> {
				if (world != targetWorld) {
					return;
				}
				
				long curTimeMod = wave.getTickMod(MessMod.INSTANCE.getGameTime());
				if (curTimeMod == this.fromTick) {
					world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
				}
			};
			this.offBlockUpdater = (phase, world) -> {
				if (world != targetWorld) {
					return;
				}
				
				long curTimeMod = wave.getTickMod(MessMod.INSTANCE.getGameTime());
				if (curTimeMod == this.toTick) {
					world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
				}
			};
			if (!this.suppressesOnUpdates) {
				this.fromPhase.addEvent(this.onBlockUpdater);
			}

			if (!this.suppressesOffUpdates) {
				this.toPhase.addEvent(this.offBlockUpdater);
			}
		}
		
		protected void unregister() {
			this.fromPhase.removeEvent(this.onBlockUpdater);
			this.toPhase.removeEvent(this.offBlockUpdater);
		}

		protected boolean canFollow(Stage prev) {
			return prev == null || this.after(prev.fromTick, prev.fromPhase);
		}
		
		private static int readTick(int base, StringReader in) throws CommandSyntaxException {
			switch (in.read()) {
			case '+':
				return in.readInt() + base;
			case '!':
				return in.readInt();
			default:
				throw new TranslatableException("cmd.wavegen.err.stgfmt");
			}
		}
		
		private static ServerTickingPhase readTickingPhase(char end, StringReader in) throws CommandSyntaxException {
			String name = in.readStringUntil(end);
			int cursor = in.getCursor();
			if (name.isEmpty()) {
				in.setCursor(cursor);
				return ServerTickingPhase.SCHEDULED_TICK;
			}

			try {
				return ServerTickingPhase.byNameOrAbbreviation(name);
			} catch (IllegalArgumentException e) {
				throw new TranslatableException("cmd.general.nodef", name);
			}
		}
		
		static boolean readOptionalFlag(StringReader in, char flag) {
			if (in.canRead() && in.peek() == flag) {
				in.skip();
				return true;
			} else {
				return false;
			}
		}
		
		static WaveForm.Stage parseSimpleMode(WaveForm.Stage prev, StringReader in) throws CommandSyntaxException {
			int level;
			switch (in.read()) {
			case 'H':
				level = 15;
				break;
			case 'L':
				level = 0;
				break;
			default:
				throw new TranslatableException("cmd.wavegen.err.stgfmt.simp");
			}
			
			int base = prev == null ? 0 : prev.toTick;
			return validateOrder(new Stage(base, ServerTickingPhase.SCHEDULED_TICK, 
					base + in.readInt(), ServerTickingPhase.SCHEDULED_TICK, 
					level, false, false), prev);
		}
		
		private static Stage validateOrder(Stage candidate, Stage prev) {
			if (candidate.after(candidate.toTick, candidate.toPhase)) {
				throw new TranslatableException("cmd.wavegen.err.tobeforefrom");
			}
			
			if (!candidate.canFollow(prev)) {
				throw new TranslatableException("cmd.wavegen.err.reqorder");
			} else {
				return candidate;
			}
		}
		
		static WaveForm.Stage parseStandardMode(WaveForm.Stage prev, StringReader in) throws CommandSyntaxException {
			if (in.read() != '(') {
				throw new TranslatableException("cmd.wavegen.err.stgfmt");
			}
			
			int from = readTick(prev == null ? 0 : prev.toTick, in);
			ServerTickingPhase fromPhase = readTickingPhase('=', in);
			if (in.read() != '>') {
				throw new TranslatableException("cmd.wavegen.err.stgfmt");
			}
			
			int to = readTick(from, in);
			ServerTickingPhase toPhase = readTickingPhase(')', in);
			int level;
			if (!in.canRead() || in.peek() == ' ' || in.peek() == '(') {
				level = 15;
			} else if (in.peek() == 'L') {
				in.skip();
				level = in.readInt();
			} else {
				throw new TranslatableException("cmd.wavegen.err.stgfmt");
			}
			
			boolean suppressesOnUpdates = readOptionalFlag(in, 'S');
			boolean suppressesOffUpdates = readOptionalFlag(in, 'T');
			return validateOrder(new Stage(from, fromPhase, to, toPhase, level, 
					suppressesOnUpdates, suppressesOffUpdates), prev);
			
		}

		public static void appendStandardModeSuggestions(StringReader in, SuggestionsBuilder sb) {
			if (!in.canRead()) {
				sb.suggest(in.getString() + "(");
				return;
			}

			in.skip();
			if (!suggestTick(in, sb, '=')) {
				return;
			} else if (!in.canRead()) {
				sb.suggest(in.getString() + "=>");
			}
			
			in.skip();
			if (!in.canRead()) {
				sb.suggest(in.getString() + ">");
				return;
			}

			in.skip();
			if (!suggestTick(in, sb, ')')) {
				return;
			} else if (!in.canRead()) {
				sb.suggest(in.getString() + ")");
				return;
			}

			in.skip();
			if (!in.canRead()) {
				sb.suggest(in.getString() + "L");
				return;
			}

			in.skip();
			if (!in.canRead()) {
				sb.suggest(in.getString() + "0").suggest(in.getString() + "15").suggest(in.getString() + "1");
				return;
			}
			
			while (in.canRead() && Character.isDigit(in.peek())) {
				in.read();
			}
			
			CharSet updateFlags = new CharOpenHashSet();
			while (in.canRead()) {
				char c = in.read();
				if (c == 'S' || c == 'T') {
					updateFlags.add(c);
				}
			}
			
			if (!updateFlags.contains('S')) {
				sb.suggest(in.getString() + "S");
			}

			if (!updateFlags.contains('T')) {
				sb.suggest(in.getString() + "T");
			}
			
		}

		private static boolean suggestTick(StringReader in, SuggestionsBuilder sb, char delim) {
			if (!in.canRead()) {
				sb.suggest(in.getString() + "!").suggest(in.getString() + "+");
				return false;
			}
			
			if (in.peek() == '!' || in.peek() == '+') {
				in.skip();
			}
			
			int numCount = 0;
			while (in.canRead() && Character.isDigit(in.peek())) {
				in.skip();
				numCount++;
			}
			
			if (numCount > 0) {
				for (ServerTickingPhase phase : ServerTickingPhase.values()) {
					if (phase.name().startsWith(in.getRemaining())) {
						sb.suggest(in.getString() + phase.name().substring(in.getRemainingLength()));
					}
				
					if (phase.abbreviation().startsWith(in.getRemaining())) {
						sb.suggest(in.getString() + phase.abbreviation().substring(in.getRemainingLength()));
					}
				}
			}
			
			while (in.canRead() && in.peek() != delim) {
				in.skip();
			}
			
			return numCount > 0;
		}

		@Override
		public int compareTo(WaveForm.Stage s) {
			if (s == null) {
				return 1;
			}
			
			if (this.fromTick - s.fromTick != 0) {
				return this.fromTick - s.fromTick;
			}
			
			return this.fromPhase.compareTo(s.fromPhase);
		}
	}
}