package org.statnlp.example.math_add_sub_fix_semi;

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
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubNetworkCompiler.NodeType;
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubNetworkCompiler.SubNodeType;
import org.statnlp.example.math_add_sub_latent.LatentAddSubInstance;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkConfig;

public class FixSemiAddSubFeatureManager extends FeatureManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6698281190159737672L;

	private enum LocalFeaType {
		unigram, bigram, multigram, fourgram, fivegram, transition, pattern, boundary_left, boundary_right, posTag, transition_posTag
	}

	private enum GlobalFeaType {
		QuestionIntend, LargerQuantity, SmallerQuantity, WholeCue,
	}

	private static HashMap<String, ArrayList<Double>> word2embedding;

	public FixSemiAddSubFeatureManager(GlobalNetworkParam param_g) throws NumberFormatException, IOException {
		super(param_g);
		// word2embedding = new HashMap<>();
		// word2embedding = PolyglotEmbeddingReader(VariantAddSubConfig.lang);
	}

	@Override
	protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
		int[] paArr = network.getNodeArray(parent_k);
		if (NodeType.values()[paArr[3]] == NodeType.leaf || NodeType.values()[paArr[3]] == NodeType.root)
			return FeatureArray.EMPTY;
		if (FixSemiAddSubConfig.lang.equals("en")) {
			return extract_helper_English(network, parent_k, children_k, children_k_index);
		} else {
			return extract_helper_Chinese(network, parent_k, children_k, children_k_index);
		}

	}

	private FeatureArray extract_helper_English(Network network, int parent_k, int[] children_k, int children_k_index) {
		int[] paArr = network.getNodeArray(parent_k);
		List<Integer> fs = new ArrayList<>();
		LatentAddSubInstance inst = (LatentAddSubInstance) network.getInstance();
		Sentence input = inst.getInput();
		Sentence text = inst.getProblemText();
		HashMap<String, Integer> num2pos = inst.getNumber2Pos();
		ArrayList<Integer> numberPos = inst.getNumberPos();
		ArrayList<String> numbers = inst.getNumbers();

		int parNumPos = paArr[0];
		int parWordPos = paArr[1];
		int signId = paArr[2];
		String output = signId + "";
		String word = input.get(parNumPos).getForm();
		// int index = numbers.indexOf(word + "_" + parNumPos);
		// if (index == -1)
		int index = numberPos.indexOf(parNumPos);
		String posTag = input.get(parWordPos).getPosTag();
		Quantity corrQ = inst.getProblemRepresentation().getQuantities().get(index);
		ProblemRepresentation probRep = inst.getProblemRepresentation();
		word = num2pos.containsValue(parWordPos) ? "number" : word;
		word = (parWordPos == num2pos.get("X") ? "X" : word);
		SubNodeType par_subNodeType = SubNodeType.values()[paArr[3]];
		// node type feature
		// fs.add(this._param_g.toFeature(network, LocalFeaType.pattern.name(), output,
		// par_subNodeType.toString()));
		fs.add(this._param_g.toFeature(network, LocalFeaType.pattern.name(), word, par_subNodeType.toString()));

		int[] childArr = network.getNodeArray(children_k[0]);
		int chNumPos = childArr[0];
		int chWordPos = childArr[1];

		String childWord = getWord(text, childArr[1], 0, numberPos);
		String childTag = getPosTag(text, childArr[1], 0);
		SubNodeType child_subNodeType = SubNodeType.values()[childArr[4]];
		int childSignId = childArr[2];
		NodeType childNodeType = NodeType.values()[childArr[3]];
		String childSign = childNodeType == NodeType.leaf ? "START" : childSignId + "";
		String child_subType = childNodeType == NodeType.leaf ? "START" : child_subNodeType.toString();
		/*
		 * gram feature
		 */
		if (parNumPos == chNumPos) {
			for (int k = chWordPos; k <= parWordPos; k++) {
				// unigram feature
				String lw = k - 1 >= 0 ? text.get(k - 1).getForm() : "START";
				String rw = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
				String lt = k - 1 >= 0 ? text.get(k - 1).getPosTag() : "START";
				String rt = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name(), output, word));
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-left", output, lw));
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-right", output, rw));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name(), output, posTag));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left", output, lt));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right", output, rt));

				// bigram feature
				String lw2 = getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
				String rw2 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos);
				String lt2 = getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
				String rt2 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2);
				fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-left", output, lw2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-right", output, rw2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left2", output, lt2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right2", output, rt2));

				// trigram feature
				String lw3 = getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
						+ getWord(text, k, -1, numberPos);
				String rw3 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
						+ getWord(text, k, +3, numberPos);
				String lt3 = getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
				String rt3 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3);
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw3));
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw3));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-left3", output, lt3));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right3", output, rt3));

				// fourgram feature
				String lw4 = getWord(text, k, -4, numberPos) + " " + getWord(text, k, -3, numberPos) + " "
						+ getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
				String rw4 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
						+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos);

				String lt4 = getPosTag(text, k, -4) + " " + getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " "
						+ getPosTag(text, k, -1);
				String rt4 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3) + " "
						+ getPosTag(text, k, +4);

				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw4));
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw4));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-left4", output, lt4));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right4", output, rt4));

				// fivegram feature
				String lw5 = getWord(text, k, -5, numberPos) + " " + getWord(text, k, -4, numberPos) + " "
						+ getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
						+ getWord(text, k, -1, numberPos);
				String rw5 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
						+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos) + " "
						+ getWord(text, k, +5, numberPos);
				// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-left",
				// output, lw5));
				// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-right",
				// output, rw5));
			}
			String blw = chWordPos - 1 >= 0 ? text.get(chWordPos - 1).getForm() : "START";
			String brw = parWordPos + 1 < text.length() ? text.get(chWordPos + 1).getPosTag() : "END";
			String blt = chWordPos - 1 >= 0 ? text.get(chWordPos - 1).getPosTag() : "START";
			String brt = parWordPos + 1 < text.length() ? text.get(parWordPos + 1).getPosTag() : "END";
			fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_left.name(), output, blw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_right.name(), output, brw));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_left.name() +
			// "-tag", output, blt));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_right.name() +
			// "-tag", output, brt));
		} else {

			// unigram feature
			int k = parWordPos;
			String lw = k - 1 >= 0 ? text.get(k - 1).getForm() : "START";
			String rw = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
			String lt = k - 1 >= 0 ? text.get(k - 1).getPosTag() : "START";
			String rt = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name(), output, word));
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-left", output, lw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-right", output, rw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name(), output, posTag));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left", output, lt));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right", output, rt));

			// bigram feature
			String lw2 = getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
			String rw2 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos);
			String lt2 = getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
			String rt2 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2);
			fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-left", output, lw2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-right", output, rw2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left2", output, lt2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right2", output, rt2));

			// trigram feature
			String lw3 = getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
					+ getWord(text, k, -1, numberPos);
			String rw3 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
					+ getWord(text, k, +3, numberPos);
			String lt3 = getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
			String rt3 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3);
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw3));
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw3));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-left3", output, lt3));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-right3", output, rt3));

			// fourgram feature
			String lw4 = getWord(text, k, -4, numberPos) + " " + getWord(text, k, -3, numberPos) + " "
					+ getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
			String rw4 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
					+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos);

			String lt4 = getPosTag(text, k, -4) + " " + getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " "
					+ getPosTag(text, k, -1);
			String rt4 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3) + " "
					+ getPosTag(text, k, +4);

			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw4));
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw4));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-left4", output, lt4));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-right4", output, rt4));

			// fivegram feature
			String lw5 = getWord(text, k, -5, numberPos) + " " + getWord(text, k, -4, numberPos) + " "
					+ getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
					+ getWord(text, k, -1, numberPos);
			String rw5 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
					+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos) + " "
					+ getWord(text, k, +5, numberPos);
			// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-left",
			// output, lw5));
			// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-right",
			// output, rw5));

		}

		/*
		 * transition feature
		 */

		{

			/** pattern #1 */
			// fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(),
			// output, childSign));
			/** pattern #2 */
			fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(), output, child_subType));
			/** pattern #3 */
			fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(), par_subNodeType.toString(),
					child_subType));
			/** pattern #4 */
			// fs.add(this._param_g.toFeature(network,
			// LocalFeaType.transition_posTag.name(), posTag, childTag));
		}
		FeatureArray featureArray = this.createFeatureArray(network, fs);

		/** Global Feature */
		{

			// if (par_subNodeType == SubNodeType.number)
			{
				// fs.add(this._param_g.toFeature(network, GlobalFeaType.QuestionIntend.name(),
				// output,
				// corrQ.isRelatedToQuestion() + ""));
				if (!corrQ.isRelatedToQuestion() && signId != 0 && !NetworkConfig.TRAINING_MODE)
					return FeatureArray.NEGATIVE_INFINITY;
				if (probRep.getDifferenceCue() || probRep.getChangeCue()) {

					if (corrQ.isLargeQuantity()) {
						if (signId != 2 && !NetworkConfig.TRAINING_MODE) {
							return FeatureArray.NEGATIVE_INFINITY;
						}
						fs.add(this._param_g.toFeature(network, GlobalFeaType.LargerQuantity.name(), output, word));
					} else if (corrQ.isSmallQuantity() && !NetworkConfig.TRAINING_MODE) {
						if (signId != 1 && !NetworkConfig.TRAINING_MODE)
							return FeatureArray.NEGATIVE_INFINITY;
						fs.add(this._param_g.toFeature(network, GlobalFeaType.SmallerQuantity.name(), output, word));
					}
				}
				if (probRep.getWholeCue() && corrQ.isRelatedToQuestion()) {
					if (!word.equals("X") && signId != 2 && !NetworkConfig.TRAINING_MODE)
						return FeatureArray.NEGATIVE_INFINITY;
					fs.add(this._param_g.toFeature(network, GlobalFeaType.WholeCue.name(), output, word));
				}
			}

		}

		return featureArray;

	}

	private FeatureArray extract_helper_Chinese(Network network, int parent_k, int[] children_k, int children_k_index) {

		int[] paArr = network.getNodeArray(parent_k);
		List<Integer> fs = new ArrayList<>();
		LatentAddSubInstance inst = (LatentAddSubInstance) network.getInstance();
		Sentence input = inst.getInput();
		Sentence text = inst.getProblemText();
		HashMap<String, Integer> num2pos = inst.getNumber2Pos();
		ArrayList<Integer> numberPos = inst.getNumberPos();
		ArrayList<String> numbers = inst.getNumbers();

		int parNumPos = paArr[0];
		int parWordPos = paArr[1];
		int parSignId = paArr[2];
		String output = parSignId + "";
		String word = input.get(parNumPos).getForm();
		int index = numberPos.indexOf(parNumPos);
		String posTag = input.get(parWordPos).getPosTag();
		Quantity corrQ = inst.getProblemRepresentation().getQuantities().get(index);
		ProblemRepresentation probRep = inst.getProblemRepresentation();
		word = num2pos.containsValue(parWordPos) ? "number" : word;
		word = parWordPos == num2pos.get("X") ? "X" : word;
		SubNodeType par_subNodeType = SubNodeType.values()[paArr[3]];

		int[] childArr = network.getNodeArray(children_k[0]);
		int chNumPos = childArr[0];
		int chWordPos = childArr[1];
		int childSignId = childArr[2];
		String childWord = getWord(text, childArr[1], 0, numberPos);
		String childTag = getPosTag(text, childArr[1], 0);
		SubNodeType child_subNodeType = SubNodeType.values()[childArr[3]];
		NodeType childNodeType = NodeType.values()[childArr[3]];
		String childSign = childNodeType == NodeType.leaf ? "START" : childSignId + "";
		String child_subType = childNodeType == NodeType.leaf ? "START" : child_subNodeType.toString();

		// node type feature
		fs.add(this._param_g.toFeature(network, LocalFeaType.pattern.name(), output, par_subNodeType.toString()));
		fs.add(this._param_g.toFeature(network, LocalFeaType.pattern.name(), word, par_subNodeType.toString()));

		/*
		 * gram feature
		 */
		if (parNumPos == chNumPos) {
			for (int k = parWordPos; k <= chWordPos; k++) {
				// unigram feature

				String lw = k - 1 >= 0 ? text.get(k - 1).getForm() : "START";
				String rw = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
				String lt = k - 1 >= 0 ? text.get(k - 1).getPosTag() : "START";
				String rt = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name(), output, word));
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-left", output, lw));
				fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-right", output, rw));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name(), output, posTag));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left", output, lt));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right", output, rt));

				// bigram feature
				String lw2 = getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
				String rw2 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos);
				String lt2 = getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
				String rt2 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2);
				fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-left", output, lw2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-right", output, rw2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left2", output, lt2));
				fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right2", output, rt2));

				// trigram feature
				String lw3 = getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
						+ getWord(text, k, -1, numberPos);
				String rw3 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
						+ getWord(text, k, +3, numberPos);
				String lt3 = getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
				String rt3 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3);
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw3));
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw3));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-left3", output, lt3));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right3", output, rt3));

				// fourgram feature
				String lw4 = getWord(text, k, -4, numberPos) + " " + getWord(text, k, -3, numberPos) + " "
						+ getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
				String rw4 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
						+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos);

				String lt4 = getPosTag(text, k, -4) + " " + getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " "
						+ getPosTag(text, k, -1);
				String rt4 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3) + " "
						+ getPosTag(text, k, +4);

				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw4));
				fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw4));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-left4", output, lt4));
				// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
				// "-right4", output, rt4));

				// fivegram feature
				String lw5 = getWord(text, k, -5, numberPos) + " " + getWord(text, k, -4, numberPos) + " "
						+ getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
						+ getWord(text, k, -1, numberPos);
				String rw5 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
						+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos) + " "
						+ getWord(text, k, +5, numberPos);
				// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-left",
				// output, lw5));
				// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-right",
				// output, rw5));

			}
			String blw = chWordPos - 1 >= 0 ? text.get(chWordPos - 1).getForm() : "START";
			String brw = parWordPos + 1 < text.length() ? text.get(chWordPos + 1).getPosTag() : "END";
			String blt = chWordPos - 1 >= 0 ? text.get(chWordPos - 1).getPosTag() : "START";
			String brt = parWordPos + 1 < text.length() ? text.get(parWordPos + 1).getPosTag() : "END";
			fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_left.name(), output, blw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_right.name(), output, brw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_left.name() + "-tag", output, blt));
			fs.add(this._param_g.toFeature(network, LocalFeaType.boundary_right.name() + "-tag", output, brt));
		} else {
			// unigram feature
			int k = parWordPos;
			String lw = k - 1 >= 0 ? text.get(k - 1).getForm() : "START";
			String rw = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
			String lt = k - 1 >= 0 ? text.get(k - 1).getPosTag() : "START";
			String rt = k + 1 < text.length() ? text.get(k + 1).getPosTag() : "END";
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name(), output, word));
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-left", output, lw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.unigram.name() + "-right", output, rw));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name(), output, posTag));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left", output, lt));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-right", output, rt));

			// bigram feature
			String lw2 = getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
			String rw2 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos);
			String lt2 = getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
			String rt2 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2);
			fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-left", output, lw2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.bigram.name() + "-right", output, rw2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-left2", output, lt2));
			fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() + "-right2", output, rt2));

			// trigram feature
			String lw3 = getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
					+ getWord(text, k, -1, numberPos);
			String rw3 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
					+ getWord(text, k, +3, numberPos);
			String lt3 = getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " " + getPosTag(text, k, -1);
			String rt3 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3);
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw3));
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw3));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-left3", output, lt3));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-right3", output, rt3));

			// fourgram feature
			String lw4 = getWord(text, k, -4, numberPos) + " " + getWord(text, k, -3, numberPos) + " "
					+ getWord(text, k, -2, numberPos) + " " + getWord(text, k, -1, numberPos);
			String rw4 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
					+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos);

			String lt4 = getPosTag(text, k, -4) + " " + getPosTag(text, k, -3) + " " + getPosTag(text, k, -2) + " "
					+ getPosTag(text, k, -1);
			String rt4 = getPosTag(text, k, +1) + " " + getPosTag(text, k, +2) + " " + getPosTag(text, k, +3) + " "
					+ getPosTag(text, k, +4);

			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-left", output, lw4));
			fs.add(this._param_g.toFeature(network, LocalFeaType.multigram.name() + "-right", output, rw4));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-left4", output, lt4));
			// fs.add(this._param_g.toFeature(network, LocalFeaType.posTag.name() +
			// "-right4", output, rt4));

			// fivegram feature
			String lw5 = getWord(text, k, -5, numberPos) + " " + getWord(text, k, -4, numberPos) + " "
					+ getWord(text, k, -3, numberPos) + " " + getWord(text, k, -2, numberPos) + " "
					+ getWord(text, k, -1, numberPos);
			String rw5 = getWord(text, k, +1, numberPos) + " " + getWord(text, k, +2, numberPos) + " "
					+ getWord(text, k, +3, numberPos) + " " + getWord(text, k, +4, numberPos) + " "
					+ getWord(text, k, +5, numberPos);
			// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-left",
			// output, lw5));
			// fs.add(this._param_g.toFeature(network, FeaType.multigram.name() + "-right",
			// output, rw5));

		}

		/*
		 * transition feature
		 */

		{

			/** pattern #1 */
			// fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(),
			// output, childSign));
			/** pattern #2 */
			fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(), output, child_subType));
			/** pattern #3 */
			// fs.add(this._param_g.toFeature(network, LocalFeaType.transition.name(),
			// par_subNodeType.toString(),
			// child_subType));
			/** pattern #4 */
			fs.add(this._param_g.toFeature(network, LocalFeaType.transition_posTag.name(), posTag, childTag));
		}

		/** Global Feature */
		{
			if (probRep.getQuantities().size() <= 3) {

				if (!corrQ.isRelatedToQuestion() && parSignId != 0 && !NetworkConfig.TRAINING_MODE) {
					return FeatureArray.NEGATIVE_INFINITY;
					// fs.add(this._param_g.toFeature(network, GlobalFeaType.QuestionIntend.name(),
					// output,corrQ.isRelatedToQuestion() + ""));
				}
			}

			if (probRep.getDifferenceCue() || probRep.getChangeCue()) {
				// System.out.println("small");
				if (corrQ.isLargeQuantity()) {
					if (parSignId != 2 && !NetworkConfig.TRAINING_MODE) {
						return FeatureArray.NEGATIVE_INFINITY;
					}
					// fs.add(this._param_g.toFeature(network, GlobalFeaType.LargerQuantity.name(),
					// output, word));
				} else if (corrQ.isSmallQuantity() && !NetworkConfig.TRAINING_MODE) {
					if (parSignId != 1)
						return FeatureArray.NEGATIVE_INFINITY;

					// fs.add(this._param_g.toFeature(network, GlobalFeaType.SmallerQuantity.name(),
					// output, word));
				}
			}
			if (probRep.getWholeCue() && corrQ.isRelatedToQuestion()) {
				if (!corrQ.isUnknown() && !NetworkConfig.TRAINING_MODE && parSignId != 2)
					return FeatureArray.NEGATIVE_INFINITY;
				// fs.add(this._param_g.toFeature(network, GlobalFeaType.WholeCue.name(),
				// output, word));
			}
		}
		FeatureArray featureArray = this.createFeatureArray(network, fs);

		return featureArray;

	}

	private String getWord(Sentence sent, int index, int offset, ArrayList<Integer> numberPos) {
		int target = index + offset;
		if (target == -1) {
			return "Start";
		} else if (target == sent.length()) {
			return "END";
		} else if (target >= 0 && target < sent.length()) {
			if (sent.get(target).getForm().equals("")) { // for multiple
				return "<UNK>";
			}
			if (numberPos.contains(target) && numberPos.get(numberPos.size() - 1) == target)
				return "X";
			else if (numberPos.contains(target) && numberPos.get(numberPos.size() - 1) != target)
				return "number";
			return sent.get(target).getForm();
		} else {
			return "<PAD>";
		}
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

	private double[] getEmbedding(String[] words) {
		double[] words_rep = new double[64];
		for (String word : words) {
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

	private double[] getEmbedding(Sentence sentence) {
		double[] words_rep = new double[64];
		for (int k = 0; k < sentence.length(); k++) {
			String word = sentence.get(k).getForm();
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

	private double computeCosineSimilarity(String[] windows, Sentence question) {
		double[] window_rep = getEmbedding(windows);
		double[] sent_rep = getEmbedding(question);
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

}
