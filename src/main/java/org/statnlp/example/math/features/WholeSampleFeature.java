package org.statnlp.example.math.features;

import java.util.List;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

import edu.stanford.nlp.ling.CoreLabel;

public class WholeSampleFeature {

	public static void addFeatures(ProblemRepresentation probRep) {
		if (probRep.getDifferenceCue())
			return;
		Quantity unknown = probRep.getUnknownQuantities().get(0);
		AnnotatedSentence question = unknown.getAnnotatedSentence();
		List<CoreLabel> tokens = question.getTokenSequence();

		boolean isWholeCue = false;
		boolean isTotal = false;
		boolean isAltogether = false;
		boolean isOverall = false;
		boolean isStartWith = false;
		boolean isInAll = false;
		for (CoreLabel token : tokens) {
			if (token.lemma().equalsIgnoreCase("total")) {
				isTotal = true;
				isWholeCue = true;
			} else if (token.lemma().equalsIgnoreCase("altogether")) {
				isAltogether = true;
				isWholeCue = true;
			} else if (token.lemma().equalsIgnoreCase("overall")) {
				isOverall = true;
				isWholeCue = true;
			} else if (token.lemma().equalsIgnoreCase("together")) {
				isTotal = true;
				isWholeCue = true;
			} else if (token.lemma().equalsIgnoreCase("start") && question.getWord(token.index() + 1).equals("with")) {
				isStartWith = true;
				isWholeCue = true;
			} else if (token.lemma().equalsIgnoreCase("in") && question.getWord(token.index() + 1).equals("all")) {
				isInAll = true;
				isWholeCue = true;
			}
		}
		if (isWholeCue) {
			probRep.setWholeCue(true);
		}
	}
}
