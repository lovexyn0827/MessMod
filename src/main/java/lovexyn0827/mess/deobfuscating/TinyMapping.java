package lovexyn0827.mess.deobfuscating;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.mapping.reader.v2.MappingGetter;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.reader.v2.TinyV2Factory;
import net.fabricmc.mapping.reader.v2.TinyVisitor;

public class TinyMapping implements Mapping {
	private final Map<String, String> classes;
	private final Map<String, String> fields;
	//private final Map<String, String> methods;
	
	public TinyMapping(File mappingFile) {
		this.classes = new HashMap<>();
		this.fields = new HashMap<>();
		//this.methods = HashMap.create(32768);
		try(BufferedReader br = new BufferedReader(new FileReader(mappingFile))) {
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
	
	/*@Override
	public String namedMethod(String srg) {
		return this.methods.containsKey(srg) ? this.methods.get(srg) : srg;
	}*/
	
	private class Reader implements TinyVisitor {
		private int namedIndex;
		private int srgIndex;
		
		@Override
		public void start(TinyMetadata metadata) {
			this.namedIndex = metadata.index("named");
			this.srgIndex = metadata.index("intermediary");
		}
		
		@Override
		public void pushClass(MappingGetter name) {
			String[] names = name.getAllNames();
			TinyMapping.this.classes.put(names[this.srgIndex].replace('/', '.'), names[this.namedIndex].replace('/', '.'));
		}
		
		@Override
		public void pushField(MappingGetter name, String descriptor) {
			String[] names = name.getAllNames();
			TinyMapping.this.fields.put(names[this.srgIndex], names[this.namedIndex]);
		}
		
		/*@Override
		public void pushMethod(MappingGetter name, String descriptor) {
			String[] names = name.getAllNames();
			TinyMapping.this.methods.put(names[this.srgIndex], names[this.namedIndex]);
		}*/
	}
}
