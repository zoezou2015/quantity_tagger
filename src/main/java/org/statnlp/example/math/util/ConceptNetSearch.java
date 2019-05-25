package org.statnlp.example.math.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class ConceptNetSearch {
	// private final String searchURL =
	// "http://conceptnet5.media.mit.edu/data/5.4/search";
	private final String searchURL = "http://api.conceptnet.io/c/en/example";

	public boolean search(JSONObject param) throws JSONException, IOException {
		String urlStr = this.searchURL + "?";
		Iterator<?> keys = param.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			urlStr += key;
			urlStr += "=";
			urlStr += URLEncoder.encode(param.getString(key), "UTF-8");
			urlStr += "&";
		}
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		if (conn.getResponseCode() != 200)
			throw new IOException(conn.getResponseMessage());

		// Buffer the result into a string
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		conn.disconnect();

		JSONObject response = new JSONObject(sb.toString());
		if (response.has("numFound") && response.getInt("numFound") > 0)
			return true;
		System.out.println(response);
		return false;
	}

	public static void main(String[] args) throws IOException, JSONException {
		JSONObject object = new JSONObject();
		object.put("start", "/c/en/buy");
		object.put("end", "/c/en/pay");
		object.put("limit", "1");

		ConceptNetSearch cn = new ConceptNetSearch();
		cn.search(object);
		// boolean related = ConceptNetCache.getInstance().isRelated("go", "miss",
		// "RelatedTo");
		// System.out.println(related);
	}
}
