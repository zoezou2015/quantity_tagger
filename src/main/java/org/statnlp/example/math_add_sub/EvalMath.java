package org.statnlp.example.math_add_sub;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.Sentence;
import org.statnlp.example.math_add_sub_latent.LatentAddSubInstance;
import org.statnlp.hypergraph.NetworkModel;

public class EvalMath {
	public static void evaluate(NetworkModel model, AddSubInstance[] test_insts, String writeFile)
			throws InterruptedException, IOException {
		Instance[] predictions = model.test(test_insts);
		int corr = 0;
		int corr_plus = 0;
		int gold_total_plus = 0;
		int pred_total_plus = 0;
		int corr_minus = 0;
		int gold_total_minus = 0;
		int pred_total_minus = 0;
		int corr_zero = 0;
		int gold_total_zero = 0;
		int pred_total_zero = 0;

		// precision = corr/pred_total
		// recall = corr / gold_total

		int total = predictions.length;
		for (Instance pred : predictions) {
			AddSubInstance pred_inst = (AddSubInstance) pred;
			ArrayList<String> prediction = pred_inst.getPrediction();
			ArrayList<String> output = pred_inst.getOutput();
			ArrayList<String> numbers = pred_inst.getInput();
			// System.out.println(prediction.size());
			// System.out.println("Gold equation: " + pred_inst.getEquation());
			double pred_sol = calculateSolution(pred_inst, numbers, prediction);
			double gold_sol = pred_inst.getGoldSolution();
			double difference = Math.abs(pred_sol - gold_sol);
			if (difference < 10e-5) {
				corr++;
				// System.out.println("True");

			} else {
				// System.out.println("False");
			}

			for (int i = 0; i < output.size(); i++) {
				String o_s = output.get(i);
				String p_s = prediction.get(i);
				if (o_s.equals("plus")) {
					gold_total_plus++;
					if (p_s.equals("plus"))
						corr_plus++;
				} else if (o_s.equals("minus")) {
					gold_total_minus++;
					if (p_s.equals("minus"))
						corr_minus++;
				} else if (o_s.equals("zero")) {
					gold_total_zero++;
					if (p_s.equals("zero"))
						corr_zero++;
				}

				if (p_s.equals("plus")) {
					pred_total_plus++;
				} else if (p_s.equals("minus")) {
					pred_total_minus++;
				} else if (p_s.equals("zero")) {
					pred_total_zero++;
				}

			}

		}
		double accuracy = corr * 1.0 / total * 100;
		double accuracy_minus = corr_minus * 1.0 / pred_total_minus * 100.0;
		double recall_minus = corr_minus * 1.0 / gold_total_minus * 100;
		double accuracy_plus = corr_plus * 1.0 / pred_total_plus * 100.0;
		double recall_plus = corr_plus * 1.0 / gold_total_plus * 100;
		double accuracy_zero = corr_zero * 1.0 / pred_total_zero * 100.0;
		double recall_zero = corr_zero * 1.0 / gold_total_zero * 100;
		String results_plus = "[Plus]: accuracy: " + accuracy_plus + " recall: " + recall_plus + "\n";
		String results_minus = "[Mins]: accuracy: " + accuracy_minus + " recall: " + recall_minus + "\n";
		String results_zero = "[Zero]: accuracy: " + accuracy_zero + " recall: " + recall_zero + "\n";
		String results = "[Accuracy]: total: " + total + "\n correct:" + corr + "\n accuracy: " + accuracy + " \n";
		String finalRel = results + results_plus + results_minus + results_zero;
		writeMathResult(predictions, finalRel, writeFile);
		System.out.printf("[Accuracy]: total: %d, correct: %d, %.2f%%\n\n", total, corr, corr * 1.0 / total * 100);
		System.out.println(results_plus);
		System.out.println(results_minus);
		System.out.println(results_zero);
	}

