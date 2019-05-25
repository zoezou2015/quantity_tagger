package org.statnlp.example.math.features;

import java.util.ArrayList;
import java.util.List;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ComparisonSample;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

import edu.stanford.nlp.ling.CoreLabel;

public class DifferenceSampleFeatureChinese {
	public static void addFeatures(ProblemRepresentation probRep) {
		boolean isDifferenceUnknown = false;
		boolean NonDifferenceUnknown = false;
		boolean totalDifference = false;
		Quantity smallerQuantity;
		Quantity largerQuantity;
		ArrayList<AnnotatedSentence> annotatedSentences = probRep.getAnnotatedSentences();
		List<Quantity> quantities = probRep.getQuantities();
		Quantity unknown = probRep.getUnknownQuantities().get(0);
		AnnotatedSentence unknownSent = annotatedSentences.get(annotatedSentences.size() - 1);
		List<CoreLabel> tokens = unknownSent.getTokenSequence();
		String problemText = probRep.getText();
		// for unknown variables
		if (quantities.size() != 3)
			return;
		if (problemText.contains("差 =") || problemText.contains("出生 年份 =") || problemText.contains("相差")|| problemText.contains("加数 ="))
			isDifferenceUnknown = true;
		// {
		//
		// boolean start = false;
		// boolean isTotal = false;
		// for (CoreLabel token : tokens) {
		// if (!start && unknownSent.getLemma(token).equalsIgnoreCase("total")
		// && !unknownSent.getWord(token.index() - 1).equalsIgnoreCase("in")) {
		// isTotal = true;
		// }
		// if (!start && token.lemma().equalsIgnoreCase("how")) {
		// start = true;
		// if (start && isTotal) {
		// isDifferenceUnknown = true;
		// }
		// }
		//
		// if (start) {
		// if (unknownSent.getPOS(token).equalsIgnoreCase("jjr")
		// && !unknownSent.getWord(token).equals("leftover")) {
		// isDifferenceUnknown = true;
		// } else if (unknownSent.getPOS(token).equalsIgnoreCase("rbr")) {
		// isDifferenceUnknown = true;
		// } else if (unknownSent.getLemma(token).equalsIgnoreCase("extra")) {
		// isDifferenceUnknown = true;
		// } else if (unknownSent.getLemma(token).equalsIgnoreCase("than")) {
		// isDifferenceUnknown = true;
		// }
		//
		// if (isDifferenceUnknown)
		// break;
		// }
		// }
		// }
		// for (Quantity q : quantities) {
		// int sentIdx = q.getSentenceId();
		// AnnotatedSentence sentence = annotatedSentences.get(sentIdx - 1);
		// List<CoreLabel> sent_token = sentence.getTokenSequence();
		// boolean containLess = false;
		// boolean containThan = false;
		// boolean containTotal = false;
		// boolean isNotWholeCue = true;
		// for (int j = 1; j <= sent_token.size(); j++) {
		// if (sentence.getWord(j).equalsIgnoreCase("less")) {
		// containLess = true;
		// } else if (sentence.getLemma(j).equalsIgnoreCase("than")) {
		// containThan = true;
		// } else if (!q.isUnknown() && sentence.getLemma(j).equalsIgnoreCase("total"))
		// {
		// containTotal = true;
		// break;
		// }
		// if (containLess && containThan) {
		// NonDifferenceUnknown = true;
		// totalDifference = true;
		// break;
		// }
		// }
		// if (containTotal) {
		// tokens = unknownSent.getTokenSequence();
		// for (CoreLabel token : tokens) {
		// if (unknownSent.getLemma(token).equalsIgnoreCase("total")
		// && unknownSent.getWord(token.index() - 1).equalsIgnoreCase("in")) {
		// isNotWholeCue = false;
		//
		// break;
		// } else if (unknownSent.getLemma(token).equalsIgnoreCase("all")
		// && unknownSent.getWord(token.index() - 1).equalsIgnoreCase("in")) {
		// isNotWholeCue = false;
		// break;
		// }
		// }
		// }
		// if (containTotal && isNotWholeCue) {
		// NonDifferenceUnknown = true;
		// // System.out.println("total id: " + probRep.getProblemId());
		// break;
		// }
		// }

		if (!isDifferenceUnknown && !NonDifferenceUnknown) {
			probRep.setDifferenceCue(false);
			return;
		} else if (isDifferenceUnknown || NonDifferenceUnknown) {
			// System.out.println("id: " + probRep.getProblemId());
			quantities = probRep.getQuantities();
			// if (quantities.size() > 3) {
			// if (!totalDifference)
			// FilterOutIrrelevantQuantity(annotatedSentences, quantities, unknown);
			// }
			smallerQuantity = FindTheSmallerQuatity(quantities);
			largerQuantity = FindTheLargerQuatity(quantities);
			largerQuantity.setLargeQuantity(true);
			smallerQuantity.setSmallQuantity(true);
			ComparisonSample sample = new ComparisonSample(largerQuantity, smallerQuantity, unknown);
			probRep.setDifferenceCue(true);
			probRep.setComparisonSample(sample);
		} else {
			throw new RuntimeException("This is not valid!");
		}

	}

	private static Quantity FindTheSmallerQuatity(List<Quantity> quantities) {
		Quantity smallest = null;
		double smallestValue = 10000000;
		for (Quantity q : quantities) {
			if (q.isUnknown()) {
				continue;
			}
			if (!q.isRelatedToQuestion()) {
				continue;
			}
			if (q.getDoubleValue() < smallestValue) {
				smallestValue = q.getDoubleValue();
				smallest = q;
			}
		}
		return smallest;
	}

	private static Quantity FindTheLargerQuatity(List<Quantity> quantities) {
		Quantity large = null;
		double largeValue = -1;
		for (Quantity q : quantities) {
			if (q.isUnknown()) {
				continue;
			}
			if (!q.isRelatedToQuestion()) {
				continue;
			}

			if (q.getDoubleValue() > largeValue) {
				largeValue = q.getDoubleValue();
				large = q;
			}
		}
		return large;
	}
}
