package org.nlp.example;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.statnlp.commons.ml.opt.OptimizerFactory;
import org.statnlp.example.math.features.ExtractSentenceFeaturesChinese;
import org.statnlp.example.math_add_sub.AddSubConfig;
import org.statnlp.example.math_add_sub.AddSubInstance;
import org.statnlp.example.math_add_sub.AddSubReader;
import org.statnlp.example.math_add_sub.AddSubView;
import org.statnlp.example.math_add_sub.EvalMath;
import org.statnlp.example.math_add_sub_duplicate.AddSubFeatureManager_Dup;
import org.statnlp.example.math_add_sub_duplicate.AddSubNetworkCompiler_Dup;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;
import org.statnlp.hypergraph.StringIndex;

public class MathAddSubMain_Dup {

	static int numIterations = 100;
	static double l2 = 0.01;
	static double lr = 0.01;
	static String optim = "lbfgs";
	static int numThreads = 1;
	static boolean testOnTrain = true;
	static boolean validation = false;
	static boolean debug = false;

	public static void main(String[] args) throws IOException, JSONException, InterruptedException {

		ArrayList<String> signs = new ArrayList<>();
		signs.add("zero");
		signs.add("minus");
		signs.add("plus");
		String train_output_file = "data/math/train_output.txt";
		String test_output_file = "data/math/test_output.txt";
		String file = "data/math/AddSub/AddSub.json";
		String train_file = "data/math/AddSub/train_data0.ser";
		String test_file = "data/math/AddSub/test_data0.ser";
		String train_ids;
		String test_ids;
		String train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/train_data0.ser";
		String test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/test_data0.ser";
		ExtractSentenceFeaturesChinese esf = null;
		if (AddSubConfig.isMath23K) {
			esf = new ExtractSentenceFeaturesChinese();
			AddSubConfig.cv_fold = 3;
			file = "data/math/Math23K/Math23K_AddSub.json";
			AddSubConfig.lang = "zh";
			numIterations = 250;
		}
		if (debug) {
			file = "data/math/debug.json";
		}
		AddSubInstance[] train_insts;
		AddSubInstance[] train_insts_clone = null;
		AddSubInstance[] test_insts;

		for (int fold = 0; fold < AddSubConfig.cv_fold; fold++) {
			NetworkConfig.TRAINING_MODE = true;
			NetworkConfig.L2_REGULARIZATION_CONSTANT = l2;
			NetworkConfig.NUM_THREADS = numThreads;
			NetworkConfig.STOPPING_CRITERIA = NetworkConfig.StoppingCriteria.MAX_ITERATION_REACHED;
			NetworkConfig.USE_FEATURE_VALUE = true;
			if (!AddSubConfig.isMath23K) {
				train_file = "data/math/AddSub/train_data" + fold + ".ser";
				test_file = "data/math/AddSub/test_data" + fold + ".ser";

				if (fold == 0) {
					train_output_file = "data/math/AddSub/MA1_train_output.txt";
					test_output_file = "data/math/AddSub/MA1_test_output.txt";
				} else if (fold == 1) {
					train_output_file = "data/math/AddSub/MA2_train_output.txt";
					test_output_file = "data/math/AddSub/MA2_test_output.txt";
				} else if (fold == 2) {
					train_output_file = "data/math/AddSub/IXL_train_output.txt";
					test_output_file = "data/math/AddSub/IXL_test_output.txt";
				}
				if (validation) {
					train_file = "data/math/AddSub/dev_train_data" + fold + ".ser";
					test_file = "data/math/AddSub/dev_test_data" + fold + ".ser";

					if (fold == 0) {
						train_output_file = "data/math/AddSub/dev_MA1_train_output.txt";
						test_output_file = "data/math/AddSub/dev_MA1_test_output.txt";
					} else if (fold == 1) {
						train_output_file = "data/math/AddSub/dev_MA2_train_output.txt";
						test_output_file = "data/math/AddSub/dev_MA2_test_output.txt";
					} else if (fold == 2) {
						train_output_file = "data/math/AddSub/dev_IXL_train_output.txt";
						test_output_file = "data/math/AddSub/dev_IXL_test_output.txt";
					}
				}
				// train_insts = AddSubReader.InstanceReader(file, train_ids, true);
				// train_insts_clone = AddSubReader.InstanceReader(file, train_ids, false);
				// test_insts = AddSubReader.InstanceReader(file, test_ids, false);
				train_insts = AddSubReader.InstanceReader(train_file);
				test_insts = AddSubReader.InstanceReader(test_file);
			} else {

				train_ids = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold + "/train.txt";
				test_ids = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold + "/test.txt";
				train_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/train_data" + fold + ".ser";
				test_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/test_data" + fold + ".ser";
				train_output_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold
						+ "/train_out2.txt";
				test_output_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold
						+ "/test_out2.txt";

				// train_insts = AddSubReader.InstanceReader_Math23K(file, train_ids, true);
				// train_insts_clone = AddSubReader.InstanceReader_Math23K(file, train_ids,
				// false);
				// test_insts = AddSubReader.InstanceReader_Math23K(file, test_ids, false);
				// train_insts = AddSubReader.InstanceReader_Math23K(train_file);
				// test_insts = AddSubReader.InstanceReader_Math23K(test_file);
				train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_train_data" + fold + ".ser";
				test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_test_data" + fold + ".ser";

				if (validation) {
					train_ids = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold + "/dev_train.txt";
					test_ids = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold + "/dev_test.txt";
					train_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_train_data" + fold + ".ser";
					test_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_test_data" + fold + ".ser";
					train_output_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold
							+ "/dev_train_out.txt";
					test_output_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/fold-" + fold
							+ "/dev_test_out.txt";
					train_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_train_data" + fold
							+ ".ser";
					test_savePath = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/dev_test_data" + fold + ".ser";

				}
				// train_insts = AddSubReader.InstanceReader_Math23K(file, train_ids, true);
				// train_insts_clone = AddSubReader.InstanceReader_Math23K(file, train_ids,
				// false);
				// test_insts = AddSubReader.InstanceReader_Math23K(file, test_ids, false);
				train_insts = AddSubReader.InstanceReader_Math23K(esf, file, train_ids, train_savePath, true);
				// train_insts_clone = AddSubReader.InstanceReader_Math23K(file, train_ids,
				// false);
				test_insts = AddSubReader.InstanceReader_Math23K(esf, file, test_ids, test_savePath, false);
			}
			OptimizerFactory optimizer = null;
			if (optim.equals("lbfgs")) {
				optimizer = OptimizerFactory.getLBFGSFactory();
			}
			GlobalNetworkParam gnp = new GlobalNetworkParam(optimizer);
			gnp.setStoreFeatureReps();

			AddSubNetworkCompiler_Dup compiler = new AddSubNetworkCompiler_Dup(signs);
			AddSubFeatureManager_Dup fm = new AddSubFeatureManager_Dup(gnp);
			NetworkModel model = DiscriminativeNetworkModel.create(fm, compiler);
			model.train(train_insts, numIterations);
			gnp.getStringIndex().buildReverseIndex();

			if (debug) {
				model.visualize(AddSubView.class, train_insts);
			}
			GlobalNetworkParam gnp_p = model.getFeatureManager().getParam_G();
			StringIndex strIdx = gnp_p.getStringIndex();
			strIdx.buildReverseIndex();
			// System.out.println(strIdx.size());
			// System.out.println(strIdx.ArrayLength());
			// System.out.println(gnp_p.getFeatureRepSize());
			gnp_p.setStoreFeatureReps();
			for (int ii = 0; ii < gnp_p.size(); ii++) {
				int[] fs = gnp_p.getFeatureRep(ii);
				String type = strIdx.get(fs[0]);
				String output = strIdx.get(fs[1]);
				String input = strIdx.get(fs[2]);
				if (type.equals("LargerQuantity")) {
					int typeId = fs[0];
					int outputId = fs[1];
					int inputId = fs[2];
					int featIdx = gnp_p.getFeatureIntMap().get(typeId).get(outputId).get(inputId);
					// System.out.println(type + ":" + output + ":" + input + ":" +
					// gnp_p.getWeight(ii));
				} else if (type.equals("SmallerQuantity")) {
					int typeId = fs[0];
					int outputId = fs[1];
					int inputId = fs[2];
					int featIdx = gnp_p.getFeatureIntMap().get(typeId).get(outputId).get(inputId);
					// System.out.println(type + ":" + output + ":" + input + ":" +
					// gnp_p.getWeight(ii));
				} else if (type.equals("transition")) {
					int typeId = fs[0];
					int outputId = fs[1];
					int inputId = fs[2];
					int featIdx = gnp_p.getFeatureIntMap().get(typeId).get(outputId).get(inputId);
					// System.out.println(type + ":" + output + ":" + input + ":" +
					// gnp_p.getWeight(ii));
				}

			}
			NetworkConfig.TRAINING_MODE = false;

			// if (testOnTrain && AddSubConfig.isMath23K) {
			// System.out.println("*****Test on training set ****");
			// EvalMath.evaluate(model, train_insts_clone, train_output_file);
			// }
			System.out.println("*****Test on test set ****");
			EvalMath.evaluate(model, test_insts, test_output_file);

		}
	}
}
