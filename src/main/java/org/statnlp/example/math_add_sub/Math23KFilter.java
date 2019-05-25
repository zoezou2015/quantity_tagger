package org.statnlp.example.math_add_sub;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Math23KFilter {
	// public static void main(String[] args) throws IOException, JSONException {
	// String file = "data/math/Math_23K.json";
	// String writeFile = "data/math/Math23K_AddSub.json";
	// FilterInstance(file, writeFile);
	// }

	/**
	 * Filter out invalid instance, in this case, we only consider the addition &
	 * subtraction
	 */
	public static void FilterInstance(String file, String writeFile) throws IOException, JSONException {

		ArrayList<AddSubInstance> instances = new ArrayList<>();
		@SuppressWarnings("deprecation")
		String jsonString = FileUtils.readFileToString(new File(file), "UTF-8");
		JSONArray testProblems = new JSONArray(jsonString);
		// System.out.println(testProblems.length());

		int count = 0;
		ArrayList<JSONObject> newObjects = new ArrayList<>();
		for (int i = 0; i < testProblems.length(); i++) {
			JSONObject test = testProblems.getJSONObject(i);

			int id = test.getInt("id");
			String original_text = test.getString("original_text");
			String segmented_text = test.getString("segmented_text");
			String equation = test.getString("equation");
			String ans = test.getString("ans");

			try {
				equation = equation.replaceAll("=", " = ");
				equation = equation.replaceAll("[+]", " + ");
			} catch (Exception e) {
			}
			try {
				equation = equation.replaceAll("[-]", " - ");
			} catch (Exception e) {
			}

			int[] filtered = new int[] { 21989, 3212, 1053, 2054, 2547, 3554, 4104, 4110, 4450, 4502, 4506, 4510, 5101,
					6448, 6518, 6551, 7642, 8372, 8897, 11043, 11325, 11426, 11731, 12103, 13864, 14175, 14397, 14696,
					14807, 14924, 16950, 16977, 17759, 17875, 18759, 18772, 20721, 23063, 23137, };

			if (FilterOutAddSubOnly(equation, ans) && TextMatchEquations(segmented_text, equation)) {
				if (checkInArray(id, filtered)) {
					// System.out.println("id : " + id);
					continue;
				}
				// System.out.println("id : " + id);
				// System.out.println("original_text " + original_text);
				// System.out.println("segmented_text" + segmented_text);
				// System.out.println("equation " + equation);
				// System.out.println("ans" + ans);
				count++;
				JSONObject obj = new JSONObject();
				obj.put("iIndex", count);
				obj.put("orig_id", id);
				obj.put("original_text", original_text);
				obj.put("sQuestion", segmented_text);
				obj.put("lEquations", equation);
				obj.put("lSolutions", ans);
				newObjects.add(obj);
			}
		}
		FileWriter fw = new FileWriter(writeFile);
		for (int j = 0; j < newObjects.size(); j++) {
			JSONObject new_obj = newObjects.get(j);
			// With four indent spaces
			fw.write(new_obj.toString(4) + "\n");
		}
		fw.flush();
		fw.close();
		System.out.println("Read " + count + " instances..");

	}

	private static boolean FilterOutAddSubOnly(String equation, String ans) {
		if (equation.equals("")) {
			return false;
		}
		if (equation.matches(".*[()*/%^].*")) {
			return false;
		}

		if (ans.matches(".*[()*/%^].*"))
			return false;

		return true;
	}

	private static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static boolean TextMatchEquations(String text, String equation) {
		String[] text_tokens = text.split(" ");
		String[] eq_tokens = equation.split(" ");
		int t_c = 0, e_c = 0;

		for (String t : text_tokens) {
			if (isNumeric(t))
				t_c++;
		}
		Set eq_number = new HashSet<>();
		for (String e : eq_tokens) {
			if (isNumeric(e)) {
				e_c++;
				eq_number.add(e);
			}
		}
		// the number from equation does not appear in text
		if (e_c > t_c)
			return false;
		// the number from equation used twice
		else if (eq_number.size() < e_c)
			return false;
		else
			return true;
	}

	private static boolean checkInArray(int currentState, int[] myArray) {
		boolean found = false;

		for (int i = 0; !found && (i < myArray.length); i++) {
			found = (myArray[i] == currentState);
		}
		return found;
	}

}