	public static void evaluate(AddSubInstance[] test_insts, String writeFile)
			throws InterruptedException, IOException {

		int corr = 0;
		int corr_plus = 0;
		int gold_total_plus = 0;
		int pred_total_plus = 0;
		int corr_minus = 0;
		int gold_total_minus = 0;
		int pred_total_minus = 0;
		int corr_zero = 0;
		int gold_total_zero = 0;
		int pred_total_zero = 0;

		// precision = corr/pred_total
		// recall = corr / gold_total

		int total = test_insts.length;
		for (Instance pred : test_insts) {
			AddSubInstance pred_inst = (AddSubInstance) pred;
			ArrayList<String> prediction = new ArrayList<>();
			ArrayList<String> output = pred_inst.getOutput();
			ArrayList<String> numbers = pred_inst.getInput();
			for (String num : numbers) {
				if (num.equals("X")) {
					prediction.add("minus");
				} else {
					prediction.add("plus");
				}
			}
			// System.out.println(prediction.size());
			// System.out.println("Gold equation: " + pred_inst.getEquation());
			double pred_sol = calculateSolution(pred_inst, numbers, prediction);
			double gold_sol = pred_inst.getGoldSolution();
			double difference = Math.abs(pred_sol - gold_sol);
			if (difference < 10e-5) {
				corr++;
				// System.out.println("True");

			} else {
				// System.out.println("False");
			}

			for (int i = 0; i < output.size(); i++) {
				String o_s = output.get(i);
				String p_s = prediction.get(i);
				if (o_s.equals("plus")) {
					gold_total_plus++;
					if (p_s.equals("plus"))
						corr_plus++;
				} else if (o_s.equals("minus")) {
					gold_total_minus++;
					if (p_s.equals("minus"))
						corr_minus++;
				} else if (o_s.equals("zero")) {
					gold_total_zero++;
					if (p_s.equals("zero"))
						corr_zero++;
				}

				if (p_s.equals("plus")) {
					pred_total_plus++;
				} else if (p_s.equals("minus")) {
					pred_total_minus++;
				} else if (p_s.equals("zero")) {
					pred_total_zero++;
				}

			}

		}
		double accuracy = corr * 1.0 / total * 100;
		double accuracy_minus = corr_minus * 1.0 / pred_total_minus * 100.0;
		double recall_minus = corr_minus * 1.0 / gold_total_minus * 100;
		double accuracy_plus = corr_plus * 1.0 / pred_total_plus * 100.0;
		double recall_plus = corr_plus * 1.0 / gold_total_plus * 100;
		double accuracy_zero = corr_zero * 1.0 / pred_total_zero * 100.0;
		double recall_zero = corr_zero * 1.0 / gold_total_zero * 100;
		String results_plus = "[Plus]: accuracy: " + accuracy_plus + " recall: " + recall_plus + "\n";
		String results_minus = "[Mins]: accuracy: " + accuracy_minus + " recall: " + recall_minus + "\n";
		String results_zero = "[Zero]: accuracy: " + accuracy_zero + " recall: " + recall_zero + "\n";
		String results = "[Accuracy]: total: " + total + "\n correct:" + corr + "\n accuracy: " + accuracy + " \n";
		String finalRel = results + results_plus + results_minus + results_zero;
		// writeMathResult(test_insts, finalRel, writeFile);
		System.out.printf("[Accuracy]: total: %d, correct: %d, %.2f%%\n\n", total, corr, corr * 1.0 / total * 100);
		System.out.println(results_plus);
		System.out.println(results_minus);
		System.out.println(results_zero);
	}

