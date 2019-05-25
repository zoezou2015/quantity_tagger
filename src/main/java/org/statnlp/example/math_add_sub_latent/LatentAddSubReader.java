package org.statnlp.example.math_add_sub_latent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.statnlp.commons.types.Sentence;
import org.statnlp.commons.types.WordToken;
import org.statnlp.example.math.features.ChangeSampleFeature;
import org.statnlp.example.math.features.DifferenceSampleFeature;
import org.statnlp.example.math.features.DifferenceSampleFeatureChinese;
import org.statnlp.example.math.features.ExtractSentenceFeatures;
import org.statnlp.example.math.features.ExtractSentenceFeaturesChinese;
import org.statnlp.example.math.features.WholeSampleFeature;
import org.statnlp.example.math.features.WholeSampleFeatureChinese;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;
import org.statnlp.example.math.util.MathUtility;

public class LatentAddSubReader {
	public static void main(String[] args) throws IOException, JSONException {
		ExtractSentenceFeatures esf = new ExtractSentenceFeatures();
		String file = "data/math/AddSub/AddSub.json";
		String train_ids = "data/math/AddSub/dev_train2.txt";
		String test_ids = "data/math/AddSub/dev_train2.txt";
		String train_savePath = "data/math/AddSub/dev_latent_train_data2.ser";
		String test_savePath = "data/math/AddSub/dev_latent_test_data2.ser";
		for (int i = 0; i < 3; i++) {
			train_ids = "data/math/AddSub/dev_train" + i + ".txt";
			test_ids = "data/math/AddSub/dev_test" + i + ".txt";
			train_savePath = "data/math/AddSub/dev_latent_train_data" + i + ".ser";
			test_savePath = "data/math/AddSub/dev_latent_test_data" + i + ".ser";
			InstanceReader(esf, file, train_ids, train_savePath, true);
			InstanceReader(esf, file, test_ids, test_savePath, false);
		}
	}

	public static LatentAddSubInstance[] InstanceReader(String filePath) {
		ObjectInputStream objectinputstream = null;
		ArrayList<LatentAddSubInstance> instances = null;
		try {
			objectinputstream = new ObjectInputStream(new FileInputStream(filePath));
			instances = (ArrayList<LatentAddSubInstance>) objectinputstream.readObject();
		} catch (Exception e) {
		}
		System.out.println("Read " + instances.size() + " instances..");
		return instances.toArray(new LatentAddSubInstance[instances.size()]);
	}

