package org.nlp.example;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.statnlp.commons.ml.opt.OptimizerFactory;
import org.statnlp.example.math.features.ExtractSentenceFeatures;
import org.statnlp.example.math.features.ExtractSentenceFeaturesChinese;
import org.statnlp.example.math_add_sub.AddSubConfig;
import org.statnlp.example.math_add_sub.EvalMath;
import org.statnlp.example.math_add_sub_latent.LatentAddSubInstance;
import org.statnlp.example.math_add_sub_semi.SemiAddSubConfig;
import org.statnlp.example.math_add_sub_semi.SemiAddSubFeatureManager;
import org.statnlp.example.math_add_sub_semi.SemiAddSubNetworkCompiler;
import org.statnlp.example.math_add_sub_semi.SemiAddSubReader;
import org.statnlp.example.math_add_sub_semi.SemiAddSubView;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

public class SemiMathAddSubMain {
	static int numIterations = 100;
	static double l2 = 0.01;
	static double lr = 0.01;
	static String optim = "lbfgs";
	static int numThreads = 2;
	static boolean testOnTrain = true;
	static boolean debug = false;
	static boolean validation = false;

	private static void argsParser(String[] args) {
		int i = 0;
		while (i < args.length) {
			if (args[i].equals("-iter")) {
				numIterations = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-thread")) {
				numThreads = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-l2")) {
				l2 = Double.parseDouble(args[++i]);
			} else if (args[i].equals("-window")) {
				SemiAddSubConfig.halfWindowSize = Integer.parseInt(args[++i]) - 1;
			} else if (args[i].equals("-lang")) {
				String lang = args[++i];
				if (lang.equals("en")) {
					SemiAddSubConfig.isMath23K = false;
				} else if (lang.equals("zh")) {
					SemiAddSubConfig.isMath23K = true;
				} else {
					throw new RuntimeException("unsupport language");
				}
			} else {
				throw new RuntimeException("Unknown opreations!" + args[i]);
			}
			i++;
		}

	}

