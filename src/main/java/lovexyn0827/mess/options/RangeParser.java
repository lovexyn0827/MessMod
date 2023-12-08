package lovexyn0827.mess.options;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;

import net.minecraft.world.chunk.ChunkStatus;

public abstract class RangeParser<T extends Comparable<T>> extends ListParser<T> {
	public RangeParser(BiMap<String, T> elements) {
		super(elements);
	}
	
	@Override
	/**
	 * Example: "IN1,IN2,IN3", "IN1,,IN3", ",,IN5", "IN1,IN3,,IN5"
	 */
	public List<T> tryParse(String str) throws InvalidOptionException {
		if(EMPTY_LIST.equals(str) || str.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<T> result = Lists.newArrayList();
		String[] splited = str.split(",");;
		for(int i = 0; i < splited.length; i++) {
			if("...".equals(splited[i])) {
				if(i == 0 && i < splited.length - 1) {
					T max = this.elements.get(splited[i + 1]);
					if(max == null) {
						throw new InvalidOptionException("cmd.general.nodef", splited[i + 1]);
					}
					
					this.elements.values().stream().filter((e) -> e.compareTo(max) <= 0).forEach(result::add);
					i++;
				} else if (i == splited.length - 1 && i > 0) {
					T min = this.elements.get(splited[i - 1]);
					if(min == null) {
						throw new InvalidOptionException("cmd.general.nodef", splited[i - 1]);
					}
					
					this.elements.values().stream().filter((e) -> e.compareTo(min) >= 0).forEach(result::add);
				} else if (i > 0 && i < splited.length - 1) {
					T max = this.elements.get(splited[i + 1]);
					if(max == null) {
						throw new InvalidOptionException("cmd.general.nodef", splited[i + 1]);
					}
					
					T min = this.elements.get(splited[i - 1]);
					if(min == null) {
						throw new InvalidOptionException("cmd.general.nodef", splited[i - 1]);
					}

					this.elements.values().stream()
					.filter((e) -> {
						return e.compareTo(min) >= 0 && e.compareTo(max) <= 0;
					})
					.forEach(result::add);
					i++;
				}
			} else {
				T val = this.elements.get(splited[i]);
				if(val == null) {
					throw new InvalidOptionException("cmd.general.nodef", splited[i]);
				}
				
				result.add(val);
			}
		}
		
		return result;
	}
	
	public static final class ChunkStatusRange extends RangeParser<ChunkStatusRange.ChunkStatusSorter> {
		private static final ImmutableBiMap<String, ChunkStatusSorter> VANILLA_CHUNK_STATUSES;
		
		public ChunkStatusRange() {
			super(VANILLA_CHUNK_STATUSES);
		}

		public static class ChunkStatusSorter implements Comparable<ChunkStatusSorter> {
			public final ChunkStatus status;
			private final int ordinal;
			
			public ChunkStatusSorter(ChunkStatus status, int ordinal) {
				this.status = status;
				this.ordinal = ordinal;
			}
			
			@Override
			public int hashCode() {
				return this.status.hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null) {
					return false;
				} else {
					if(obj.getClass() == ChunkStatusSorter.class) {
						ChunkStatusSorter other = (ChunkStatusSorter) obj;
						return this.status.equals(other.status);
					} else {
						return false;
					}
				}
			}
			
			@Override
			public int compareTo(ChunkStatusSorter o) {
				return this.ordinal - o.ordinal;
			}
		}
		
		static {
			ImmutableBiMap.Builder<String, ChunkStatusSorter> builder = ImmutableBiMap.builder();
			Stream.of(ChunkStatus.class.getDeclaredFields())
					.filter((f) -> Modifier.isStatic(f.getModifiers()))
					.filter((f) -> f.getType().equals(ChunkStatus.class))
					.map((f) -> {
						try {
							return (ChunkStatus) f.get(null);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					})
					.filter((status) -> status != ChunkStatus.BIOMES && status != ChunkStatus.FULL)
					.map((status) -> new ChunkStatusSorter(status, status.getIndex()))
					.forEach((wrapped) -> builder.put(wrapped.status.getId(), wrapped));
			VANILLA_CHUNK_STATUSES = builder.build();
		}
	}
}
