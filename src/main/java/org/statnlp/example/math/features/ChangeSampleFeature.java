package org.statnlp.example.math.features;

import java.util.ArrayList;
import java.util.List;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;
import org.statnlp.example.math.type.Tense;

import edu.stanford.nlp.ling.CoreLabel;

public class ChangeSampleFeature {

	public static void addFeatures(ProblemRepresentation probRep) {
		if (probRep.getWholeCue() || probRep.getDifferenceCue())
			return;
		List<Quantity> quantities = probRep.getQuantities();
		int relatedCount = 0;
		List<Quantity> relatedQuantities = new ArrayList<>();
		Quantity unknown = probRep.getUnknownQuantities().get(0);
		for (Quantity q : quantities) {
			if (q.isUnknown())
				continue;
			if (q.isRelatedToQuestion())
				relatedQuantities.add(q);
		}
		if (relatedQuantities.size() != 2)
			return;
		Quantity quantity1 = relatedQuantities.get(0);
		Quantity quantity2 = relatedQuantities.get(1);
		List<CoreLabel> type1 = quantity1.getType();
		List<CoreLabel> type2 = quantity2.getType();
		List<CoreLabel> verb1 = quantity1.getAssociatedEntity("verb");
		List<CoreLabel> verb2 = quantity2.getAssociatedEntity("verb");
		List<CoreLabel> un_verb = unknown.getAssociatedEntity("verb");
		AnnotatedSentence sentence1 = quantity1.getAnnotatedSentence();
		AnnotatedSentence sentence2 = quantity2.getAnnotatedSentence();
		AnnotatedSentence un_sent = unknown.getAnnotatedSentence();
		boolean isTypeMatch = TypeMatcher(type1, type2);
		boolean isTenseMatch = TenseMatcher(verb1, verb2, un_verb, sentence1, sentence2, un_sent);

		if (isTypeMatch && isTenseMatch) {
			probRep.setChangeCue(true);
			Quantity smaller = quantity1.getDoubleValue() > quantity2.getDoubleValue() ? quantity2 : quantity1;
			Quantity larger = quantity1.getDoubleValue() < quantity2.getDoubleValue() ? quantity2 : quantity1;
			smaller.setSmallQuantity(true);
			larger.setLargeQuantity(true);
		}

	}

	private static boolean TypeMatcher(List<CoreLabel> type1, List<CoreLabel> type2) {
		if (type1.size() == 0 || type2.size() == 0)
			return true;

		if (type1.size() == 1 && type2.size() == 1) {
			String word1 = type1.get(0).lemma();
			String word2 = type2.get(0).lemma();
			if (word1.equalsIgnoreCase(word2))
				return true;
		} else if (type1.size() == 2 && type2.size() == 2) {
			String word1 = type1.get(0).lemma();
			String word2 = type2.get(0).lemma();
			String word3 = type1.get(1).lemma();
			String word4 = type2.get(1).lemma();
			if (word1.equalsIgnoreCase(word2) && word3.equalsIgnoreCase(word4))
				return true;
		}
		return false;
	}

	private static boolean TenseMatcher(List<CoreLabel> verb1, List<CoreLabel> verb2, List<CoreLabel> un_verb,
			AnnotatedSentence sentence1, AnnotatedSentence sentence2, AnnotatedSentence un_sent) {
		Tense tense1 = verb1.size() > 0 ? sentence1.getTense(verb1.get(0).index()) : Tense.NULL;
		Tense tense2 = verb2.size() > 0 ? sentence2.getTense(verb2.get(0).index()) : Tense.NULL;
		Tense un_tense = un_verb.size() > 0 ? un_sent.getTense(un_verb.get(0).index()) : Tense.NULL;
		tense1 = isFutureTense(sentence1) ? Tense.FUTURE : tense1;
		tense2 = isFutureTense(sentence2) ? Tense.FUTURE : tense2;
		un_tense = isFutureTense(un_sent) ? Tense.FUTURE : un_tense;
		if (tense2.ordinal() > tense1.ordinal()) {
			return true;
		} else if ((un_tense.ordinal() > tense1.ordinal() || un_tense.ordinal() > tense1.ordinal())
				&& un_tense.equals(Tense.FUTURE) && !tense1.equals(Tense.FUTURE)) {
			if (isIfExist(un_sent) && un_tense.equals(Tense.FUTURE))
				return false;
			return true;
		}
		return false;
	}

	private static boolean isFutureTense(AnnotatedSentence sent) {
		for (CoreLabel token : sent.getTokenSequence()) {
			if (token.originalText().equalsIgnoreCase("will")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isIfExist(AnnotatedSentence sent) {
		for (CoreLabel token : sent.getTokenSequence()) {
			if (token.originalText().equalsIgnoreCase("if")) {
				return true;
			}
		}
		return false;
	}
}
