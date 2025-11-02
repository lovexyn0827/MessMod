package lovexyn0827.mess.log.chunk;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.deobfuscating.Mapping;

public final class ChunkTaskPrintUtil {
	public static String printTask(long id, Object task) {
		if (!OptionManager.detailedChunkTaskLogging) {
			return Long.toString(id);
		}
		
		StringBuilder sb = new StringBuilder(256);
		Mapping map = MessMod.INSTANCE.getMapping();
		String taskClassName = task.getClass().getName();
		sb.append(map.namedClass(taskClassName));
		sb.append('#').append(id);
		sb.append('{');
		for (Field f : Reflection.getInstanceFields(task.getClass())) {
			sb.append(map.namedField(f.getName()));
			sb.append('=');
			f.setAccessible(true);
			try {
				Object val = f.get(task);
				String str;
				if (val == null) {
					str = "[null]";
				} else if (val.getClass().isSynthetic() || val instanceof CompletableFuture) {
					try {
						str = "[" + printTask(System.identityHashCode(val), val) + "]";
					} catch (Exception e) {
						str = val.toString().replace(',', ';');
						e.printStackTrace();
					}
					
				} else {
					str = val.toString().replace(',', ';');
				}
				
				sb.append(str);
			} catch (Exception e) {
				sb.append("?ERROR?");
				e.printStackTrace();
			}
			
			sb.append('|');
		}
		
		sb.deleteCharAt(sb.length() - 1);
		sb.append('}');
		return sb.toString();
	}
}
