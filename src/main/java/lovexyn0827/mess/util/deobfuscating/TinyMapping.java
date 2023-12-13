package lovexyn0827.mess.util.deobfuscating;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

class TinyMapping implements Mapping {
	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Srg <=> Named 
	 */
	private final BiMap<String, String> classes;
	/**
	 * Srg class => BiMap of members => (Srg <=> Named)
	 */
	private final Map<String, BiMap<String, String>> fieldsByClass;
	/**
	 * Srg => Named
	 */
	private final Map<String, String> fields;
	private final Map<String, String> methods;
	/**
	 * Srg or named class => BiMap of members => (Srg <=> Named)
	 */
	private final Map<String, Set<MethodInfo>> methodsByClass;
	
	public TinyMapping(File mappingFile) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(mappingFile)));
	}

	public TinyMapping(BufferedReader r) {
		this.classes = HashBiMap.create();
		this.fields = new HashMap<>();
		this.fieldsByClass = new HashMap<>();
		this.methods = new HashMap<>();
		this.methodsByClass = new HashMap<>();
		try(BufferedReader br = r) {
			TinyMappingParser.visit(br, new Reader());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String namedClass(String srg) {
		return this.classes.getOrDefault(srg, srg);
	}
	
	@Override
	@Nullable
	public String srgClass(String named) {
		return this.classes.inverse().getOrDefault(named, named);
	}
	
	@Override
	public String namedField(String srg) {
		return this.fields.getOrDefault(srg, srg);
	}

	/**
	 * @param clazz The srg name of the class
	 * @return The srg name of the given field, or {@code null} if the mapping doesn't contain it.
	 */
	@Override
	@Nullable
	public String srgField(String clazz, String named) {
		BiMap<String, String> classMap = this.fieldsByClass.get(clazz);
		if(classMap == null) {
			return null;
		}
		
		return classMap.inverse().getOrDefault(named, named);
	}

	@Override
	public String namedMethod(String srg, String desc) {
		return this.methods.getOrDefault(srg, srg);
	}

	/**
	 * @param clazz The srg name of the class
	 */
	@Override
	@Nullable
	public String srgMethod(String clazz, String named, String desc) {
		Set<MethodInfo> classMap = this.methodsByClass.get(clazz);
		if(classMap != null) {
			return classMap.stream()
					.filter((m) -> m.name.equals(named) && m.descriptor.equals(desc))
					.map((m) -> m.srgName)
					.findFirst()
					.orElse(null);
		} else {
			return null;
		}
	}

	@Override
	public boolean isClassMapped(Class<?> clazz) {
		return this.classes.containsKey(clazz.getCanonicalName());
	}
	
	private class Reader {
		private int namedIndex;
		private int srgIndex;
		private String currentClassSrg;
		
		public void start(MessTinyMetadata metadata) {
			this.namedIndex = metadata.index("named");
			this.srgIndex = metadata.index("intermediary");
		}
		
		public void pushClass(TinyMappingParser.MessMappingGetter name) {
			String[] names = name.getAllNames();
			String srg = names[this.srgIndex].replace('/', '.');
			String named = names[this.namedIndex].replace('/', '.');
			TinyMapping.this.classes.put(srg, named);
			this.currentClassSrg = srg;
			TinyMapping.this.fieldsByClass.put(srg, HashBiMap.create());
			TinyMapping.this.methodsByClass.put(srg, new HashSet<>());
		}
		
		public void pushField(TinyMappingParser.MessMappingGetter name, String descriptor) {
			String[] names = name.getAllNames();
			BiMap<String, String> map = TinyMapping.this.fieldsByClass.get(this.currentClassSrg);
			TinyMapping.this.fields.put(names[this.srgIndex], names[this.namedIndex]);
			map.put(names[this.srgIndex], names[this.namedIndex]);
		}
		
		public void pushMethod(TinyMappingParser.MessMappingGetter name, String descriptor) {
			String[] names = name.getAllNames();
			TinyMapping.this.methodsByClass.get(this.currentClassSrg)
					.add(new MethodInfo(names[this.srgIndex], names[this.namedIndex], descriptor));
			TinyMapping.this.methods.put(names[this.srgIndex], names[this.namedIndex]);
		}
	}
	
	// We should have our own implementation as Tiny Mapping Parser is no longer available in Fabric 0.15+
	private static final class TinyMappingParser {
		private static final String[] NAME_CACHE = new String[16];
		
		public static void visit(BufferedReader br, Reader reader) throws IOException {
			// 1. Validate
			String metaLine = br.readLine();
			String[] metaSplited = metaLine.split("\t");
			if(metaSplited.length < 5 || !metaSplited[0].equals("tiny")) {
				LOGGER.warn("Trying to read non-tiny mapping, which is not supported!");
				throw new RuntimeException("Trying to read non-tiny mapping, which is not supported!");
			}
			
			// 2. Validate major version
			if(Integer.parseInt(metaSplited[1]) != 2) {
				LOGGER.warn("Unsupported maapping version: {}", Integer.parseInt(metaSplited[2]));
				throw new RuntimeException(String.format("Unsupported maapping version: %s", metaSplited[2]));
			}
			
			// 3. Read namespaces
			List<String> namespaces = new ArrayList<>();
			for(int i = 3; i < metaSplited.length; i++) {
				namespaces.add(metaSplited[i]);
			}
			
			reader.start(new MessTinyMetadata(namespaces));
			
			// 4. Parse entries
			String line;
			while((line = br.readLine()) != null) {
				String pure = line.trim();
				String parts[];
				switch(pure.charAt(0)) {
				case 'c':
					if(line.charAt(0) == '\t') {
						// Skip comment entries
						break;
					}
					
					parts = pure.split("\t");
					for(int i = 1; i < parts.length; i++) {
						NAME_CACHE[i - 1] = parts[i];
					}
					
					reader.pushClass(MessMappingGetter.INSTANCE);
					break;
				case 'f':
					parts = pure.split("\t");
					for(int i = 2; i < parts.length; i++) {
						NAME_CACHE[i - 2] = parts[i];
					}
					
					reader.pushField(MessMappingGetter.INSTANCE, parts[1]);
					break;
				case 'm':
					parts = pure.split("\t");
					for(int i = 2; i < parts.length; i++) {
						NAME_CACHE[i - 2] = parts[i];
					}
					
					reader.pushMethod(MessMappingGetter.INSTANCE, parts[1]);
					break;
				}
			}
		}
		
		private static final class MessMappingGetter {
			public static final MessMappingGetter INSTANCE = new MessMappingGetter();
			
			public String[] getAllNames() {
				return NAME_CACHE;
			}
		}
	}
	
	private static final class MessTinyMetadata {
		private final List<String> namespaces;

		private MessTinyMetadata(List<String> namespaces) {
			this.namespaces = namespaces;
		}

		public int index(String string) {
			return this.namespaces.indexOf(string);
		}
	}
	
	
}
