package org.statnlp.example.math.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class ConceptNetCache {
	private Map<String, Boolean> cache;

	private static final String filename = "data/math/AddSub/c5cache.txt";
	private ConceptNetSearch helper = null;
	private static ConceptNetCache conceptNetCache = null;

	private ConceptNetCache(String filename) throws IOException {
		this.helper = new ConceptNetSearch();
		this.cache = new HashMap<>();
		File file = new File(filename);
		for (String line : FileUtils.readLines(file)) {
			String[] keyval = line.split("\t");
			this.cache.put(keyval[0], Boolean.parseBoolean(keyval[1]));
		}
	}

	public boolean isRelated(String start, String end, String rel) {
		String key = start + "@" + end + "@" + rel;
		// System.out.println(key);
		if (this.cache.containsKey(key)) {
			// System.out.println(this.cache.get(key));
			return this.cache.get(key);
		}
		try {
			JSONObject object = new JSONObject();
			object.put("start", "/c/en/" + start);
			object.put("end", "/c/en/" + end);
			object.put("rel", "/r/" + rel);
			object.put("limit", "1");
			boolean ret = this.helper.search(object);
			this.cache.put(key, ret);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void persist() throws IOException {
		List<String> list = new LinkedList<>();
		for (Entry<String, Boolean> entry : this.cache.entrySet()) {
			list.add(entry.getKey() + "\t" + entry.getValue());
		}

		FileUtils.writeLines(new File(filename), list);
	}

	public static ConceptNetCache getInstance() throws RuntimeException {
		if (conceptNetCache == null) {
			try {
				conceptNetCache = new ConceptNetCache(filename);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return conceptNetCache;
	}
}
