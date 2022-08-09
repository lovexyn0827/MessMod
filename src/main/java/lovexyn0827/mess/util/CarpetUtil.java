package lovexyn0827.mess.util;

import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;

import lovexyn0827.mess.MessMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Lazy;

/**
 * @author lovexyn0827
 * Date: April 10, 2022
 */
@SuppressWarnings("deprecation")
public class CarpetUtil {
	private static final Lazy<BooleanSupplier> IS_TICK_FROZEN = new Lazy<>(() -> {
		if(FabricLoader.getInstance().isModLoaded("carpet")) {
			try {
				Field f = Class.forName("carpet.helpers.TickSpeed").getField("process_entities");
				return () -> {
					try {
						return !f.getBoolean(null);
					} catch (Exception e) {
						MessMod.LOGGER.error("An error occured in getting the process_entities, "
								+ "but the carpet is installed, something may work wrongly later.");
						e.printStackTrace();
						return false;
					}
				};
			} catch (ClassNotFoundException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
				return () -> false;
			}
		} else {
			return () -> false;
		}
	});
	
	public static boolean isTickFrozen() {
		return IS_TICK_FROZEN.get().getAsBoolean();
	}
}
