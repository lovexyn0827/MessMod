package lovexyn0827.mess.util;

import com.mojang.brigadier.StringReader;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

public class EntityDataDumpHelper {
	public static void tryDumpTarget(PlayerEntity player) {
		Entity e = RaycastUtil.getTargetEntity(player);
		if(e == null) {
			return;
		}
		
		ItemStack holding = player.getStackInHand(Hand.MAIN_HAND);
		if(OptionManager.dumpTargetEntitySummonCommand) {
			Text copyCmd = new LiteralText(I18N.translate("misc.copyentitycmd"))
					.styled((s) -> {
						String cmd = asCommand(e, holding.hasEnchantments());
						return s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(cmd)))
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, cmd))
								.withItalic(true)
								.withColor(Formatting.GREEN);
					});
			player.sendSystemMessage(copyCmd, Util.NIL_UUID);
		}
		
		if(OptionManager.dumpTargetEntityNbt) {
			Text data;
			if(holding.hasCustomName()) {
				String pathStr = holding.getName().getString();
				if(holding.hasEnchantments()) {
					try {
						AccessingPath path = AccessingPathArgumentType.accessingPathArg()
								.parse(new StringReader(pathStr));
						Object ob = path.access(e, e.getClass());
						data = new LiteralText(ob == null ? "null" : ob.toString());
					} catch (Exception e1) {
						data = e.toTag(new CompoundTag()).toText();
					}
				} else {
					try {
						NbtPathArgumentType.NbtPath path = NbtPathArgumentType.nbtPath()
								.parse(new StringReader(pathStr));
						data = path.get(e.toTag(new CompoundTag())).get(0).toText();
					} catch (Exception e1) {
						data = e.toTag(new CompoundTag()).toText();
					}
				}
			} else {
				data = e.toTag(new CompoundTag()).toText();
			}

			player.sendSystemMessage(data, Util.NIL_UUID);
		}
	}

	private static String asCommand(Entity e, boolean fullTags) {
		CompoundTag tag;
		if(fullTags) {
			tag = e.toTag(new CompoundTag());
			removeUuid(tag);
		} else {
			tag = new CompoundTag();
			Vec3d motion = e.getVelocity();
			ListTag motionTag = new ListTag();
			motionTag.add(DoubleTag.of(motion.x));
			motionTag.add(DoubleTag.of(motion.y));
			motionTag.add(DoubleTag.of(motion.z));
			tag.put("Motion", motionTag);
			ListTag rotationTag = new ListTag();
			rotationTag.add(FloatTag.of(e.yaw));
			rotationTag.add(FloatTag.of(e.pitch));
			tag.put("Rotation", rotationTag);
		}
		
		return String.format("/execute in %s run summon %s %s %s %s %s", 
				e.getEntityWorld().getRegistryKey().getValue(), 
				EntityType.getId(e.getType()), 
				Double.toString(e.getX()), Double.toString(e.getY()), Double.toString(e.getZ()), 
				tag.asString());
	}

	private static void removeUuid(CompoundTag tag) {
		tag.remove("UUID");
		for(Tag passenger : tag.getList("Passengers", 10)) {
			removeUuid((CompoundTag) passenger);
		}
	}
}
