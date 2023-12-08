package lovexyn0827.mess.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.brigadier.StringReader;

public final class NameFilter implements Predicate<String> {
	private final Predicate<String> nameFilter;
	
	private NameFilter(Predicate<String> nameFilter) {
		this.nameFilter = nameFilter;
	}

	@Override
	public boolean test(String s) {
		return this.nameFilter.test(s);
	}
	
	public <T extends Enum<?>> Set<T> filter(Class<? extends T> clazz) {
		Set<T> filtered = new HashSet<>();
		for(T e : clazz.getEnumConstants()) {
			if(this.test(e.name())) {
				filtered.add(e);
			}
		}
		
		return filtered;
	}
	
	public <T> Set<T> filter(Iterable<T> c) {
		Set<T> filtered = new HashSet<>();
		for(T e : c) {
			if(this.test(e.toString())) {
				filtered.add(e);
			}
		}
		
		return filtered;
	}
	
	public <T> Set<T> filter(Iterable<T> c, Function<T, String> toString) {
		Set<T> filtered = new HashSet<>();
		for(T e : c) {
			if(this.test(toString.apply(e))) {
				filtered.add(e);
			}
		}
		
		return filtered;
	}
	
	public <K, V> Map<K, V> filterByKey(Map<K, V> m, Function<K, String> toString) {
		Map<K, V> filtered = new HashMap<>();
		for(Map.Entry<K, V> e : m.entrySet()) {
			if(this.test(toString.apply(e.getKey()))) {
				filtered.put(e.getKey(), e.getValue());
			}
		}
		
		return filtered;
	}
	
	public static NameFilter compile(String in) {
		if("*".equals(in)) {
			return new NameFilter((n) -> true);
		}
		
		if(!in.contains("*") && !in.contains("?")) {
			return new NameFilter(in::equals);
		}
		
		StringReader sr = new StringReader(in);
		List<Node> nodes = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		while(sr.canRead()) {
			char c = sr.read();
			switch(c) {
			case '*':
				if(sb.length() != 0) {
					nodes.add(new TextNode(sb.toString()));
					sb = new StringBuilder();
				}
				
				nodes.add(AsteriskNode.INSTANCE);
				continue;
			case '?':
				if(sb.length() != 0) {
					nodes.add(new TextNode(sb.toString()));
					sb = new StringBuilder();
				}
				
				// get the length of the sequence of '?'
				int count;
				for(count = 1; sr.canRead() && sr.peek() == '?'; sr.skip(), count++);
				nodes.add(new QmNode(count));
				continue;
			}
			
			sb.append(c);
		}
		
		if(sb.length() != 0) {
			nodes.add(new TextNode(sb.toString()));
		}
		
		return new NameFilter((s) -> {
			ListIterator<Node> itr = nodes.listIterator();
			Node n;
			int l = s.length();
			int i = 0;
			while(itr.hasNext()) {
				n = itr.next();
				checkNode:
				switch(n.type()) {
				case ASTERISK:
					if(itr.hasNext()) {
						n = itr.next();
						if(n.type() == NodeType.TEXT) {
							TextNode tn = (TextNode) n;
							char start = tn.text.charAt(0);
							// cursor is at the first character of the sequence denoted by '*'
							// then read until the starting character
							for(; i < l && s.charAt(i) != start; i++);
							// cursor moved to the first character of the first suspected sequence 
							// or exactly after the end of input
							if(i == l) {
								return false;
							}
							
							// cursor is at the first character of the first suspected sequence
							while(i + tn.length - 1 < l) {
								if(s.regionMatches(i, tn.text, 0, tn.length)) {
									i += tn.length;
									break checkNode;
								}
								
								i++;
							}
							
							return false;
						} else {
							throw new IllegalStateException();
						}
					} else {
						return true;
					}
				case QM:
					QmNode qmn = (QmNode) n;
					if(i + qmn.length - 1 >= l) {
						return false;
					}
					
					i += qmn.length;
					break;
				case TEXT:
					TextNode tn = (TextNode) n;
					if(s.regionMatches(i, tn.text, 0, tn.length)) {
						i += tn.length;
						break checkNode;
					}
					
					return false;
				}
			}
			
			return true;
		});
	}
	
	private static interface Node {
		NodeType type();
	}
	
	private static enum AsteriskNode implements Node {
		INSTANCE;

		@Override
		public NodeType type() {
			return NodeType.ASTERISK;
		}
	}
	
	private static final class QmNode implements Node {
		protected final int length;
		
		protected QmNode(int length) {
			this.length = length;
		}

		@Override
		public NodeType type() {
			return NodeType.QM;
		}
	}
	
	private static final class TextNode implements Node {
		protected final int length;
		protected final String text;
		
		protected TextNode(String text) {
			this.length = text.length();
			this.text = text;
		}

		@Override
		public NodeType type() {
			return NodeType.TEXT;
		}
	}
	
	private static enum NodeType {
		ASTERISK, 
		QM, 
		TEXT;
	}
}