	public static void InstanceReader(ExtractSentenceFeatures esf, String file, String ids_filename, String savePath,
			boolean isTraining) throws IOException, JSONException {
		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(ids_filename), "UTF8"));
		ArrayList<Integer> ids = new ArrayList<>();
		String line;
		while ((line = scan.readLine()) != null) {
			int id = Integer.parseInt(line);
			ids.add(id);
		}
		scan.close();
		String pos_file = "data/math/AddSub/AddSub-tagged.txt";
		HashMap<Integer, String> id2sent_tag = readPosTagFile(pos_file, "en");
		ArrayList<LatentAddSubInstance> instances = new ArrayList<>();
		@SuppressWarnings("deprecation")
		String jsonString = FileUtils.readFileToString(new File(file));
		JSONArray testProblems = new JSONArray(jsonString);
		// System.out.println(testProblems.length());

		// duplicate numbers in text, but only part of them are used in equation
		ArrayList<Integer> mis_instances = new ArrayList<>();
		// dupliate numbers in equation
		ArrayList<Integer> duplicate_instances = new ArrayList<>();

		for (int i = 0; i < testProblems.length(); i++) {
			JSONObject test = testProblems.getJSONObject(i);
			ArrayList<String> numbers = new ArrayList<>();
			ArrayList<String> signs = new ArrayList<>();
			ArrayList<Integer> numberPos = new ArrayList<>();
			HashMap<String, Integer> num2pos = new HashMap<>();
			HashMap<String, String> num2sign = new HashMap<>();

			int id = test.getInt("iIndex");
			if (!ids.contains(id))
				continue;
			String lEquations = ((JSONArray) test.get("lEquations")).get(0).toString();
			String lSigns = "";
			double lSolutions = Double.parseDouble(((JSONArray) test.get("lSolutions")).get(0).toString());
			String sQuestion = test.getString("sQuestion");
			try {
				lSigns = ((JSONArray) test.get("lSigns")).get(0).toString();
			} catch (Exception e) {
			}

			ProblemRepresentation probRep = new ProblemRepresentation(id, sQuestion.trim(), lEquations.trim(),
					lSolutions);
			/*
			 * Run pre-processing tasks including tokenization, lemmatization, sentence
			 * splitting, CFG parsing, dependency parsing, co-reference resolution
			 */
			esf.StructureTagger(probRep);
			/*
			 * detect the unknonwn(s) in the problem create a variable for it extracts and
			 * saves the feature that represents the meaning of the variable
			 *
			 */
			esf.FindUnknowns(probRep);

			String sent_tag = id2sent_tag.get(id);
			String[] word_tags = sent_tag.trim().split(" ");
			String[] words = sQuestion.trim().toLowerCase().split(" ");

			WordToken[] wTokens = new WordToken[words.length];
			for (int k = 0; k < words.length; k++) {
				int index = word_tags[k].indexOf("_");
				String tag = word_tags[k].substring(index + 1);
				wTokens[k] = new WordToken(words[k], tag);
			}
			// System.out.println(Arrays.toString(words));
			Sentence text = new Sentence(wTokens);
			int idx = Arrays.asList(words).indexOf("how");
			if (idx < 0) {
				// System.out.println(id);
				idx = Arrays.asList(words).indexOf("what");
				;
				if (idx < 0) {
					System.out.println(id);
				}
			}

			WordToken[] ques_tokens = Arrays.copyOfRange(wTokens, idx, wTokens.length);
			Sentence ques_part = new Sentence(ques_tokens);

			for (int k = 0; k < words.length; k++) {
				String word = words[k];
				if (MathUtility.isNumber(word)) {
					String num = word + "_" + k;
					numbers.add(num);
					signs.add(num);
					numberPos.add(k);
					if (num2pos.containsKey(num)) {
						throw new RuntimeException("each number should be unique!");
					}
					num2pos.put(num, k);
				}
				if (k == idx) {
					// System.out.println(k);
					numbers.add("X");
					num2pos.put("X", k);
					numberPos.add(k);
					signs.add("X");
					// break;
				}
			}

			// according to equation, assign sign for each number
			String[] equa_tokens = lEquations.trim().split(" ");
			ArrayList<String> equa_tokens_list = new ArrayList<>();
			for (int e = 0; e < equa_tokens.length; e++) {
				equa_tokens_list.add(equa_tokens[e]);
			}
			boolean duplicateNumber = checkDuplicateNumberInEquation(equa_tokens_list);
			if (duplicateNumber) {
				duplicate_instances.add(id);
			}

			int equal_pos = equa_tokens_list.indexOf("=");
			if (lSigns.equals("")) {
				// int equal_pos = Arrays.asList(equa_tokens).indexOf("=");
				// System.out.println(Arrays.toString(equa_tokens));
				// System.out.println("equal pos: " + equal_pos);
				for (int n = 0; n < numbers.size(); n++) {
					String number_k = numbers.get(n);
					String number = number_k.split("_")[0].trim();

					for (String num_in_equa : equa_tokens) {
						if (MathUtility.isNumber(num_in_equa) && MathUtility.isNumber(number)) {
							number = reFormatNumber(number, num_in_equa);
						}
					}

					// int number_pos_at_euq = Arrays.asList(equa_tokens).indexOf(number);
					int number_pos_at_euq = equa_tokens_list.indexOf(number);
					String sign_temp = addSign(equa_tokens, number_pos_at_euq, equal_pos);
					num2sign.put(number_k, sign_temp);
					signs.set(n, sign_temp);
				}
			} else {
				String[] annotatedSigns = lSigns.trim().split(" ");
				for (int n = 0; n < numbers.size(); n++) {
					String number_k = numbers.get(n);
					if (number_k.equals("X")) {
						// int number_pos_at_euq = Arrays.asList(equa_tokens).indexOf(number);
						int number_pos_at_euq = equa_tokens_list.indexOf(number_k);
						String sign_temp = addSign(equa_tokens, number_pos_at_euq, equal_pos);
						num2sign.put(number_k, sign_temp);
						signs.set(n, sign_temp);
					} else {
						num2sign.put(number_k, annotatedSigns[n]);
						signs.set(n, annotatedSigns[n]);
					}

				}
			}

			/**
			 * Check whether the number of quantities are equal
			 */
			if (probRep.getNumberOfQuantities() != signs.size()) {
				num2sign.remove(num2sign.get(num2sign.size() - 1));
				num2pos.remove(num2pos.get(num2pos.size() - 1));
				signs.remove(signs.get(signs.size() - 1));
				numberPos.remove(numberPos.get(numberPos.size() - 1));
				numbers.remove(numbers.get(numbers.size() - 1));
			}

			if (probRep.getNumberOfQuantities() != signs.size()) {
				System.out.println(probRep.getNumberOfQuantities());
				System.out.println(signs.size());
				System.out.println(sQuestion);
			}

			List<Quantity> quantities = probRep.getQuantities();
			int index = 0;
			boolean isXLast = (numbers.indexOf("X") == numbers.size() - 1);
			if (!isXLast) {
				Quantity temp = quantities.get(numbers.size() - 2);
				quantities.set(numbers.size() - 2, quantities.get(numbers.size() - 1));
				quantities.set(numbers.size() - 1, temp);
				probRep.setQuantities(quantities);
			}
			for (Quantity q : quantities) {
				String value = q.getStringValue();
				String number_idx = numbers.get(index).split("_")[0].trim();
				if (!value.equals(number_idx)) {
					System.out.println("q: " + value + " n: " + number_idx);
					throw new RuntimeException();
				}
				q.setSentencePos(numberPos.get(index));
				q.setSign(signs.get(index), isTraining);
				index++;
			}

			double solution = calculateSolution(num2sign);
			double difference = Math.abs(lSolutions - solution);
			if (difference > 10e-6) {
				System.out.println("gold: " + lSolutions);
				System.out.println("test: " + solution);
				mis_instances.add(id);
				throw new RuntimeException("solutions do not match");
			}
			LatentAddSubInstance instance = new LatentAddSubInstance(instances.size() + 1, 1.0, id, lEquations,
					lSolutions, text, num2pos, num2sign, numbers, signs, numberPos);
			if (!isTraining)
				esf.FindRelatedQuantity(probRep);
			instance.setQestionPart(ques_part);
			instance.setProblemRepresentation(probRep);
			instance.setQuantities(quantities);
			DifferenceSampleFeature.addFeatures(probRep);
			WholeSampleFeature.addFeatures(probRep);
			ChangeSampleFeature.addFeatures(probRep);
			if (isTraining)
				instance.setLabeled();
			else
				instance.setUnlabeled();
			instances.add(instance);
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savePath));
		oos.writeObject(instances);
		oos.close();
		System.out.println("Read " + instances.size() + " instances..");
	}

	public static LatentAddSubInstance[] InstanceReader_Math23K(String file, String ids_filename, boolean isTraining)
			throws IOException, JSONException {
		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(ids_filename), "UTF8"));
		ArrayList<Integer> ids = new ArrayList<>();
		String line;
		while ((line = scan.readLine()) != null) {
			int id = Integer.parseInt(line);
			ids.add(id);
		}
		scan.close();

		ArrayList<LatentAddSubInstance> instances = new ArrayList<>();
		@SuppressWarnings("deprecation")
		String jsonString = FileUtils.readFileToString(new File(file));
		JSONArray testProblems = new JSONArray(jsonString);
		// System.out.println(testProblems.length());

		// duplicate numbers in text, but only part of them are used in equation
		ArrayList<Integer> mis_instances = new ArrayList<>();
		// dupliate numbers in equation
		ArrayList<Integer> duplicate_instances = new ArrayList<>();

		for (int i = 0; i < testProblems.length(); i++) {
			JSONObject test = testProblems.getJSONObject(i);
			ArrayList<String> numbers = new ArrayList<>();
			ArrayList<String> signs = new ArrayList<>();
			ArrayList<Integer> numberPos = new ArrayList<>();
			HashMap<String, Integer> num2pos = new HashMap<>();
			HashMap<String, String> num2sign = new HashMap<>();

			int id = test.getInt("iIndex");
			if (!ids.contains(id))
				continue;

			String lEquations = test.getString("lEquations");
			String lSigns = "";
			double lSolutions = Double.parseDouble(test.getString("lSolutions"));
			String sQuestion = test.getString("sQuestion");
			try {
				lSigns = ((JSONArray) test.get("lSigns")).get(0).toString();
			} catch (Exception e) {
			}
			// if (!lSigns.equals("")) {
			// System.out.println("id : " + id);
			// System.out.println("id : " + lSigns);
			// System.exit(0);
			// }
			// System.out.println("id : " + id);
			// System.out.println("equation " + lEquations);
			// System.out.println("lSolutions" + lSolutions);
			// System.out.println("sQuestion " + sQuestion);

			String[] words = sQuestion.trim().toLowerCase().split(" ");

			WordToken[] wTokens = new WordToken[words.length];
			for (int k = 0; k < words.length; k++) {
				wTokens[k] = new WordToken(words[k]);
			}
			// System.out.println(Arrays.toString(words));
			Sentence text = new Sentence(wTokens);

			for (int k = 0; k < words.length; k++) {
				String word = words[k];
				if (isNumeric(word)) {
					String num = word + "_" + k;
					numbers.add(num);
					signs.add(num);
					numberPos.add(k);
					if (num2pos.containsKey(num)) {
						throw new RuntimeException("each number should be unique!");
					}
					num2pos.put(num, k);
				}
				if (k == words.length - 1) {
					// System.out.println(k);
					numbers.add("X");
					num2pos.put("X", k);
					numberPos.add(k);
					signs.add("X");
					// break;
				}
			}

			// according to equation, assign sign for each number
			String[] equa_tokens = lEquations.trim().split(" ");
			ArrayList<String> equa_tokens_list = new ArrayList<>();
			for (int e = 0; e < equa_tokens.length; e++) {
				equa_tokens_list.add(equa_tokens[e]);
			}
			boolean duplicateNumber = checkDuplicateNumberInEquation(equa_tokens_list);
			if (duplicateNumber) {
				duplicate_instances.add(id);
			}

			int equal_pos = equa_tokens_list.indexOf("=");
			if (lSigns.equals("")) {
				// int equal_pos = Arrays.asList(equa_tokens).indexOf("=");
				// System.out.println(Arrays.toString(equa_tokens));
				// System.out.println("equal pos: " + equal_pos);
				for (int n = 0; n < numbers.size(); n++) {
					String number_k = numbers.get(n);
					String number = number_k.split("_")[0].trim();

					for (String num_in_equa : equa_tokens) {
						if (isNumeric(num_in_equa) && isNumeric(number)) {
							number = reFormatNumber(number, num_in_equa);
						}
					}

					// int number_pos_at_euq = Arrays.asList(equa_tokens).indexOf(number);
					int number_pos_at_euq = equa_tokens_list.indexOf(number);
					String sign_temp = addSign(equa_tokens, number_pos_at_euq, equal_pos);
					num2sign.put(number_k, sign_temp);
					signs.set(n, sign_temp);
				}
			} else {
				String[] annotatedSigns = lSigns.trim().split(" ");
				for (int n = 0; n < numbers.size(); n++) {
					String number_k = numbers.get(n);
					if (number_k.equals("X")) {
						// int number_pos_at_euq = Arrays.asList(equa_tokens).indexOf(number);
						int number_pos_at_euq = equa_tokens_list.indexOf(number_k);
						String sign_temp = addSign(equa_tokens, number_pos_at_euq, equal_pos);
						num2sign.put(number_k, sign_temp);
						signs.set(n, sign_temp);
					} else {
						num2sign.put(number_k, annotatedSigns[n]);
						signs.set(n, annotatedSigns[n]);
					}

				}
			}

			double solution = calculateSolution(num2sign);
			double difference = Math.abs(lSolutions - solution);
			if (difference > 10e-6) {
				System.out.println("iIndex" + id);
				for (String number_k : numbers) {
					System.out.println(
							number_k + " " + text.get(num2pos.get(number_k)).getForm() + " " + num2sign.get(number_k));
				}
				System.out.println("gold: " + lSolutions);
				System.out.println("test: " + solution);
				mis_instances.add(id);
				// throw new RuntimeException("solutions do not match");
			}
			LatentAddSubInstance instance = new LatentAddSubInstance(instances.size() + 1, 1.0, id, lEquations,
					lSolutions, text, num2pos, num2sign, numbers, signs, numberPos);
			if (isTraining)
				instance.setLabeled();
			else
				instance.setUnlabeled();
			instances.add(instance);
		}
		// System.out.println("duplicate instances " + duplicate_instances.size());
		// System.out.println(duplicate_instances);
		System.out.println("Read " + instances.size() + " instances..");
		return instances.toArray(new LatentAddSubInstance[instances.size()]);

	}

	public static LatentAddSubInstance[] InstanceReader_Math23K(ExtractSentenceFeaturesChinese esf, String file,
			String ids_filename, String savePath, boolean isTraining) throws IOException, JSONException {

		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(ids_filename), "UTF8"));
		ArrayList<Integer> ids = new ArrayList<>();
		String line;
		while ((line = scan.readLine()) != null) {
			int id = Integer.parseInt(line);
			ids.add(id);
		}
		scan.close();

		// String writefile = "data/math/Math23K/input_list.txt";
		// BufferedWriter par_bw = new BufferedWriter(new FileWriter(writefile));
		// String writefile_individual;

		String pos_file = "data/math/Math23K/Math23K_AddSub_text-tagged.txt";
		HashMap<Integer, String> id2sent_tag = readPosTagFile(pos_file, "zh");

		ArrayList<LatentAddSubInstance> instances = new ArrayList<>();
		@SuppressWarnings("deprecation")
		String jsonString = FileUtils.readFileToString(new File(file));
		JSONArray testProblems = new JSONArray(jsonString);
		// System.out.println(testProblems.length());

		// duplicate numbers in text, but only part of them are used in equation
		ArrayList<Integer> mis_instances = new ArrayList<>();
		// dupliate numbers in equation
		ArrayList<Integer> duplicate_instances = new ArrayList<>();

		for (int i = 0; i < testProblems.length(); i++) {
			JSONObject test = testProblems.getJSONObject(i);
			ArrayList<String> numbers = new ArrayList<>();
			ArrayList<String> signs = new ArrayList<>();
			ArrayList<Integer> numberPos = new ArrayList<>();
			HashMap<String, Integer> num2pos = new HashMap<>();
			HashMap<String, String> num2sign = new HashMap<>();

			int id = test.getInt("iIndex");
			if (!ids.contains(id))
				continue;

			String lEquations = test.getString("lEquations");
			String lSigns = "";
			double lSolutions = Double.parseDouble(test.getString("lSolutions"));
			String sQuestion = test.getString("sQuestion");
			try {
				lSigns = ((JSONArray) test.get("lSigns")).get(0).toString();
			} catch (Exception e) {
			}
			// writefile_individual = "data/math/Math23K/input/Math23K-question-" + id + "";
			// par_bw.write("input/Math23K-question-" + id + "\n");
			// BufferedWriter bw = new BufferedWriter(new FileWriter(writefile_individual));
			// bw.write("Instance" + id + ".\n");
			// bw.write(sQuestion + "\n");
			// bw.close();

			String[] words = sQuestion.trim().split(" ");

			String sent_tag = id2sent_tag.get(id);
			String[] word_tags = sent_tag.toLowerCase().trim().split(" ");
			if (word_tags.length != words.length) {
				System.out.println("id: " + id);
				System.out.println(words.length + " " + sQuestion);
				System.out.println(word_tags.length + " " + sent_tag);
				for (int gg = 0; gg < words.length; gg++) {
					System.out.println(words[gg] + " " + word_tags[gg]);
				}
				throw new RuntimeException();
			}
			WordToken[] wTokens = new WordToken[words.length];
			for (int k = 0; k < words.length; k++) {
				// System.out.println(sQuestion.length());
				// System.out.println(sent_tag.length());
				// System.out.println(word_tags[k]);
				int index = word_tags[k].indexOf("#");
				String tag = word_tags[k].substring(index + 1);
				wTokens[k] = new WordToken(words[k], tag);
				// wTokens[k] = new WordToken(words[k]);
			}
			// System.out.println(Arrays.toString(words));
			Sentence text = new Sentence(wTokens);
			String[] words_low = sQuestion.trim().toLowerCase().split(" ");

			for (int k = 0; k < words.length; k++) {
				String word = words[k];
				int index = word_tags[k].indexOf("#");
				String pos = word_tags[k].substring(index + 1);
				if (MathUtility.isNumber(word)) {
					String num = word + "_" + k;
					numbers.add(num);
					signs.add(num);
					numberPos.add(k);
					if (num2pos.containsKey(num)) {
						throw new RuntimeException("each number should be unique!");
					}
					num2pos.put(num, k);
				}

				if (k == words.length - 1) {
					numbers.add("X");
					num2pos.put("X", k);
					numberPos.add(k);
					signs.add("X");
				}
			}

			// according to equation, assign sign for each number
			String[] equa_tokens = lEquations.trim().split(" ");
			ArrayList<String> equa_tokens_list = new ArrayList<>();
			for (int e = 0; e < equa_tokens.length; e++) {
				equa_tokens_list.add(equa_tokens[e]);
			}
			boolean duplicateNumber = checkDuplicateNumberInEquation(equa_tokens_list);
			if (duplicateNumber) {
				duplicate_instances.add(id);
			}

			int equal_pos = equa_tokens_list.indexOf("=");
			if (lSigns.equals("")) {
				// int equal_pos = Arrays.asList(equa_tokens).indexOf("=");
				// System.out.println(Arrays.toString(equa_tokens));
				// System.out.println("equal pos: " + equal_pos);
				for (int n = 0; n < numbers.size(); n++) {
					String number_k = numbers.get(n);
					String number = number_k.split("_")[0].trim();

					for (String num_in_equa : equa_tokens) {
						if (MathUtility.isNumber(num_in_equa) && MathUtility.isNumber(number)) {
							number = reFormatNumber(number, num_in_equa);
						}
					}

					// int number_pos_at_euq = Arrays.asList(equa_tokens).indexOf(number);
					int number_pos_at_euq = equa_tokens_list.indexOf(number);
					String sign_temp = addSign(equa_tokens, number_pos_at_euq, equal_pos);
					num2sign.put(number_k, sign_temp);
					signs.set(n, sign_temp);
				}
			} else {
				String[] annotatedSigns = lSigns.trim().split(" ");
				for (int n = 0; n < numbers.size(); n++) {
					String number_k = numbers.get(n);
					if (number_k.equals("X")) {
						// int number_pos_at_euq = Arrays.asList(equa_tokens).indexOf(number);
						int number_pos_at_euq = equa_tokens_list.indexOf(number_k);
						String sign_temp = addSign(equa_tokens, number_pos_at_euq, equal_pos);
						num2sign.put(number_k, sign_temp);
						signs.set(n, sign_temp);
					} else {
						num2sign.put(number_k, annotatedSigns[n]);
						signs.set(n, annotatedSigns[n]);
					}

				}
			}

			ProblemRepresentation probRep = new ProblemRepresentation(id, sQuestion.trim(), lEquations.trim(),
					lSolutions);
			probRep.setNum2pos(num2pos);
			probRep.setNumbers(numbers);
			/*
			 * Run pre-processing tasks including tokenization, lemmatization, sentence
			 * splitting, CFG parsing, dependency parsing, co-reference resolution
			 */
			esf.StructureTagger(probRep);
			/*
			 * detect the unknonwn(s) in the problem create a variable for it extracts and
			 * saves the feature that represents the meaning of the variable
			 *
			 */
			esf.FindUnknowns(probRep);
			/**
			 * Check whether the number of quantities are equal
			 */
			if (probRep.getNumberOfQuantities() != signs.size()) {
				// num2sign.remove(num2sign.get(num2sign.size() - 1));
				// num2pos.remove(num2pos.get(num2pos.size() - 1));
				// signs.remove(signs.get(signs.size() - 1));
				// numberPos.remove(numberPos.get(numberPos.size() - 1));
				// numbers.remove(numbers.get(numbers.size() - 1));
			}

			// if (probRep.getNumberOfQuantities() != numbers.size()) {
			// System.out.println(
			// "prop. quantitiy: " + probRep.getNumberOfQuantities() + ": " +
			// probRep.getQuantityValue());
			// System.out.println("sign size: " + signs.size());
			// System.out.println("numbers: " + numbers);
			// System.out.println("id: " + id + " " + sQuestion);
			// throw new RuntimeException();
			// }

			List<Quantity> quantities = probRep.getQuantities();
			int index = 0;
			boolean isXLast = (numbers.indexOf("X") == numbers.size() - 1);
			// if (!isXLast) {
			// Quantity temp = quantities.get(numbers.size() - 2);
			// quantities.set(numbers.size() - 2, quantities.get(numbers.size() - 1));
			// quantities.set(numbers.size() - 1, temp);
			// probRep.setQuantities(quantities);
			// }
			// for (Quantity q : quantities) {
			// String value = q.getStringValue();
			// String number_idx = numbers.get(index).split("_")[0].trim();
			// if (!value.equals(number_idx)) {
			// System.out.println("q: " + value + " n: " + number_idx);
			// System.out.println(
			// "prop. quantitiy: " + probRep.getNumberOfQuantities() + ": " +
			// probRep.getQuantityValue());
			// System.out.println("sign size: " + signs.size());
			// System.out.println("numbers: " + numbers);
			// System.out.println("id: " + id + " " + sQuestion);
			// throw new RuntimeException();
			// }
			// q.setSentencePos(numberPos.get(index));
			// q.setSign(signs.get(index), isTraining); // isTraining
			// index++;
			// }

			double solution = calculateSolution(num2sign);
			double difference = Math.abs(lSolutions - solution);
			if (difference > 10e-6) {
				System.out.println("iIndex" + id);
				for (String number_k : numbers) {
					System.out.println(
							number_k + " " + text.get(num2pos.get(number_k)).getForm() + " " + num2sign.get(number_k));
				}
				System.out.println("gold: " + lSolutions);
				System.out.println("test: " + solution);
				System.out.println(
						"prop. quantitiy: " + probRep.getNumberOfQuantities() + ": " + probRep.getQuantityValue());
				System.out.println("sign size: " + signs.size());
				System.out.println("numbers: " + numbers);
				System.out.println("id: " + id + " " + sQuestion);
				mis_instances.add(id);
				throw new RuntimeException("solutions do not match");
			}

			LatentAddSubInstance instance = new LatentAddSubInstance(instances.size() + 1, 1.0, id, lEquations,
					lSolutions, text, num2pos, num2sign, numbers, signs, numberPos);
			// if (!isTraining)
			esf.FindRelatedQuantity(probRep, new ArrayList<>());
			// instance.setQestionPart(ques_part);
			instance.setProblemRepresentation(probRep);
			// instance.setQuantities(quantities);
			// boolean isDiffTem = DifferenceSampleFeature.checkComparisonTemplate(signs,
			// lEquations);
			// if (isDiffTem) {
			// DifferenceSampleFeature.addFeaturesForTrainingData(quantities, probRep,
			// isTraining);
			// }
			DifferenceSampleFeatureChinese.addFeatures(probRep);
			WholeSampleFeatureChinese.addFeatures(probRep);
			// ChangeSampleFeature.addFeatures(probRep);

			if (isTraining)
				instance.setLabeled();
			else
				instance.setUnlabeled();
			instances.add(instance);
		}
		// par_bw.close();
		// System.out.println("duplicate instances " + duplicate_instances.size());
		// System.out.println(duplicate_instances);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savePath));
		oos.writeObject(instances);
		oos.close();
		System.out.println("Read " + instances.size() + " instances..");
		return instances.toArray(new LatentAddSubInstance[instances.size()]);

	}

	private static boolean checkDuplicateNumberInEquation(ArrayList<String> equa_tokens_list) {
		ArrayList<String> euqa_copy = new ArrayList<>();
		euqa_copy.addAll(equa_tokens_list);
		// euqa_copy.remove("+");
		// euqa_copy.remove("-");
		euqa_copy.remove("=");
		Set<String> set = new HashSet<>(equa_tokens_list);
		if (set.size() < euqa_copy.size())
			return true;
		else
			return false;
	}

	// add sign for each number according to equation
	private static String addSign(String[] equa_tokens, int number_pos_at_euq, int equal_pos) {

		String sign = "";
		// the number is irrelevant
		if (number_pos_at_euq == -1) {
			sign = "zero";
		}
		// in the left part of the equation
		else if (number_pos_at_euq < equal_pos) {
			// the first number of the left part of the equation
			if (number_pos_at_euq == 0) {
				sign = "minus";
			} else {
				if (equa_tokens[number_pos_at_euq - 1].equals("-")) {
					sign = "plus";
				} else {
					sign = "minus";
				}
			}
		}
		// in the right part of the equation
		else if (number_pos_at_euq > equal_pos) {
			// the first number of the left part of the equation
			if (number_pos_at_euq == 0) {
				sign = "minus";
			} else {
				if (equa_tokens[number_pos_at_euq - 1].equals("-")) {
					sign = "minus";
				} else {
					sign = "plus";
				}
			}
		}
		return sign;
	}

	private static boolean isNumeric(String str) {
		if (str.contains("d"))
			return false;
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static String reFormatNumber(String number_from_text, String number_from_equation) {
		double num_from_equation = Double.parseDouble(number_from_equation);
		double num_from_text = Double.parseDouble(number_from_text);
		double difference = num_from_equation - num_from_text;
		if (difference == 0.0)
			return number_from_equation;
		else
			return number_from_text;
	}

	private static double reFormatNumber(String str) {
		if (isNumeric(str)) {
			double d = Double.parseDouble(str);
			int d_int = (int) d;

		}
		return -1;
	}

	private static double calculateSolution(HashMap<String, String> num2sign) {
		String signForX = num2sign.get("X");
		double factor = 1.0;
		if (signForX.equals("minus"))
			factor = 1.0;
		else
			factor = -1.0;
		ArrayList<Double> numberList = new ArrayList<>();
		for (String number_k : num2sign.keySet()) {
			if (number_k.equals("X"))
				continue;
			String num_string = number_k.split("_")[0].trim();
			double num = Double.parseDouble(num_string);
			String num_sign = num2sign.get(number_k);
			if (num_sign.equals("minus")) {
				num = -1.0 * num;
			} else if (num_sign.equals("plus")) {
				num = 1.0 * num;
			} else if (num_sign.equals("zero")) {
				num = 0.0 * num;
			}
			numberList.add(num);
		}
		double sum = 0;
		for (Double d : numberList)
			sum += d;
		return factor * sum;

	}

	private static HashMap<Integer, String> readPosTagFile(String filepath, String lang)
			throws IOException, FileNotFoundException {
		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF8"));
		ArrayList<Integer> sentId = new ArrayList<>();
		HashMap<Integer, String> sentId2sentTag = new HashMap<>();
		String line;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		if (lang.equals("en")) {
			while ((line = scan.readLine()) != null) {
				if (line.startsWith("Instance")) {
					// System.out.println(line);
					if (!first) {
						sb.append("#OUT#");
					}
					if (first)
						first = false;
					int start_idx = line.indexOf("e");
					int end_idx = line.indexOf("_");
					int id = Integer.parseInt(line.substring(start_idx + 1, end_idx));
					sentId.add(id);
					// System.out.println("id: " + id);

				} else {
					sb.append(line.trim() + " ");
				}
			}
		} else if (lang.equals("zh")) {
			while ((line = scan.readLine()) != null) {
				if (line.startsWith("Instance")) {
					// System.out.println(line);
					if (!first) {
						sb.append("#OUT#");
					}
					if (first)
						first = false;
					int start_idx = line.indexOf("e");
					int end_idx = line.indexOf(".");
					int id = Integer.parseInt(line.substring(start_idx + 1, end_idx));
					sentId.add(id);
					// System.out.println("id: " + id);

				} else {
					sb.append(line.trim() + " ");
				}
			}
		}
		scan.close();
		String[] sentences = sb.toString().trim().split("#OUT#");
		// System.out.println(sentences.length);
		for (int i = 0; i < sentences.length; i++) {
			String sentence = sentences[i];
			int id = sentId.get(i);
			sentId2sentTag.put(id, sentence);
		}
		// System.out.println(sentId2sentTag.size());
		return sentId2sentTag;
	}

}
