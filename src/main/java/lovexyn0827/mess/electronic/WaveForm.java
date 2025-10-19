package lovexyn0827.mess.electronic;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import lovexyn0827.mess.util.phase.TickingPhase.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WaveForm {
	private final int length;
	private final int startModLength;
	private Stage currentStage = null;
	
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
		
		MessMod.INSTANCE.getWaveGenerator().register(targetWorld, pos, this);
	}
	
	public void unregister() {
		this.stages.forEach(Stage::unregister);
	}
	
	private int getTickMod(long tick) {
		return (int) ((tick + this.startModLength + this.length) % this.length);
	}
	
	public int getCurrentLevel() {
		if (this.currentStage == null) {
			return 0;
		} else {
			return this.currentStage.level;
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
		List<Stage> stages = parseStages(in, null, 1);
		if (stages.isEmpty()) {
			throw new TranslatableException("cmd.wavegen.err.empty");
		}
		
		stages.get(stages.size() - 1).setImmediateSuccessor(stages.get(0), true, false);
		int len = stages.get(stages.size() - 1).toTick + 1;
		if (offset == null) {
			offset = (int) (MessMod.INSTANCE.getGameTime() % len);
		} 
		
		if (offset >= len) {
			throw new TranslatableException("cmd.wavegen.err.largeoffset", offset, len);
		}
		
		return new WaveForm(len, offset >= 0 ? offset : len - offset, stages);
	}
	
	private static List<Stage> parseStages(StringReader in, Stage prev, int repeat) throws CommandSyntaxException {
		List<Stage> stages = new ArrayList<>();
		int cursor = in.getCursor();
		for (int i = 0; i < repeat; i++) {
			in.setCursor(cursor);
			while (in.canRead()) {
				if (in.peek() == ']') {
					in.read();
					break;
				}
				
				// 3[(+0=>+0)L15 (+0=>+0)L0]
				char firstCh = in.peek();
				if (Character.isDigit(firstCh)) {
					stages.addAll(parseStages(in, prev, readRepeatitionStart(in)));
					if (!stages.isEmpty()) {
						prev = stages.get(stages.size() - 1);
					}
				} else if (firstCh == '(') {
					Stage cur = Stage.parseStandardMode(prev, in);
					if (prev != null) {
						prev.setImmediateSuccessor(cur, false, false);
					}
					
					stages.add(cur);
					prev = cur;
				} else {
					throw new TranslatableException("cmd.wavegen.err.stgfmt");
				}
				
				in.skipWhitespace();
			}
		}
		
		return stages;
	}
	
	private static int readRepeatitionStart(StringReader in) throws CommandSyntaxException {
		int repeat = in.readInt();
		if (in.canRead() && in.peek() == '*') {
			in.skip();
		}
		
		if (!in.canRead() || in.read() != '[') {
			throw new TranslatableException("cmd.wavegen.err.stgfmt");
		}
		
		return repeat;
	}

	private static WaveForm parseSimpleMode(StringReader in) throws CommandSyntaxException {
		WaveForm.Stage prev = null;
		List<WaveForm.Stage> stages = new ArrayList<>();
		while (in.canRead()) {
			Stage cur = Stage.parseSimpleMode(prev, in);
			if (prev != null) {
				prev.setImmediateSuccessor(cur, false, true);
			}
			
			stages.add(cur);
			prev = cur;
			in.skipWhitespace();
		}

		if (stages.isEmpty()) {
			throw new TranslatableException("cmd.wavegen.err.empty");
		}
		
		Stage last = stages.get(stages.size() - 1);
		last.setImmediateSuccessor(stages.get(0), true, true);
		int len = last.toTick + 1;
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
		String inStr = in.getString();
		in.setCursor(Math.max(inStr.lastIndexOf(' '), inStr.lastIndexOf('[')) + 1);
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
		private Stage immediateSuccessor;
		
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
		
		protected boolean after(int tickMod, ServerTickingPhase phase) {
			long tickModFlat = flatten(tickMod, phase);
			return tickModFlat < flatten(this.fromTick, this.fromPhase);
		}
		
		private boolean notBefore(int tickMod, ServerTickingPhase phase) {
			long tickModFlat = flatten(tickMod, phase);
			return tickModFlat <= flatten(this.fromTick, this.fromPhase);
		}

		protected void register(World targetWorld, BlockPos pos, WaveForm wave) {
			this.onBlockUpdater = (phase, world) -> {
				if (world != targetWorld) {
					return;
				}
				
				long curTimeMod = wave.getTickMod(MessMod.INSTANCE.getGameTime());
				if (curTimeMod == this.fromTick) {
					wave.currentStage = this;
					world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
				}
			};
			this.offBlockUpdater = (phase, world) -> {
				if (world != targetWorld) {
					return;
				}
				
				long curTimeMod = wave.getTickMod(MessMod.INSTANCE.getGameTime());
				if (curTimeMod == this.toTick) {
					wave.currentStage = this.immediateSuccessor;
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
			return prev == null || this.notBefore(prev.fromTick, prev.fromPhase);
		}
		
		protected void setImmediateSuccessor(Stage successor, boolean atEnds, boolean simpleMode) {
			boolean noGap;
			if (atEnds) {
				noGap = simpleMode 
						|| successor.fromPhase.ordinal() == 0 && this.toPhase == ServerTickingPhase.REST;
			} else {
				noGap = successor.followsImmediately(this);
			}

			this.immediateSuccessor = noGap ? successor : null;
			
		}

		public boolean followsImmediately(Stage prev) {
			return flatten(this.fromTick, this.fromPhase) == flatten(prev.toTick, prev.toPhase);
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
		
		private static boolean isDelim(char c) {
			return c == ' ' || c == ']';
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
			if (!in.canRead() || isDelim(in.peek()) || in.peek() == '(') {
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
			while (in.canRead() && !isDelim(in.peek())) {
				char c = in.read();
				if (c == 'S' || c == 'T') {
					updateFlags.add(c);
				}
			}
			
			if (in.canRead() && isDelim(in.peek())) {
				return;
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

		@Override
		public String toString() {
			return String.format("Stage[%d:%s => %d:%s]L%d%c%c", 
					this.fromTick, this.fromPhase.abbreviation(), 
					this.toTick, this.toPhase.abbreviation(), 
					this.level, 
					this.suppressesOnUpdates ? 'S' : '_', 
					this.suppressesOffUpdates ? 'T' : '_');
		}
		
		
	}
}