	public static void main(String[] args) throws IOException, JSONException, InterruptedException {
		argsParser(args);
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
		String train_savePath = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/variant_train_data0.ser";
		String test_savePath = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/variant_test_data0.ser";
		ExtractSentenceFeaturesChinese esfc = null;
		ExtractSentenceFeatures esf = null;
		if (SemiAddSubConfig.isMath23K) {
			esfc = new ExtractSentenceFeaturesChinese();
			SemiAddSubConfig.cv_fold = 3;
			file = "data/math/Math23K/Math23K_AddSub.json";
			numIterations = 250;
			SemiAddSubConfig.lang = "zh";
			SemiAddSubConfig.halfWindowSize = 5;
		} else {
			esf = new ExtractSentenceFeatures();
			SemiAddSubConfig.lang = "en";
			SemiAddSubConfig.halfWindowSize = 3;
		}
		if (debug) {
			file = "data/math/debug.json";
		}
		LatentAddSubInstance[] train_insts;
		LatentAddSubInstance[] train_insts_clone;
		LatentAddSubInstance[] test_insts;

		for (int fold = 0; fold < SemiAddSubConfig.cv_fold; fold++) {
			NetworkConfig.TRAINING_MODE = true;
			NetworkConfig.L2_REGULARIZATION_CONSTANT = l2;
			NetworkConfig.NUM_THREADS = numThreads;
			NetworkConfig.STOPPING_CRITERIA = NetworkConfig.StoppingCriteria.MAX_ITERATION_REACHED;
			NetworkConfig.USE_FEATURE_VALUE = true;
			if (!SemiAddSubConfig.isMath23K) {
				train_file = "data/math/AddSub/variant_train_data" + fold + ".ser";
				test_file = "data/math/AddSub/variant_test_data" + fold + ".ser";

				if (fold == 0) {
					train_output_file = "data/math/AddSub/Semi_MA1_train_output.txt";
					test_output_file = "data/math/AddSub/Semi_MA1_test_output.txt";
				} else if (fold == 1) {
					train_output_file = "data/math/AddSub/Semi_MA2_train_output.txt";
					test_output_file = "data/math/AddSub/Semi_MA2_test_output.txt";
				} else if (fold == 2) {
					train_output_file = "data/math/AddSub/Semi_IXL_train_output.txt";
					test_output_file = "data/math/AddSub/Semi_IXL_test_output.txt";
				}
				train_ids = "data/math/AddSub/train" + fold + ".txt";
				test_ids = "data/math/AddSub/test" + fold + ".txt";
				if (validation) {
					train_file = "data/math/AddSub/dev_variant_train_data" + fold + ".ser";
					test_file = "data/math/AddSub/dev_variant_test_data" + fold + ".ser";

					if (fold == 0) {
						train_output_file = "data/math/AddSub/dev_Semi_MA1_train_output.txt";
						test_output_file = "data/math/AddSub/dev_Semi_MA1_test_output.txt";
					} else if (fold == 1) {
						train_output_file = "data/math/AddSub/dev_Semi_MA2_train_output.txt";
						test_output_file = "data/math/AddSub/dev_Semi_MA2_test_output.txt";
					} else if (fold == 2) {
						train_output_file = "data/math/AddSub/dev_Semi_IXL_train_output.txt";
						test_output_file = "data/math/AddSub/dev_Semi_IXL_test_output.txt";
					}
					train_ids = "data/math/AddSub/dev_train" + fold + ".txt";
					test_ids = "data/math/AddSub/dev_test" + fold + ".txt";
				}
				// train_insts = SemiAddSubReader.InstanceReader(train_file);
				// test_insts = SemiAddSubReader.InstanceReader(test_file);
				train_insts = SemiAddSubReader.InstanceReader(esf, file, train_ids, true);
				test_insts = SemiAddSubReader.InstanceReader(esf, file, test_ids, false);
			} else {
				train_ids = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold + "/train.txt";
				test_ids = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold + "/test.txt";

				train_output_file = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold
						+ "/Semi_train_out.txt";
				test_output_file = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold
						+ "/Semi_test_out.txt";

				// train_insts = LatentAddSubReader.InstanceReader_Math23K(file, train_ids,
				// true);
				// train_insts_clone = LatentAddSubReader.InstanceReader_Math23K(file,
				// train_ids, false);
				// test_insts = LatentAddSubReader.InstanceReader_Math23K(file, test_ids,
				// false);
				train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/variant_train_data" + fold
						+ ".ser";
				test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/variant_test_data" + fold + ".ser";
				if (validation) {
					train_ids = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold
							+ "/dev_train.txt";
					test_ids = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold + "/dev_test.txt";

					train_output_file = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold
							+ "/dev_Semi_train_out.txt";
					test_output_file = "data/math/Math23K/" + SemiAddSubConfig.cv_fold + "-fold/fold-" + fold
							+ "/dev_Semi_test_out.txt";

					// train_insts = LatentAddSubReader.InstanceReader_Math23K(file, train_ids,
					// true);
					// train_insts_clone = LatentAddSubReader.InstanceReader_Math23K(file,
					// train_ids, false);
					// test_insts = LatentAddSubReader.InstanceReader_Math23K(file, test_ids,
					// false);
					train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_Semi_train_data" + fold
							+ ".ser";
					test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_Semi_test_data" + fold
							+ ".ser";

				}
				train_insts = SemiAddSubReader.InstanceReader_Math23K(esfc, file, train_ids, train_savePath, true);
				test_insts = SemiAddSubReader.InstanceReader_Math23K(esfc, file, test_ids, test_savePath, false);
			}
			OptimizerFactory optimizer = null;
			if (optim.equals("lbfgs")) {
				optimizer = OptimizerFactory.getLBFGSFactory();
			}
			GlobalNetworkParam gnp = new GlobalNetworkParam(optimizer);
			SemiAddSubNetworkCompiler compiler = new SemiAddSubNetworkCompiler(signs);
			SemiAddSubFeatureManager fm = new SemiAddSubFeatureManager(gnp);
			NetworkModel model = DiscriminativeNetworkModel.create(fm, compiler);

			model.train(train_insts, numIterations);
			if (debug) {
				model.visualize(SemiAddSubView.class, train_insts);
			}

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
