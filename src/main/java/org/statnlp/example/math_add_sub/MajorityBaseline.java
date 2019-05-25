package org.statnlp.example.math_add_sub;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

/**
 * Classify every instance as increase, which means label all numbers as "+" and
 * all "X" as "-"
 * 
 * @author 1001937
 *
 */
public class MajorityBaseline {

	public static void main(String[] args) throws InterruptedException, IOException, JSONException {
		ArrayList<String> signs = new ArrayList<>();
		String train_output_file = "data/math/train_output.txt";
		String test_output_file = "data/math/test_output.txt";
		String file = "data/math/AddSub/AddSub.json";
		String train_file = "data/math/AddSub/train_data0.ser";
		String test_file = "data/math/AddSub/test_data0.ser";
		String train_ids;
		String test_ids;
		boolean isMath23K = true;
		int cv_fold = 3;
		if (isMath23K) {
			cv_fold = 3;
			file = "data/math/Math23K/Math23K_AddSub.json";
		}
		AddSubInstance[] train_insts;
		AddSubInstance[] test_insts;

		for (int fold = 0; fold < cv_fold; fold++) {
			if (!isMath23K) {
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

				// train_insts = AddSubReader.InstanceReader(file, train_ids, true);
				// train_insts_clone = AddSubReader.InstanceReader(file, train_ids, false);
				// test_insts = AddSubReader.InstanceReader(file, test_ids, false);
				train_insts = AddSubReader.InstanceReader(train_file);
				test_insts = AddSubReader.InstanceReader(test_file);

			} else {
				train_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/train_data" + fold + ".ser";
				test_file = "data/math/Math23K/" + AddSubConfig.cv_fold + "-fold/test_data" + fold + ".ser";

				train_output_file = "data/math/Math23K/" + cv_fold + "-fold/fold-" + fold + "/train_out.txt";
				test_output_file = "data/math/Math23K/" + cv_fold + "-fold/fold-" + fold + "/test_out.txt";

				train_insts = AddSubReader.InstanceReader_Math23K(train_file);
				test_insts = AddSubReader.InstanceReader_Math23K(test_file);
			}
			// if (testOnTrain) {
			// System.out.println("*****Test on training set ****");
			// EvalMath.evaluate(model, train_insts_clone, train_output_file);
			// }
			System.out.println("*****Test on test set ****");
			EvalMath.evaluate(test_insts, test_output_file);
		}
	}
}
