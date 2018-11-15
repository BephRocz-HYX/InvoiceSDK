package hyx.invoice.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class SortMap {

	/**
	 * 让 Map按key进行排序
	 */
	public static Map<Double, String> sortMapByKey(Map<Double, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<Double, String> sortMap = new TreeMap<Double, String>(new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}
}

// 实现一个比较器类

class MapKeyComparator implements Comparator<Double> {

	@Override
	public int compare(Double s1, Double s2) {
		return s2.compareTo(s1); // 从大到小排序
	}
}