	public static void evaluate(NetworkModel model, LatentAddSubInstance[] test_insts, String writeFile)
			throws InterruptedException, IOException {
		Instance[] predictions = model.test(test_insts);
		int corr = 0;
		int corr_plus = 0;
		int gold_total_plus = 0;
		int pred_total_plus = 0;
		int corr_minus = 0;
		int gold_total_minus = 0;
		int pred_total_minus = 0;
		int corr_zero = 0;
		int gold_total_zero = 0;
		int pred_total_zero = 0;
		int total = predictions.length;
		for (Instance pred : predictions) {
			LatentAddSubInstance pred_inst = (LatentAddSubInstance) pred;
			ArrayList<String> prediction = pred_inst.getPrediction();
			ArrayList<String> output = pred_inst.getOutput();
			ArrayList<String> numbers = pred_inst.getNumbers();
			// System.out.println(prediction.size());
			// System.out.println("Gold equation: " + pred_inst.getEquation());
			double pred_sol = calculateSolution(pred_inst, numbers, prediction);
			double gold_sol = pred_inst.getGoldSolution();
			double difference = Math.abs(pred_sol - gold_sol);
			if (difference < 10e-5) {
				corr++;
				// System.out.println("True");

			} else {
				// System.out.println("False");
			}
			for (int i = 0; i < output.size(); i++) {
				String o_s = output.get(i);
				String p_s = prediction.get(i);
				if (o_s.equals("plus")) {
					gold_total_plus++;
					if (p_s.equals("plus"))
						corr_plus++;
				} else if (o_s.equals("minus")) {
					gold_total_minus++;
					if (p_s.equals("minus"))
						corr_minus++;
				} else if (o_s.equals("zero")) {
					gold_total_zero++;
					if (p_s.equals("zero"))
						corr_zero++;
				}

				if (p_s.equals("plus")) {
					pred_total_plus++;
				} else if (p_s.equals("minus")) {
					pred_total_minus++;
				} else if (p_s.equals("zero")) {
					pred_total_zero++;
				}

			}
		}
		double accuracy = corr * 1.0 / total * 100;
		double accuracy_minus = corr_minus * 1.0 / pred_total_minus * 100.0;
		double recall_minus = corr_minus * 1.0 / gold_total_minus * 100;
		double accuracy_plus = corr_plus * 1.0 / pred_total_plus * 100.0;
		double recall_plus = corr_plus * 1.0 / gold_total_plus * 100;
		double accuracy_zero = corr_zero * 1.0 / pred_total_zero * 100.0;
		double recall_zero = corr_zero * 1.0 / gold_total_zero * 100;
		String results_plus = "[Plus]: accuracy: " + accuracy_plus + " recall: " + recall_plus + "\n";
		String results_minus = "[Mins]: accuracy: " + accuracy_minus + " recall: " + recall_minus + "\n";
		String results_zero = "[Zero]: accuracy: " + accuracy_zero + " recall: " + recall_zero + "\n";
		String results = "[Accuracy]: total: " + total + "\n correct:" + corr + "\n accuracy: " + accuracy + " \n";
		String finalRel = results + results_plus + results_minus + results_zero;
		System.out.printf("[Accuracy]: total: %d, correct: %d, %.2f%%\n\n", total, corr, corr * 1.0 / total * 100);
		System.out.println(results_plus);
		System.out.println(results_minus);
		System.out.println(results_zero);
		writeLatentMathResult(predictions, finalRel, writeFile);
	}

	private static void writeMathResult(Instance[] predictions, String results, String writeFile) throws IOException {
		PrintWriter pw = RAWF.writer(writeFile);
		for (int index = 0; index < predictions.length; index++) {
			Instance inst = predictions[index];
			AddSubInstance pred_inst = (AddSubInstance) inst;
			ArrayList<String> prediction = pred_inst.getPrediction();
			ArrayList<String> numbers = pred_inst.getInput();

			double pred_sol = calculateSolution(pred_inst, numbers, prediction);
			double gold_sol = pred_inst.getGoldSolution();
			double difference = Math.abs(pred_sol - gold_sol);
			boolean corr = false;
			if (difference < 10e-5) {
				corr = true;
			} else {
				corr = false;
			}

			pw.write("id: " + pred_inst.getProblemId() + " " + corr + "\n");
			pw.write(pred_inst.getProblemText() + "\n");
			pw.write("Gold Equation: " + pred_inst.getEquation() + "\nGold Solution: X = " + gold_sol + "\n");
			pw.write("Pred Equation: " + pred_inst.getPredEquation() + " = 0\nPred Solution: X = " + pred_sol + "\n");

			pw.write("\n\n");
		}
		pw.write(results + "\n");
		pw.close();
	}

