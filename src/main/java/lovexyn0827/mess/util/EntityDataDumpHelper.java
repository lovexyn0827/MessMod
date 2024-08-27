package lovexyn0827.mess.util;

import com.mojang.brigadier.StringReader;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class EntityDataDumpHelper {
	public static void tryDumpTarget(PlayerEntity player) {
		Entity e = RaycastUtil.getTargetEntity(player);
		if(e == null) {
			return;
		}
		
		ItemStack holding = player.getStackInHand(Hand.MAIN_HAND);
		if(OptionManager.dumpTargetEntitySummonCommand) {
			Text copyCmd = new FormattedText("misc.copyentitycmd", "aon", true)
					.asMutableText()
					.styled((s) -> {
						String cmd = asCommand(e, holding.hasEnchantments());
						return s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(cmd)))
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, cmd));
					});
			player.sendMessage(copyCmd, false);
		}
		
		if(OptionManager.dumpTargetEntityNbt) {
			Text data;
			if(holding.get(DataComponentTypes.CUSTOM_NAME) != null) {	// XXX
				String pathStr = holding.getName().getString();
				if(holding.hasEnchantments()) {
					try {
						AccessingPath path = AccessingPathArgumentType.accessingPathArg()
								.parse(new StringReader(pathStr));
						Object ob = path.access(e, e.getClass());
						data = Text.literal(ob == null ? "null" : ob.toString());
					} catch (Exception e1) {
						data = NbtHelper.toPrettyPrintedText(e.writeNbt(new NbtCompound()));
					}
				} else {
					try {
						NbtPathArgumentType.NbtPath path = NbtPathArgumentType.nbtPath()
								.parse(new StringReader(pathStr));
						data = NbtHelper.toPrettyPrintedText(path.get(e.writeNbt(new NbtCompound())).get(0));
					} catch (Exception e1) {
						data = NbtHelper.toPrettyPrintedText(e.writeNbt(new NbtCompound()));
					}
				}
			} else {
				data = NbtHelper.toPrettyPrintedText(e.writeNbt(new NbtCompound()));
			}

			player.sendMessage(data, false);
		}
	}

	private static String asCommand(Entity e, boolean fullTags) {
		NbtCompound tag;
		if(fullTags) {
			tag = e.writeNbt(new NbtCompound());
			removeUuid(tag);
		} else {
			tag = new NbtCompound();
			Vec3d motion = e.getVelocity();
			NbtList motionTag = new NbtList();
			motionTag.add(NbtDouble.of(motion.x));
			motionTag.add(NbtDouble.of(motion.y));
			motionTag.add(NbtDouble.of(motion.z));
			tag.put("Motion", motionTag);
			NbtList rotationTag = new NbtList();
			rotationTag.add(NbtFloat.of(e.getYaw()));
			rotationTag.add(NbtFloat.of(e.getPitch()));
			tag.put("Rotation", rotationTag);
		}
		
		return String.format("/execute in %s run summon %s %s %s %s %s", 
				e.getEntityWorld().getRegistryKey().getValue(), 
				EntityType.getId(e.getType()), 
				Double.toString(e.getX()), Double.toString(e.getY()), Double.toString(e.getZ()), 
				tag.asString());
	}

	private static void removeUuid(NbtCompound tag) {
		tag.remove("UUID");
		for(NbtElement passenger : tag.getList("Passengers", 10)) {
			removeUuid((NbtCompound) passenger);
		}
	}
}
