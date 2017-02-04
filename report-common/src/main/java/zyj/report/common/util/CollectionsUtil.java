package zyj.report.common.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionsUtil {

	@SuppressWarnings("rawtypes")
	public static Map parserToMap(String s) {
		Map map = new HashMap();
		JSONObject json = JSONObject.fromObject(s);
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = json.get(key).toString();
			if (value.startsWith("{") && value.endsWith("}")) {
				map.put(key, parserToMap(value));
			} else {
				map.put(key, value);
			}

		}
		return map;
	}

	public static Map<String, Map<String, Object>> trans(List<Map<String, Object>> d, String[] key) {
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		if (d != null) {
			for (Map<String, Object> obj : d) {
				String k = "";
				for (String t : key) {
					k = k + ObjectUtils.toString(obj.get(t));
				}
				map.put(k, obj);
			}
		}
		return map;
	}

	public static List<Map<String, Object>> leftjoinMapByKey(List<Map<String, Object>> a, List<Map<String, Object>> b, String key) {
		Map<String, Map<String, Object>> aDic = trans(a, new String[]{key});
		Map<String, Map<String, Object>> bDic = trans(b, new String[]{key});
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		for (Map.Entry<String, Map<String, Object>> entry : aDic.entrySet()) {
			String k = entry.getKey();
			Map<String, Object> m = new HashMap<String, Object>(entry.getValue());
			try {
				Map<String, Object> n = new HashMap<String, Object>(bDic.get(k));
				m.putAll(n);
			} catch (Exception e) {
				System.out.println("Warn : b表中没有key为 " + k + "的字段");
			}
			res.add(m);
		}
		return res;
	}

	public static boolean containsKey(List<Map<String, Object>> l, String key, String value) {
		for (Map<String, Object> m : l) {
			if (m.containsKey(key)) {
				return m.get(key).equals(value);
			}
		}
		return false;
	}

	public static List<Map<String, Object>> groupby(List<Map<String, Object>> old, String[] keys, Map<String, Object> otherParams) {
		Map<String, Map<String, Object>> map = trans(old, keys);
		List<Map<String, Object>> new_map = (List) map.values();
		if (otherParams != null)
			for (Map m : new_map) {
				m.putAll(otherParams);
			}
		return new_map;
	}

	public static Map<String, List<Map<String, Object>>> partitionBy(List<Map<String, Object>> source, String[] keys) {
		if (source == null)
			return null;
		else {
			Map<String, List<Map<String, Object>>> mapping = new ConcurrentHashMap();

			source.parallelStream().forEach(m -> {
				StringBuffer sb = new StringBuffer();
				for (String k : keys) {
					sb.append(m.getOrDefault(k, ""));
				}
				final String newKey = sb.toString();
				mapping.putIfAbsent(newKey, new ArrayList<>());
				mapping.computeIfPresent(newKey, (k, list) -> {
					list.add(m);
					return list;
				});
			});
			return mapping;
		}


	}

	/**
	 * Map.get(key)的String值按指定String数组来排序
	 *
	 * @param d
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public static void orderBySpecifiedValue(List<Map<String, Object>> d, final String key, final Object[] value) {
		if (d != null) {
			Collections.sort(d, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Map<String, Object> m1 = (Map<String, Object>) o1;
					Map<String, Object> m2 = (Map<String, Object>) o2;
					Object k1 = m1.get(key);
					Object k2 = m2.get(key);
					int i1 = indexOf(value, k1);
					int i2 = indexOf(value, k2);
					return i1 - i2;
				}
			});
		}

	}

	/**
	 * 按Map.get(key)的String值来排序
	 *
	 * @param d
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public static void orderByStringValue(List<Map<String, Object>> d, final String key) {
		if (d != null) {
			Collections.sort(d, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Map<String, Object> m1 = (Map<String, Object>) o1;
					Map<String, Object> m2 = (Map<String, Object>) o2;
					String k1 = m1.get(key).toString();
					String k2 = m2.get(key).toString();
					return k1.compareTo(k2);
				}
			});
		}
	}

	/**
	 * 按Map.get(key)的String值来排序
	 *
	 * @param d
	 * @param keys
	 */
	@SuppressWarnings("unchecked")
	public static void orderByMultiStringValue(List<Map<String, Object>> d, final String[] keys) {
		if (d != null) {
			Collections.sort(d, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Map<String, Object> m1 = (Map<String, Object>) o1;
					Map<String, Object> m2 = (Map<String, Object>) o2;
					String k1 = "";
					String k2 = "";
					for (int i = 0; i < keys.length; i++) {
						k1 += m1.get(keys[i]).toString();
						k2 += m2.get(keys[i]).toString();

					}
					return k1.compareTo(k2);
				}
			});
		}
	}

	/**
	 * 按Map.get(key)的int值来排序
	 *
	 * @param d
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public static void orderByIntValue(List<Map<String, Object>> d, final String key) {
		if (d != null) {
			Collections.sort(d, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Map<String, Object> m1 = (Map<String, Object>) o1;
					Map<String, Object> m2 = (Map<String, Object>) o2;
					int k1 = Integer.parseInt(m1.get(key).toString());
					int k2 = Integer.parseInt(m2.get(key).toString());
					return k1 - k2;
				}
			});
		}
	}

	/**
	 * 按Map.get(key)的int值来倒序排序
	 *
	 * @param d
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public static void orderByIntValueDesc(List<Map<String, Object>> d, final String key) {
		if (d != null) {
			Collections.sort(d, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Map<String, Object> m1 = (Map<String, Object>) o1;
					Map<String, Object> m2 = (Map<String, Object>) o2;
					int k1 = Integer.parseInt(m1.get(key).toString());
					int k2 = Integer.parseInt(m2.get(key).toString());
					return k2 - k1;
				}
			});
		}
	}

	public static int indexOf(Object[] f, Object o) {
		if (o != null) {
			for (int i = 0; i < f.length; i++) {
				if (f[i].toString().equals(o.toString())) {
					return i;
				}
			}
		}
		return -1;
	}

	public static void rank(List<Map<String, Object>> d, final String key, final String rankKey) {

		orderByIntValueDesc(d, key);

		Integer previousValue = null;
		int rankValue = 0;
		int sameValue = 0;
		for (Map<String, Object> m : d) {
			Integer value = Integer.parseInt(m.get(key).toString());
			if (value != null && value.equals(previousValue)) {
				m.put(rankKey, rankValue);
				sameValue++;
			} else if (value != null && !value.equals(previousValue)) {
				rankValue = rankValue + sameValue + 1;
				m.put(rankKey, rankValue);
				sameValue = 0;
			}
			previousValue = value;
		}
	}
}
