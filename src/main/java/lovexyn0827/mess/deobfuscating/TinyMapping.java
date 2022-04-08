package lovexyn0827.mess.deobfuscating;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.fabricmc.mapping.reader.v2.MappingGetter;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.reader.v2.TinyV2Factory;
import net.fabricmc.mapping.reader.v2.TinyVisitor;

class TinyMapping implements Mapping {
	/**
	 * Srg <=> Named 
	 */
	private final BiMap<String, String> classes;
	/**
	 * Srg or named class => BiMap of members => (Srg <=> Named)
	 */
	private final Map<String, BiMap<String, String>> fieldsByClass;
	/**
	 * Srg => Named
	 */
	private final Map<String, String> fields;
	//private final Map<String, String> methods;
	
	public TinyMapping(File mappingFile) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(mappingFile)));
	}

	public TinyMapping(BufferedReader r) {
		this.classes = HashBiMap.create();
		this.fields = new HashMap<>();
		fieldsByClass = new HashMap<>();
		//this.methods = HashMap.create(32768);
		try(BufferedReader br = r) {
			TinyV2Factory.visit(br, new Reader());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String namedClass(String srg) {
		return this.classes.containsKey(srg) ? this.classes.get(srg) : srg;
	}
	
	@Override
	public String namedField(String srg) {
		return this.fields.containsKey(srg) ? this.fields.get(srg) : srg;
	}
	
	@Override
	public String srgClass(String named) {
		return this.classes.inverse().get(named);
	}

	@Override
	public String srgField(String clazz, String named) {
		return this.fieldsByClass.get(clazz).inverse().get(named);
	}
	
	/*@Override
	public String namedMethod(String srg) {
		return this.methods.containsKey(srg) ? this.methods.get(srg) : srg;
	}*/
	
	private class Reader implements TinyVisitor {
		private int namedIndex;
		private int srgIndex;
		private String currentClassSrg;
		
		@Override
		public void start(TinyMetadata metadata) {
			this.namedIndex = metadata.index("named");
			this.srgIndex = metadata.index("intermediary");
		}
		
		@Override
		public void pushClass(MappingGetter name) {
			String[] names = name.getAllNames();
			String srg = names[this.srgIndex].replace('/', '.');
			String named = names[this.namedIndex].replace('/', '.');
			TinyMapping.this.classes.put(srg, named);
			this.currentClassSrg = srg;
			TinyMapping.this.fieldsByClass.put(srg, HashBiMap.create());
			TinyMapping.this.fieldsByClass.put(named, HashBiMap.create());
		}
		
		@Override
		public void pushField(MappingGetter name, String descriptor) {
			String[] names = name.getAllNames();
			BiMap<String, String> map = TinyMapping.this.fieldsByClass.get(this.currentClassSrg);
			TinyMapping.this.fields.put(names[this.srgIndex], names[this.namedIndex]);
			map.put(names[this.srgIndex], names[this.namedIndex]);
		}
		
		/*@Override
		public void pushMethod(MappingGetter name, String descriptor) {
			String[] names = name.getAllNames();
			TinyMapping.this.methods.put(names[this.srgIndex], names[this.namedIndex]);
		}*/
	}
}
