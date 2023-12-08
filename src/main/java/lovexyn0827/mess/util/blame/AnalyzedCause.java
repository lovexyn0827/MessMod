package lovexyn0827.mess.util.blame;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.Reflection;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public class AnalyzedCause implements Cause {
	private final ImmutableSet<Clue> clues;
	
	AnalyzedCause(Set<Clue> clues) {
		List<Clue> l = new ArrayList<>(clues);
		Collections.<Clue>sort(l, (c1, c2) -> c1.category().compareTo(c2.category()));
		this.clues = ImmutableSet.copyOf(l);
	}

	public ImmutableSet<Clue> getClues() {
		return this.clues;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Clue c : this.clues) {
			sb.append(c.name()).append(',');
		}
		
		if(sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.clues);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		AnalyzedCause other = (AnalyzedCause) obj;
		return Objects.equals(this.clues, other.clues);
	}

	public static interface Clue {
		static final ImmutableMap<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> BUILTIN = Util.make(() -> {
			Map<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> temp = new HashMap<>();
			Reflection.ENTITY_TYPE_TO_CLASS.forEach((type, cl) -> {
				Set<Pair<Predicate<TraceElement>, Clue>> clues = temp.computeIfAbsent(cl, (k) -> new HashSet<>());
				clues.add(new Pair<Predicate<TraceElement>, Clue>((te) -> true, 
						new TypeC(EntityType.getId(type).getPath(), Category.ENTITY)));
			});
			Reflection.BLOCK_ENTITY_TYPE_TO_CLASS.forEach((type, cl) -> {
				Set<Pair<Predicate<TraceElement>, Clue>> clues = temp.computeIfAbsent(cl, (k) -> new HashSet<>());
				clues.add(new Pair<Predicate<TraceElement>, Clue>((te) -> true, 
						new TypeC(BlockEntityType.getId(type).getPath(), Category.BLOCK_ENTITY)));
			});
			EntityC.register(temp);
			BlockEntityC.register(temp);
			ChunkManagerC.register(temp);
			WorldC.register(temp);
			ServerWorldC.register(temp);
			return ImmutableMap.copyOf(temp);
		});
		
		String name();
		Category category();
		
		public static enum Category {
			ENTITY, 
			BLOCK_ENTITY, 
			CHUNK_MANAGER, 
			WORLD, 
			SERVER_WORLD, 
			GENERATION, 
			UNSPECIFIED;
		}
	}
	
	private static final class TypeC implements Clue {
		private final String type;
		private final Category category;

		TypeC(String type, Category category) {
			this.type = type;
			this.category = category;
		}

		@Override
		public String name() {
			return this.type;
		}

		@Override
		public Category category() {
			return this.category;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.category, this.type);
		}
		
		@Override
		public String toString() {
			return this.category + ": " + this.type;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj == null) {
				return false;
			}
			
			if (getClass() != obj.getClass()) {
				return false;
			}
			
			TypeC other = (TypeC) obj;
			return this.category == other.category && Objects.equals(this.type, other.type);
		}
	}
	
	private static enum EntityC implements Clue {
		TICK_ENTITY(Reflection.getMethodForInternalPropose(Entity.class, "tick", "method_5773")), 
		MOVE(Reflection.getMethodForInternalPropose(Entity.class, "move", "method_5784", 
				MovementType.class, Vec3d.class)), 
		COLLISION_DETECTION(Reflection.getMethodForInternalPropose(Entity.class, 
				"adjustMovementForCollisions", "method_17835", 
				Vec3d.class)), 
		BLOCK_COLLISION(Reflection.getMethodForInternalPropose(Entity.class, "checkBlockCollision", "method_5852"));
		
		private final Method sign;
		
		EntityC(Method sign) {
			this.sign = sign;
		}
		
		static void register(Map<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> temp) {
			Reflection.ENTITY_TYPE_TO_CLASS.values().forEach((cl) -> {
				Set<Pair<Predicate<TraceElement>, Clue>> clues = temp.computeIfAbsent(cl, (k) -> new HashSet<>());
				for(EntityC ec : values()) {
					clues.add(new Pair<Predicate<TraceElement>, Clue>(
							(te) -> te.mentionsConsideringInherance(ec.sign).isAtLeast(OptionManager.blameThreshold),
							ec));
				}
			});
		}

		@Override
		public Category category() {
			return Category.ENTITY;
		}
	}
	
	private static enum BlockEntityC implements Clue {
		TICK_BE;
		
		private static final Set<String> TICK_METHOD_NAMES = ImmutableSet.of("method_16896", "tick");
		
		static void register(Map<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> temp) {
			Reflection.BLOCK_ENTITY_TYPE_TO_CLASS.values().forEach((cl) -> {
				Set<Pair<Predicate<TraceElement>, Clue>> clues = temp.computeIfAbsent(cl, (k) -> new HashSet<>());
				for(BlockEntityC bec : values()) {
					clues.add(new Pair<Predicate<TraceElement>, Clue>(
							(te) -> TICK_METHOD_NAMES.contains(te.methodName), bec));
				}
			});
		}

		@Override
		public Category category() {
			return Category.BLOCK_ENTITY;
		}
	}
	
	private static enum ChunkManagerC implements Clue {
		TICK(Reflection.getMethodForInternalPropose(ServerChunkManager.class, "tick", "method_12127", 
				BooleanSupplier.class)), 
		UPDATE(Reflection.getMethodForInternalPropose(ServerChunkManager.class, "tick", "method_16155")), 
		GET_CHUNK(Reflection.getMethodForInternalPropose(ServerChunkManager.class, "getChunk", "method_12121", 
				int.class, int.class, ChunkStatus.class, boolean.class)), 
		GET_WORLD_CHUNK(Reflection.getMethodForInternalPropose(ServerChunkManager.class, "getWorldChunk", "method_21730", 
				int.class, int.class));

		private final Method sign;
		
		ChunkManagerC(Method sign) {
			this.sign = sign;
		}
		
		static void register(Map<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> temp) {
			Set<Pair<Predicate<TraceElement>, Clue>> clues = temp
					.computeIfAbsent(ServerChunkManager.class, (k) -> new HashSet<>());
			for(ChunkManagerC c : values()) {
				clues.add(new Pair<Predicate<TraceElement>, Clue>(
						(te) -> te.mentionsConsideringInherance(c.sign).isAtLeast(OptionManager.blameThreshold), c));
			}
		}

		@Override
		public Category category() {
			return Category.CHUNK_MANAGER;
		}
	}
	
	private static enum WorldC implements Clue {
		GET_ENTITY(Reflection.getMethodForInternalPropose(World.class, 
				"getEntitiesIncludingUngeneratedChunks", "method_21728", 
				Class.class, Box.class, Predicate.class)), 
		GET_BLOCK(Reflection.getMethodForInternalPropose(World.class, "getBlockState", "method_8320", 
				BlockPos.class));

		private final Method sign;

		WorldC(Method sign) {
			this.sign = sign;
		}

		static void register(Map<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> temp) {
			Set<Pair<Predicate<TraceElement>, Clue>> clues = temp.computeIfAbsent(World.class, (k) -> new HashSet<>());
			for(WorldC c : values()) {
				clues.add(new Pair<Predicate<TraceElement>, Clue>(
						(te) -> te.mentionsConsideringInherance(c.sign).isAtLeast(OptionManager.blameThreshold), c));
			}
		}

		@Override
		public Category category() {
			return Category.WORLD;
		}
	}
	
	private static enum ServerWorldC implements Clue {
		TICK(Reflection.getMethodForInternalPropose(ServerWorld.class, 
				"tick", "method_18765", BooleanSupplier.class)), 
		TICK_NTE_BLOCK(Reflection.getMethodForInternalPropose(ServerWorld.class, 
				"tickBlock", "method_14189", ScheduledTick.class)), 
		TICK_NTE_FLUID(Reflection.getMethodForInternalPropose(ServerWorld.class, 
				"tickFluid", "method_14171", ScheduledTick.class)), 
		BLOCK_EVENT(Reflection.getMethodForInternalPropose(ServerWorld.class, 
				"processBlockEvent", "method_14174", BlockEvent.class)), 
		TICK_CHUNK(Reflection.getMethodForInternalPropose(ServerWorld.class, 
				"tickChunk", "method_18203", WorldChunk.class, int.class));

		private final Method sign;
		
		ServerWorldC(Method sign) {
			this.sign = sign;
		}
		
		static void register(Map<Class<?>, Set<Pair<Predicate<TraceElement>, Clue>>> temp) {
			Set<Pair<Predicate<TraceElement>, Clue>> clues = temp
					.computeIfAbsent(ServerWorld.class, (k) -> new HashSet<>());
			for(ServerWorldC c : values()) {
				clues.add(new Pair<Predicate<TraceElement>, Clue>(
						(te) -> te.mentionsConsideringInherance(c.sign).isAtLeast(OptionManager.blameThreshold), c));
			}
		}

		@Override
		public Category category() {
			return Category.SERVER_WORLD;
		}
	}
}
