package lovexyn0827.mess;

import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class TestMain {
	public static Map<String, Integer> getScoreMapFromStdin(int n) {
		Map<String, Integer> scores = new TreeMap<String, Integer>();
		Scanner s = new Scanner(System.in);
		for (int i = 0; i < n; i++) {
			scores.put(s.next(), s.nextInt());
		}
		
		s.close();
		return scores;
	}
	
	public static void filter(Map<String, Integer> scores) {
		Iterator<Integer> itr = scores.values().iterator();
		for (; itr.hasNext();) {
			if (itr.next() <= 85) {
				itr.remove();
			}
		}
	}
	
	public static void print(Map<String, Integer> scores) {
		for (Map.Entry<String, Integer> e : scores.entrySet()) {
			System.out.printf("%s: %d\n", e.getKey(), e.getValue());
		}
	}

	public static void main(String[] args) {
		Map<String, Integer> scores = getScoreMapFromStdin(5);
		filter(scores);
		print(scores);
	}
}
