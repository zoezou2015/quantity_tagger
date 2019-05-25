package org.nlp.example;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.statnlp.commons.ml.opt.OptimizerFactory;
import org.statnlp.example.math.features.ExtractSentenceFeatures;
import org.statnlp.example.math.features.ExtractSentenceFeaturesChinese;
import org.statnlp.example.math_add_sub.AddSubConfig;
import org.statnlp.example.math_add_sub.EvalMath;
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubConfig;
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubFeatureManager;
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubNetworkCompiler;
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubReader;
import org.statnlp.example.math_add_sub_fix_semi.FixSemiAddSubView;
import org.statnlp.example.math_add_sub_latent.LatentAddSubInstance;
import org.statnlp.example.math_add_sub_semi.SemiAddSubConfig;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

public class FixSemiMathAddSubMain {
	static int numIterations = 100;
	static double l2 = 0.01;
	static double lr = 0.01;
	static String optim = "lbfgs";
	static int numThreads = 2;
	static boolean testOnTrain = true;
	static boolean debug = false;

	private static void argParser(String[] args) {
		int i = 0;
		while (i < args.length) {
			if (args[i].equals("window"))
				FixSemiAddSubConfig.halfWindowSize = Integer.parseInt(args[++i]);
			else if (args[i].equals("iter")) {
				numIterations = Integer.parseInt(args[++i]);
			}
		}
	}

	public static void main(String[] args) throws IOException, JSONException, InterruptedException {
		argParser(args);
		// NetworkConfig.STOPPING_CRITERIA =
		// NetworkConfig.STOPPING_CRITERIA.SMALL_ABSOLUTE_CHANGE;
		// NetworkConfig.STOPPING_CRITERIA =
		// NetworkConfig.StoppingCriteria.SMALL_RELATIVE_CHANGE;
		NetworkConfig.STOPPING_CRITERIA = NetworkConfig.StoppingCriteria.MAX_ITERATION_REACHED;
		NetworkConfig.USE_FEATURE_VALUE = true;
		ArrayList<String> signs = new ArrayList<>();
		signs.add("zero");
		signs.add("minus");
		signs.add("plus");
		String train_output_file = "data/math/train_output.txt";
		String test_output_file = "data/math/test_output.txt";
		String file = "data/math/AddSub/AddSub.json";
		String train_file = "data/math/AddSub/variant_train_data0.ser";
		String test_file = "data/math/AddSub/variant_test_data0.ser";
		String train_ids;
		String test_ids;
		String train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/variant_train_data0.ser";
		String test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/variant_test_data0.ser";
		ExtractSentenceFeaturesChinese esfc = null;
		ExtractSentenceFeatures esf = null;
		if (FixSemiAddSubConfig.isMath23K) {
			esfc = new ExtractSentenceFeaturesChinese();
			FixSemiAddSubConfig.cv_fold = 3;
			file = "data/math/Math23K/Math23K_AddSub.json";
			numIterations = 250;
			FixSemiAddSubConfig.lang = "zh";
		} else {
			// esf = new ExtractSentenceFeatures();
		}
		if (debug) {
			file = "data/math/debug.json";
		}
		LatentAddSubInstance[] train_insts;
		LatentAddSubInstance[] train_insts_clone;
		LatentAddSubInstance[] test_insts;

		for (int fold = 0; fold < FixSemiAddSubConfig.cv_fold; fold++) {
			NetworkConfig.TRAINING_MODE = true;
			NetworkConfig.L2_REGULARIZATION_CONSTANT = l2;
			NetworkConfig.NUM_THREADS = numThreads;
			NetworkConfig.STOPPING_CRITERIA = NetworkConfig.StoppingCriteria.MAX_ITERATION_REACHED;
			NetworkConfig.USE_FEATURE_VALUE = true;
			if (!SemiAddSubConfig.isMath23K) {
				train_file = "data/math/AddSub/variant_train_data" + fold + ".ser";
				test_file = "data/math/AddSub/variant_test_data" + fold + ".ser";

				if (fold == 0) {
					train_output_file = "data/math/AddSub/FixSemi_MA1_train_output.txt";
					test_output_file = "data/math/AddSub/FixSemi_MA1_test_output.txt";
				} else if (fold == 1) {
					train_output_file = "data/math/AddSub/FixSemi_MA2_train_output.txt";
					test_output_file = "data/math/AddSub/FixSemi_MA2_test_output.txt";
				} else if (fold == 2) {
					train_output_file = "data/math/AddSub/FixSemi_IXL_train_output.txt";
					test_output_file = "data/math/AddSub/FixSemi_IXL_test_output.txt";
				}
				train_ids = "data/math/AddSub/train" + fold + ".txt";
				test_ids = "data/math/AddSub/test" + fold + ".txt";
				train_insts = FixSemiAddSubReader.InstanceReader(train_file);
				test_insts = FixSemiAddSubReader.InstanceReader(test_file);
				// train_insts = SemiAddSubReader.InstanceReader(esf, file, train_ids, true);
				// test_insts = SemiAddSubReader.InstanceReader(esf, file, test_ids, false);
			} else {
				train_ids = "data/math/Math23K/" + FixSemiAddSubConfig.cv_fold + "-fold/fold-" + fold + "/train.txt";
				test_ids = "data/math/Math23K/" + FixSemiAddSubConfig.cv_fold + "-fold/fold-" + fold + "/test.txt";

				train_output_file = "data/math/Math23K/" + FixSemiAddSubConfig.cv_fold + "-fold/fold-" + fold
						+ "/FixSemi_train_out.txt";
				test_output_file = "data/math/Math23K/" + FixSemiAddSubConfig.cv_fold + "-fold/fold-" + fold
						+ "/FixSemi_test_out.txt";

				// train_insts = LatentAddSubReader.InstanceReader_Math23K(file, train_ids,
				// true);
				// train_insts_clone = LatentAddSubReader.InstanceReader_Math23K(file,
				// train_ids, false);
				// test_insts = LatentAddSubReader.InstanceReader_Math23K(file, test_ids,
				// false);
				train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/variant_train_data" + fold
						+ ".ser";
				test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/variant_test_data" + fold + ".ser";
				train_insts = FixSemiAddSubReader.InstanceReader_Math23K(esfc, file, train_ids, train_savePath, true);
				test_insts = FixSemiAddSubReader.InstanceReader_Math23K(esfc, file, test_ids, test_savePath, false);
			}
			OptimizerFactory optimizer = null;
			if (optim.equals("lbfgs")) {
				optimizer = OptimizerFactory.getLBFGSFactory();
			}
			GlobalNetworkParam gnp = new GlobalNetworkParam(optimizer);
			FixSemiAddSubNetworkCompiler compiler = new FixSemiAddSubNetworkCompiler(signs);
			FixSemiAddSubFeatureManager fm = new FixSemiAddSubFeatureManager(gnp);
			NetworkModel model = DiscriminativeNetworkModel.create(fm, compiler);

			model.train(train_insts, numIterations);
			if (debug) {
				model.visualize(FixSemiAddSubView.class, train_insts);
			}
			// model.visualize(FixSemiAddSubView.class, train_insts);

			NetworkConfig.TRAINING_MODE = false;
			// if (testOnTrain) {
			// System.out.println("*****Test on training set ****");
			// EvalMath.evaluate(model, train_insts_clone, train_output_file);
			// }
			System.out.println("*****Test on test set ****");
			EvalMath.evaluate(model, test_insts, test_output_file);
		}
	}
}
