package org.crimsoncrips.craftorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtil {

	public static <T> Map<T, Long> countOccurrences(List<T> words) {
		return words.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public static <T> Map<T, Long> countRepeats(List<T> words) {
		return countOccurrences(words).entrySet().stream().filter(e -> e.getValue() > 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public static <T> List<T> getExclusions(List<T> expected, List<T> given) {
		ArrayList<T> excluded = new ArrayList<>(given);
		excluded.removeAll(expected);
		return excluded;
	}

}