	private static void writeLatentMathResult(Instance[] predictions, String results, String writeFile)
			throws IOException {
		PrintWriter pw = RAWF.writer(writeFile);
		for (int index = 0; index < predictions.length; index++) {
			Instance inst = predictions[index];
			LatentAddSubInstance pred_inst = (LatentAddSubInstance) inst;
			ArrayList<String> prediction = pred_inst.getPrediction();
			ArrayList<String> numbers = pred_inst.getNumbers();
			ArrayList<String> span = pred_inst.getSpan();
			Sentence text = pred_inst.getInput();
			StringBuilder text_sb = new StringBuilder();
			for (int pos = 0; pos < span.size(); pos++) {
				// text_sb.append(text.get(pos).getForm() + "(" + span.get(pos) + ") ");
				text_sb.append("(" + span.get(pos) + ") ");

			}
			String text_string = text_sb.toString();

			double pred_sol = calculateSolution(pred_inst, numbers, prediction);
			double gold_sol = pred_inst.getGoldSolution();
			double difference = Math.abs(pred_sol - gold_sol);
			boolean corr = false;
			if (difference < 10e-5) {
				corr = true;
			} else {
				corr = false;
			}
			pw.write("id: " + pred_inst.getProblemId() + " " + corr + "\n");
			pw.write(text_string + "\n");
			pw.write("Gold Equation: " + pred_inst.getEquation() + "\nGold Solution: X = " + gold_sol + "\n");
			pw.write("Pred Equation: " + pred_inst.getPredEquation() + " = 0\nPred Solution: X = " + pred_sol + "\n");
			pw.write("\n\n");
		}
		pw.write(results + "\n");
		pw.close();
	}

	private static double calculateSolution(AddSubInstance inst, ArrayList<String> numbers, ArrayList<String> sings) {
		StringBuilder sb = new StringBuilder();

		int idxForX = numbers.indexOf("X");
		String signForX = sings.get(idxForX);
		double factor = 1.0;
		if (signForX.equals("minus"))
			factor = 1.0;
		else
			factor = -1.0;
		ArrayList<Double> numberList = new ArrayList<>();
		for (int i = 0; i < numbers.size(); i++) {
			String number_k = numbers.get(i);
			if (number_k.equals("X")) {
				if (signForX.equals("minus"))
					sb.append("- X ");
				else
					sb.append("+ X ");

				continue;
			}
			String num_string = number_k.split("_")[0].trim();
			double num = Double.parseDouble(num_string);
			String num_sign = sings.get(i);
			if (num_sign.equals("minus")) {
				num = -1.0 * num;
				sb.append("- " + num_string + " ");
			} else if (num_sign.equals("plus")) {
				num = 1.0 * num;
				sb.append("+ " + num_string + " ");
			} else if (num_sign.equals("zero")) {
				num = 0.0 * num;
				sb.append("+0*" + num_string + " ");
			}
			numberList.add(num);
		}
		String equation = sb.toString();
		// System.out.println("Predicted equation: " + equation);
		inst.setPredEquation(equation);
		double sum = 0;
		for (Double d : numberList)
			sum += d;
		return factor * sum;

	}

	private static double calculateSolution(LatentAddSubInstance inst, ArrayList<String> numbers,
			ArrayList<String> sings) {
		StringBuilder sb = new StringBuilder();

		int idxForX = numbers.indexOf("X");
		String signForX = sings.get(idxForX);
		double factor = 1.0;
		if (signForX.equals("minus"))
			factor = 1.0;
		else
			factor = -1.0;
		ArrayList<Double> numberList = new ArrayList<>();
		for (int i = 0; i < numbers.size(); i++) {
			String number_k = numbers.get(i);
			if (number_k.equals("X")) {
				if (signForX.equals("minus"))
					sb.append("- X ");
				else
					sb.append("+ X ");

				continue;
			}
			String num_string = number_k.split("_")[0].trim();
			double num = Double.parseDouble(num_string);
			String num_sign = sings.get(i);
			if (num_sign.equals("minus")) {
				num = -1.0 * num;
				sb.append("- " + num_string + " ");
			} else if (num_sign.equals("plus")) {
				num = 1.0 * num;
				sb.append("+ " + num_string + " ");
			} else if (num_sign.equals("zero")) {
				num = 0.0 * num;
				sb.append("+0*" + num_string + " ");
			}
			numberList.add(num);
		}
		String equation = sb.toString();
		// System.out.println("Predicted equation: " + equation);
		inst.setPredEquation(equation);
		double sum = 0;
		for (Double d : numberList)
			sum += d;
		return factor * sum;

	}

}
