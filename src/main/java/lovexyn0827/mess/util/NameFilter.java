package lovexyn0827.mess.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
	
	public static NameFilter compile(String pattern) {
		// No actual compilation after rewriting
		return new NameFilter((s) -> matchWildcard(s, pattern));
	}
	
	// Reference: https://en.wikipedia.org/wiki/Matching_wildcards
	// Tested with: https://leetcode.com/problems/wildcard-matching/description/
	private static boolean matchWildcard(String s, String p) {
		int lenS = s.length();
		int lenP = p.length();
		boolean[][] matches = new boolean[lenS + 1][lenP + 1];
		matches[0][0] = true;
		for (int i = 1; i <= lenS; i++) {
			matches[i][0] = false;
		}

		for (int j = 1; j <= lenP; j++) {
			matches[0][j] = matches[0][j - 1] && p.charAt(j - 1) == '*';
		}

		for (int i = 1; i <= lenS; i++) {
			for (int j = 1; j <= lenP; j++) {
				char si = s.charAt(i - 1);
				char pj = p.charAt(j - 1);
				if (pj == '?' || pj == si) {
					matches[i][j] = matches[i - 1][j - 1];
				} else if (pj == '*') {
					matches[i][j] = matches[i - 1][j] || matches[i][j - 1];
				} else {
					matches[i][j] = false;
				}
			}
		}

		return matches[lenS][lenP];
	}
}
