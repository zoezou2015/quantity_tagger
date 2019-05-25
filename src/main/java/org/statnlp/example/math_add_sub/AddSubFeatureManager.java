package org.statnlp.example.math_add_sub;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.statnlp.commons.types.Sentence;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;
import org.statnlp.example.math_add_sub.AddSubNetworkCompiler.NodeType;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.util.instance_parser.InstanceParser;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class AddSubFeatureManager extends FeatureManager {

	private static final long serialVersionUID = 6951613447170020167L;

	private enum LocalFeaType {
		unigram, bigram, multigram, transition, posTag
	}

	private enum GlobalFeaType {
		QuestionIntend, LargerQuantity, SmallerQuantity, WholeCue,
	}

	private static HashMap<String, ArrayList<Double>> word2embedding;

	public AddSubFeatureManager(GlobalNetworkParam param_g, InstanceParser instanceParser) {
		super(param_g, instanceParser);
	}

	public AddSubFeatureManager(GlobalNetworkParam param_g) throws NumberFormatException, IOException {
		super(param_g);
		// word2embedding = new HashMap<>();
		// word2embedding = PolyglotEmbeddingReader(AddSubConfig.lang);
	}

	@Override
	protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
		if (AddSubConfig.lang.equals("en")) {
			return extract_helper_English(network, parent_k, children_k, children_k_index);
		} else if (AddSubConfig.lang.equals("zh")) {
			return extract_helper_Chinese(network, parent_k, children_k, children_k_index);
		} else {
			return FeatureArray.EMPTY;
		}
	}

	private FeatureArray extract_helper_English(Network network, int parent_k, int[] children_k, int children_k_index) {

		int[] paArr = network.getNodeArray(parent_k);
		if (NodeType.values()[paArr[2]] == NodeType.leaf || NodeType.values()[paArr[2]] == NodeType.root)
			return FeatureArray.EMPTY;
		List<Integer> fs = new ArrayList<>();
		AddSubInstance inst = (AddSubInstance) network.getInstance();
		ArrayList<String> input = inst.getInput();
		ProblemRepresentation probRep = inst.getProblemRepresentation();
		Sentence text = inst.getProblemText();
		int iIndex = inst.getProblemId();

		HashMap<String, Integer> num2pos = inst.getNumber2Pos();
		String number_k = input.get(paArr[0]);
		int pos = num2pos.get(number_k);
		String number = number_k.split("_")[0].trim();

		Quantity unknown = probRep.getUnknownQuantities().get(0);
		Quantity corrQ = probRep.getQuantities().get(paArr[0]);
		ArrayList<CoreLabel> unKnownTypes = unknown.getType();
		ArrayList<CoreLabel> corrQTypes = corrQ.getType();

		int size = text.length();
		int numOrder = paArr[0];
		int signId = paArr[1];
		String output = signId + "";
		String lw = pos - 1 >= 0 ? text.get(pos - 1).getForm() : "START";
		String rw = pos + 1 < text.length() ? text.get(pos + 1).getForm() : "END";
		boolean isUnknowX = number_k.equals("X");
		String word = isUnknowX ? "number" : "X";
		int X_pos = num2pos.get("X");
		if (isUnknowX) {
			pos = text.length() - 1;
		}
		Sentence quesPart = inst.getQestionPart();
		String lt = pos - 1 >= 0 ? text.get(pos - 1).getPosTag() : "START";
		String rt = pos + 1 < text.length() ? text.get(pos + 1).getPosTag() : "END";

		String posTag = text.get(pos).getPosTag();
		// String posTag = text.get(size - 1).getPosTag();

		/*
		 * Add difference cue features
		 */

		/*
		 * gram features
		 */
		{
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name(), output, word));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name(), output, posTag));
			if (!isUnknowX) {
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-left", output, lw));
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-right", output, rw));

				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left", output, lt));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right", output, rt));

				// bigram feature
				String lw2 = getWord(text, pos, -2) + " " + getWord(text, pos, -1);
				String rw2 = getWord(text, pos, +1) + " " + getWord(text, pos, +2);
				String lt2 = getPosTag(text, pos, -2) + " " + getPosTag(text, pos, -1);
				String rt2 = getPosTag(text, pos, +1) + " " + getPosTag(text, pos, +2);
				fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-left", output, lw2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-right", output, rw2));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-left2", output, lt2));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right2", output, rt2));

				// trigram feature
				String lw3 = getWord(text, pos, -3) + " " + getWord(text, pos, -2) + " " + getWord(text, pos, -1);
				String rw3 = getWord(text, pos, +1) + " " + getWord(text, pos, +2) + " " + getWord(text, pos, +3);
				String lt3 = getPosTag(text, pos, -3) + " " + getPosTag(text, pos, -2) + " " + getPosTag(text, pos, -1);
				String rt3 = getPosTag(text, pos, +1) + " " + getPosTag(text, pos, +2) + " " + getPosTag(text, pos, +3);
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw3));
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw3));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-left3", output, lt3));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right3", output, rt3));
				// fourgram feature
				String lw4 = getWord(text, pos, -4) + " " + getWord(text, pos, -3) + " " + getWord(text, pos, -2) + " "
						+ getWord(text, pos, -1);
				String rw4 = getWord(text, pos, +1) + " " + getWord(text, pos, +2) + " " + getWord(text, pos, +3) + " "
						+ getWord(text, pos, +4);
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw4));
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw4));

				// fivegram feature
				// String lw5 = getWord(text, pos, -5) + " " + getWord(text, pos, -4) + " " +
				// getWord(text, pos, -3) + " "
				// + getWord(text, pos, -2) + " " + getWord(text, pos, -1);
				// String rw5 = getWord(text, pos, +1) + " " + getWord(text, pos, +2) + " " +
				// getWord(text, pos, +3) + " "
				// + getWord(text, pos, +4) + " " + getWord(text, pos, +5);
				// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-left",
				// output, lw5));
				// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-right",
				// output, rw5));
			}
		}
		/*
		 * transition feature
		 */
		{
			int[] childArr = network.getNodeArray(children_k[0]);
			NodeType childNodeType = NodeType.values()[childArr[2]];
			int childSignId = childArr[1];
			String childSign = childNodeType == NodeType.leaf ? "START" : childSignId + "";
			if (isUnknowX) {
				fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(), output, childSign));
			}
		}

		/*
		 * global feature
		 */
		{
			String[] numberRightWindow = new String[3];
			for (int i = 0; i < numberRightWindow.length; i++) {
				numberRightWindow[i] = getWord(text, pos, i + 1);
			}
			fs.add(this._param_g.toFeature(network, GlobalFeaType.QuestionIntend.name(), output,
					corrQ.isRelatedToQuestion() + ""));
		}

		if (probRep.getDifferenceCue() || probRep.getChangeCue()) {
			if (corrQ.isLargeQuantity()) {
				if (signId != 2 && !NetworkConfig.TRAINING_MODE) {
					return FeatureArray.NEGATIVE_INFINITY;
				}
				fs.add(this._param_g.toFeature(network, GlobalFeaType.LargerQuantity.name(), output, word));
			} else if (corrQ.isSmallQuantity() && !NetworkConfig.TRAINING_MODE) {
				if (signId != 1)
					return FeatureArray.NEGATIVE_INFINITY;
				fs.add(this._param_g.toFeature(network, GlobalFeaType.SmallerQuantity.name(), output, word));
			}
		}

		if (probRep.getWholeCue() && corrQ.isRelatedToQuestion()) {
			fs.add(this._param_g.toFeature(network, GlobalFeaType.WholeCue.name(), output, word));
		}
		FeatureArray featureArray = this.createFeatureArray(network, fs);
		return featureArray;

	}

	private FeatureArray extract_helper_Chinese(Network network, int parent_k, int[] children_k, int children_k_index) {

		int[] paArr = network.getNodeArray(parent_k);
		if (NodeType.values()[paArr[2]] == NodeType.leaf || NodeType.values()[paArr[2]] == NodeType.root)
			return FeatureArray.EMPTY;
		List<Integer> fs = new ArrayList<>();
		AddSubInstance inst = (AddSubInstance) network.getInstance();
		ArrayList<String> input = inst.getInput();
		ProblemRepresentation probRep = inst.getProblemRepresentation();
		Sentence text = inst.getProblemText();
		int iIndex = inst.getProblemId();

		HashMap<String, Integer> num2pos = inst.getNumber2Pos();
		String number_k = input.get(paArr[0]);
		int pos = num2pos.get(number_k);
		String number = number_k.split("_")[0].trim();

		// Quantity unknown = probRep.getUnknownQuantities().get(0);
		// Quantity corrQ = probRep.getQuantities().get(paArr[0]);
		// ArrayList<CoreLabel> unKnownTypes = unknown.getType();
		// ArrayList<CoreLabel> corrQTypes = corrQ.getType();

		int numOrder = paArr[0];
		int signId = paArr[1];
		String output = signId + "";
		String lw = pos - 1 >= 0 ? text.get(pos - 1).getForm() : "START";
		String rw = pos + 1 < text.length() ? text.get(pos + 1).getForm() : "END";
		boolean isUnknowX = number_k.equals("X");
		String word = isUnknowX ? "number" : "X";
		int X_pos = num2pos.get("X");
		Sentence quesPart = inst.getQestionPart();
		String lt = pos - 1 >= 0 ? text.get(pos - 1).getPosTag() : "START";
		String rt = pos + 1 < text.length() ? text.get(pos + 1).getPosTag() : "END";
		String posTag = text.get(pos).getPosTag();

		/*
		 * Add difference cue features
		 */

		/*
		 * gram features
		 */
		{
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name(), output, word));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name(), output, posTag));
			// if (!isUnknowX) {
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-left", output, lw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-right", output, rw));

			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left", output, lt));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right", output, rt));

			// bigram feature
			String lw2 = getWord(text, pos, -2) + " " + getWord(text, pos, -1);
			String rw2 = getWord(text, pos, +1) + " " + getWord(text, pos, +2);
			String lt2 = getPosTag(text, pos, -2) + " " + getPosTag(text, pos, -1);
			String rt2 = getPosTag(text, pos, +1) + " " + getPosTag(text, pos, +2);
			fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-left", output, lw2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-right", output, rw2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left2", output, lt2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right2", output, rt2));

			// trigram feature
			String lw3 = getWord(text, pos, -3) + " " + getWord(text, pos, -2) + " " + getWord(text, pos, -1);
			String rw3 = getWord(text, pos, +1) + " " + getWord(text, pos, +2) + " " + getWord(text, pos, +3);
			String lt3 = getPosTag(text, pos, -3) + " " + getPosTag(text, pos, -2) + " " + getPosTag(text, pos, -1);
			String rt3 = getPosTag(text, pos, +1) + " " + getPosTag(text, pos, +2) + " " + getPosTag(text, pos, +3);
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw3));
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw3));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left3", output, lt3));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right3", output, rt3));

			// fourgram feature
			String lw4 = getWord(text, pos, -4) + " " + getWord(text, pos, -3) + " " + getWord(text, pos, -2) + " "
					+ getWord(text, pos, -1);
			String rw4 = getWord(text, pos, +1) + " " + getWord(text, pos, +2) + " " + getWord(text, pos, +3) + " "
					+ getWord(text, pos, +4);
			// fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() +
			// "-left", output, lw4));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() +
			// "-right", output, rw4));

			// fivegram feature
			// String lw5 = getWord(text, pos, -5) + " " + getWord(text, pos, -4) + " " +
			// getWord(text, pos, -3) + " "
			// + getWord(text, pos, -2) + " " + getWord(text, pos, -1);
			// String rw5 = getWord(text, pos, +1) + " " + getWord(text, pos, +2) + " " +
			// getWord(text, pos, +3) + " "
			// + getWord(text, pos, +4) + " " + getWord(text, pos, +5);
			// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-left",
			// output, lw5));
			// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-right",
			// output, rw5));
		}
		// }
		/*
		 * transition feature
		 */
		{
			int[] childArr = network.getNodeArray(children_k[0]);
			NodeType childNodeType = NodeType.values()[childArr[2]];
			int childSignId = childArr[1];
			String childSign = childNodeType == NodeType.leaf ? "START" : childSignId + "";
			// if (!isUnknowX) {
			fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(), output, childSign));
			// }
		}

		/*
		 * global feature
		 */
		{
			// String[] numberRightWindow = new String[3];
			// for (int i = 0; i < numberRightWindow.length; i++) {
			// numberRightWindow[i] = getWord(text, pos, i + 1);
			// }
			// boolean match = QuantityTypeMatch(unKnownTypes, corrQTypes);
			// int duplicatewords = DuplicateWords(quesPart, numberRightWindow);
			// String dup = match ? "t" : "f";
			// // System.out.println(corrQ.isRelatedToQuestion());
			// if (!corrQ.isRelatedToQuestion() && signId != 0)
			// return FeatureArray.NEGATIVE_INFINITY;
			// fs.add(this._param_g.toFeature(network, GlobalFeaType.QuestionIntend.name(),
			// output,
			// corrQ.isRelatedToQuestion() + ""));
			// // System.out.println(duplicatewords);
			// // System.out.println(quesPart.toString() + " " +
			// // Arrays.asList(numberRightWindow).toString());
			// double sim_score = computeCosineSimilarity(unKnownTypes, corrQTypes);
			// ArrayList<Double> sim = new ArrayList<>();
			// sim.add(sim_score);
			// ArrayList<Integer> Fs = new ArrayList<>();
			// Fs.add(0);
			// featureArray.addNext(this.createFeatureArray(network, Fs, sim));
			// fs.add(this._param_g.toFeature(network, GlobalFeaType.QuestionIntend.name(),
			// output, sim_score + ""));
		}

		// if (probRep.getDifferenceCue() || probRep.getChangeCue()) {
		// // if (NetworkConfig.TRAINING_MODE)
		// // System.out.println(iIndex);
		// if (corrQ.isLargeQuantity()) {
		// // System.out.println("large num: " + corrQ.getStringValue() + " sign: " +
		// // output);
		// // System.out.println("text: " + inst.getProblemText());
		// if (signId != 2 && !NetworkConfig.TRAINING_MODE) {
		// return FeatureArray.NEGATIVE_INFINITY;
		// }
		// fs.add(this._param_g.toFeature(network, GlobalFeaType.LargerQuantity.name(),
		// output, word));
		// } else if (corrQ.isSmallQuantity() && !NetworkConfig.TRAINING_MODE) {
		// // System.out.println("small num: " + corrQ.getStringValue() + " sign: " +
		// // output);
		// // System.out.println("text: " + inst.getProblemText());
		// if (signId != 1)
		// return FeatureArray.NEGATIVE_INFINITY;
		// fs.add(this._param_g.toFeature(network, GlobalFeaType.SmallerQuantity.name(),
		// output, word));
		// }
		//
		// }

		// if (probRep.getWholeCue() && corrQ.isRelatedToQuestion()) {
		// fs.add(this._param_g.toFeature(network, GlobalFeaType.WholeCue.name(),
		// output, word));
		// }
		FeatureArray featureArray = this.createFeatureArray(network, fs);
		return featureArray;

	}

	private boolean QuantityTypeMatch(ArrayList<CoreLabel> unKnownTypes, ArrayList<CoreLabel> corrQTypes) {
		for (CoreLabel un_type : unKnownTypes) {
			String un_lemma = un_type.get(LemmaAnnotation.class);
			for (CoreLabel curr_type : corrQTypes) {
				String curr_lemma = curr_type.get(LemmaAnnotation.class);
				if (un_lemma.equals(curr_lemma))
					return true;
			}
		}
		return false;
	}

	private double QuantityTypeMatchScore(ArrayList<CoreLabel> unKnownTypes, ArrayList<CoreLabel> corrQTypes) {
		return computeCosineSimilarity(unKnownTypes, corrQTypes);
	}

	private double[] getEmbedding(ArrayList<CoreLabel> types) {
		double[] words_rep = new double[64];
		for (CoreLabel type : types) {
			String word = type.get(LemmaAnnotation.class);
			if (word2embedding.containsKey(word)) {
				ArrayList<Double> embedding = word2embedding.get(word);
				for (int i = 0; i < embedding.size(); i++) {
					words_rep[i] = embedding.get(i) > words_rep[i] ? embedding.get(i) : words_rep[i];
				}
			} else {

				// throw new RuntimeException(word);
			}
		}
		return words_rep;
	}

	private double computeCosineSimilarity(ArrayList<CoreLabel> unKnownTypes, ArrayList<CoreLabel> corrQTypes) {
		double[] window_rep = getEmbedding(unKnownTypes);
		double[] sent_rep = getEmbedding(corrQTypes);
		double fenzi = 0;
		for (int i = 0; i < window_rep.length; i++) {
			fenzi += window_rep[i] * sent_rep[i];
		}
		long left = 0;
		long right = 0;
		for (int i = 0; i < window_rep.length; i++) {
			left += window_rep[i] * window_rep[i];
			right += sent_rep[i] * sent_rep[i];
		}
		if (left * right == 0) {
			return 2.0000;
		}
		double result = fenzi / Math.sqrt(left * right);
		DecimalFormat df = new DecimalFormat("#.####");
		return Double.parseDouble(df.format(result));
	}

	public static double cosineSimilarity(int[] A, int[] B) {
		if (A.length != B.length) {
			return 2.0000;
		}
		if (A == null || B == null) {
			return 2.0000;
		}
		long fenzi = 0;
		for (int i = 0; i < A.length; i++) {
			fenzi += A[i] * B[i];
		}
		long left = 0;
		long right = 0;
		for (int i = 0; i < A.length; i++) {
			left += A[i] * A[i];
			right += B[i] * B[i];
		}
		if (left * right == 0) {
			return 2.0000;
		}
		double result = fenzi / Math.sqrt(left * right);
		DecimalFormat df = new DecimalFormat("#.####");
		return Double.parseDouble(df.format(result));
	}

	private String getPosTag(Sentence sent, int index, int offset) {
		int target = index + offset;
		if (target == -1) {
			return "Start";
		} else if (target == sent.length()) {
			return "END";
		} else if (target >= 0 && target < sent.length()) {
			if (sent.get(target).getPosTag().equals("")) { // for multiple
															// whitespaces..
				return "<UNK>";
			}
			return sent.get(target).getPosTag();
		} else {
			return "<PAD>";
		}
	}

	private String getWord(Sentence sent, int index, int offset) {
		int target = index + offset;
		if (target == -1) {
			return "Start";
		} else if (target == sent.length()) {
			return "END";
		} else if (target >= 0 && target < sent.length()) {
			if (sent.get(target).getForm().equals("")) { // for multiple
															// whitespaces..
				return "<UNK>";
			}
			return sent.get(target).getForm();
		} else {
			return "<PAD>";
		}
	}

	private HashMap<String, ArrayList<Double>> PolyglotEmbeddingReader(String lang)
			throws NumberFormatException, IOException {
		String filepath = "nn-crf-interface/neural_server/polyglot/polyglot-" + lang + ".txt";
		HashMap<String, ArrayList<Double>> word2embedding = new HashMap<>();
		BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF8"));
		String line;
		while ((line = scan.readLine()) != null) {
			String[] tokens = line.split(" ");
			if (tokens.length != 65) {
				throw new RuntimeException("The size of embedding is not correct!");
			}
			String word = tokens[0].toLowerCase();
			ArrayList<Double> embedding = new ArrayList<>();
			for (int i = 1; i < tokens.length; i++) {
				double dim = Double.parseDouble(tokens[i]);
				embedding.add(dim);

			}
			word2embedding.put(word, embedding);
		}
		scan.close();
		return word2embedding;
	}

	private int DuplicateWords(Sentence quesPart, String[] wordWindow) {
		int count = 0;

		for (int i = 0; i < quesPart.length(); i++) {
			for (int j = 0; j < wordWindow.length; j++) {
				String wordFromQuestion = quesPart.get(i).getForm();
				String wordFromWindow = wordWindow[j];
				if (wordFromQuestion.contentEquals(wordFromWindow)) {
					count++;
					wordWindow[j] = "";
				}
			}
		}

		return count;
	}

}
