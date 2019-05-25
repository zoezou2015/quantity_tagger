package org.statnlp.example.math.features;

import java.util.ArrayList;
import java.util.List;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ComparisonSample;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class DifferenceSampleFeature {

	public static void addFeaturesForTrainingData(List<Quantity> quantities, ProblemRepresentation probRep,
			boolean isTraining) {
		if (!isTraining)
			return;
		Quantity smallerQuantity;
		Quantity largerQuantity;
		Quantity unknown = null;
		for (Quantity q : quantities)
			if (q.isUnknown())
				unknown = q;
		smallerQuantity = FindTheSmallerQuatity(quantities);
		largerQuantity = FindTheLargerQuatity(quantities);
		largerQuantity.setLargeQuantity(true);
		smallerQuantity.setSmallQuantity(true);
		ComparisonSample sample = new ComparisonSample(largerQuantity, smallerQuantity, unknown);
		probRep.setDifferenceCue(true);
		probRep.setComparisonSample(sample);
		System.out.println("id: " + probRep.getProblemId());
	}

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
		// for unknown variables
		{

			boolean start = false;
			boolean isTotal = false;
			for (CoreLabel token : tokens) {
				if (!start && unknownSent.getLemma(token).equalsIgnoreCase("total")
						&& !unknownSent.getWord(token.index() - 1).equalsIgnoreCase("in")) {
					isTotal = true;
				}
				if (!start && token.lemma().equalsIgnoreCase("how")) {
					start = true;
					if (start && isTotal) {
						isDifferenceUnknown = true;
					}
				}

				if (start) {
					if (unknownSent.getPOS(token).equalsIgnoreCase("jjr")
							&& !unknownSent.getWord(token).equals("leftover")) {
						isDifferenceUnknown = true;
					} else if (unknownSent.getPOS(token).equalsIgnoreCase("rbr")) {
						isDifferenceUnknown = true;
					} else if (unknownSent.getLemma(token).equalsIgnoreCase("extra")) {
						isDifferenceUnknown = true;
					} else if (unknownSent.getLemma(token).equalsIgnoreCase("than")) {
						isDifferenceUnknown = true;
					}

					if (isDifferenceUnknown)
						break;
				}
			}
		}
		for (Quantity q : quantities) {
			int sentIdx = q.getSentenceId();
			AnnotatedSentence sentence = annotatedSentences.get(sentIdx - 1);
			List<CoreLabel> sent_token = sentence.getTokenSequence();
			boolean containLess = false;
			boolean containThan = false;
			boolean containTotal = false;
			boolean isNotWholeCue = true;
			for (int j = 1; j <= sent_token.size(); j++) {
				if (sentence.getWord(j).equalsIgnoreCase("less")) {
					containLess = true;
				} else if (sentence.getLemma(j).equalsIgnoreCase("than")) {
					containThan = true;
				} else if (!q.isUnknown() && sentence.getLemma(j).equalsIgnoreCase("total")) {
					containTotal = true;
					break;
				}
				if (containLess && containThan) {
					NonDifferenceUnknown = true;
					totalDifference = true;
					break;
				}
			}
			if (containTotal) {
				tokens = unknownSent.getTokenSequence();
				for (CoreLabel token : tokens) {
					if (unknownSent.getLemma(token).equalsIgnoreCase("total")
							&& unknownSent.getWord(token.index() - 1).equalsIgnoreCase("in")) {
						isNotWholeCue = false;

						break;
					} else if (unknownSent.getLemma(token).equalsIgnoreCase("all")
							&& unknownSent.getWord(token.index() - 1).equalsIgnoreCase("in")) {
						isNotWholeCue = false;
						break;
					}
				}
			}
			if (containTotal && isNotWholeCue) {
				NonDifferenceUnknown = true;
				// System.out.println("total id: " + probRep.getProblemId());
				break;
			}
		}

		if (!isDifferenceUnknown && !NonDifferenceUnknown) {
			probRep.setDifferenceCue(false);
			return;
		} else if (isDifferenceUnknown || NonDifferenceUnknown) {
			// System.out.println("id: " + probRep.getProblemId());
			quantities = probRep.getQuantities();
			if (quantities.size() > 3) {
				if (!totalDifference)
					FilterOutIrrelevantQuantity(annotatedSentences, quantities, unknown);
			}
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

	private static void FilterOutIrrelevantQuantity(ArrayList<AnnotatedSentence> annotatedSentences,
			List<Quantity> quantities, Quantity unknown) {
		if (quantities.size() == 3) {
			for (Quantity q : quantities)
				q.setRelatedToQustion(true);
			return;
		}
		unknown.setRelatedToQustion(true);
		List<CoreLabel> unknownTypes = unknown.getType();
		List<CoreLabel> un_verb = unknown.getAssociatedEntity("verb");
		List<CoreLabel> un_nsubj = unknown.getAssociatedEntity("nsubj");

		for (Quantity q : quantities) {
			int sentIdx = q.getSentenceId();
			AnnotatedSentence sentence = annotatedSentences.get(sentIdx - 1);
			if (q.isUnknown()) {
				q.setRelatedToQustion(true);
				continue;
			}
			List<CoreLabel> type = q.getType();
			List<CoreLabel> q_verb = q.getAssociatedEntity("verb");
			List<CoreLabel> q_nsubj = q.getAssociatedEntity("nsubj");
			boolean match = TypeMatcher(q, sentence, unknownTypes, type, un_verb, q_verb, un_nsubj, q_nsubj);
			boolean gold_rel = q.isRelatedToQuestion();
			if (match != gold_rel) {
				// System.out.println(unknown.getStringValue() + " " + unknownTypes);
				// System.out.println("un_verb: " + " " + un_verb);
				// System.out.println("un_nsubj: " + " " + un_nsubj);
				// String v1 = un_verb.get(0).lemma();
				// String v2 = q_verb.get(0).lemma();
				// // boolean relatedToVerb = ConceptNetCache.getInstance().isRelated(v1, v2,
				// // "RelatedTo");
				// String q_pos = unknownTypes.size() > 0 ?
				// unknownTypes.get(0).get(PartOfSpeechAnnotation.class) : "";
				// String q_pos1 = unknownTypes.size() > 1 ?
				// unknownTypes.get(1).get(PartOfSpeechAnnotation.class) : "";
				// System.out.println(q.getStringValue() + " " + type + q_pos + q_pos1);
				// System.out.println("verb: " + " " + q_verb);
				// System.out.println("q_nsubj: " + " " + q_nsubj);
				// System.out.println(match + " " + gold_rel);
			}
			q.setRelatedToQustion(match);
		}
	}

	private static boolean TypeMatcher(Quantity q, AnnotatedSentence sentence, List<CoreLabel> unknownTypes,
			List<CoreLabel> qType, List<CoreLabel> un_verb, List<CoreLabel> q_verb, List<CoreLabel> un_nsubj,
			List<CoreLabel> q_nsubj) {

		if (qType.get(0).originalText().equalsIgnoreCase("cat")) {

			if (sentence.getToken(q.getTokenId() + 1).originalText().equals("can")
					&& unknownTypes.get(1).originalText().equals("food")) {
				return true;
			}
		}
		if (unknownTypes.size() == 0)
			return false;
		if (qType.size() == 0)
			return false;
		if ((unknownTypes.get(0).originalText().equals("money"))
				&& (qType.get(0).originalText().equals("$") || qType.get(0).lemma().equals("dollar"))) {
			if (un_nsubj.size() > 0 && q_nsubj.size() > 0 && isNsubjMatch(un_nsubj, q_nsubj))
				return true;
			return false;
		}

		boolean adjMatch = false;
		boolean prevIsAdj = false;
		boolean nnMatch = false;
		if (unknownTypes.size() == 1) {
			if (qType.size() == 1) {
				String v1 = un_verb.size() > 0 ? un_verb.get(0).lemma() : "";
				String v2 = q_verb.size() > 0 ? q_verb.get(0).lemma() : "";
				if (unknownTypes.get(0).get(LemmaAnnotation.class).equals(qType.get(0).get(LemmaAnnotation.class))) {
					return true;
				} else {
				}
			} else {
				for (CoreLabel q_l : qType) {
					if (unknownTypes.get(0).get(LemmaAnnotation.class).equals(q_l.get(LemmaAnnotation.class))) {
						return true;
					}
				}
			}
		}
		if (unknownTypes.size() == 2 && qType.size() == 1) {
			boolean match1 = false;
			boolean match2 = false;
			if (unknownTypes.get(0).get(PartOfSpeechAnnotation.class).toLowerCase().equalsIgnoreCase("jj")
					&& unknownTypes.get(0).lemma().equals(qType.get(0).lemma())) {
				return true;
			}

		}

		if (unknownTypes.size() == 2 && qType.size() == 2) {

			if (unknownTypes.get(0).get(PartOfSpeechAnnotation.class).toLowerCase().equalsIgnoreCase("jj")) {
				if (unknownTypes.get(0).get(LemmaAnnotation.class).equals(qType.get(0).get(LemmaAnnotation.class)))
					adjMatch = true;
				if (unknownTypes.get(1).get(PartOfSpeechAnnotation.class).toLowerCase().startsWith("nn")) {
					if (unknownTypes.get(1).get(LemmaAnnotation.class).equals(qType.get(1).get(LemmaAnnotation.class)))
						nnMatch = true;
				}
				if (adjMatch && nnMatch)
					return true;
				else
					return false;
			} else if (unknownTypes.get(0).get(PartOfSpeechAnnotation.class).toLowerCase().startsWith("nn")) {
				boolean nnMatch1 = false;
				boolean nnMatch2 = false;
				if (unknownTypes.get(0).get(LemmaAnnotation.class).equals(qType.get(0).get(LemmaAnnotation.class)))
					nnMatch1 = true;
				if (unknownTypes.get(1).get(PartOfSpeechAnnotation.class).toLowerCase().startsWith("nn")) {
					if (unknownTypes.get(1).get(LemmaAnnotation.class)
							.equals(qType.get(1).get(LemmaAnnotation.class))) {
						nnMatch2 = true;

					} else {
						nnMatch2 = false;
					}
				}
				if (nnMatch1 && nnMatch2) {
					String v1 = un_verb.size() > 0 ? un_verb.get(0).originalText() : "";
					String v2 = q_verb.size() > 0 ? q_verb.get(0).originalText() : "";
					return true;
				} else if (nnMatch1 && !nnMatch2 && unknownTypes.get(1).get(LemmaAnnotation.class).equals("material")
						|| unknownTypes.get(1).get(LemmaAnnotation.class).equals("ingredient")) {
					return true;
				}

				return false;
			}
		}

		for (CoreLabel un_l : unknownTypes) {
			String un_pos = un_l.get(PartOfSpeechAnnotation.class);
			if (un_pos.toLowerCase().startsWith("jj")) {
				prevIsAdj = true;
				for (CoreLabel q_l : qType) {
					String q_pos = q_l.get(PartOfSpeechAnnotation.class);
					if (q_pos.toLowerCase().startsWith("jj")
							&& un_l.get(LemmaAnnotation.class).equals(q_l.get(LemmaAnnotation.class)))
						adjMatch = true;
				}
			}
			if (un_pos.toLowerCase().startsWith("nn")) {
				for (CoreLabel q_l : qType) {
					String q_pos = q_l.get(PartOfSpeechAnnotation.class);
					if (q_pos.toLowerCase().startsWith("nn")
							&& un_l.get(LemmaAnnotation.class).equals(q_l.get(LemmaAnnotation.class))) {
						if (prevIsAdj && adjMatch)
							return true;
						else if (prevIsAdj && !adjMatch)
							return true;
						else
							return true;
					}
				}
			}

		}

		return false;
	}

	private static boolean isNsubjMatch(List<CoreLabel> un_nsubj, List<CoreLabel> q_nsubj) {
		if (un_nsubj.size() == 0 || q_nsubj.size() == 0)
			return false;
		if (un_nsubj.get(0).originalText().equals(q_nsubj.get(0).originalText()))
			return true;
		// System.out.println("ne:" +
		// un_nsubj.get(0).get(NamedEntityTagAnnotation.class));
		// System.out.println("pos:" +
		// q_nsubj.get(0).get(PartOfSpeechAnnotation.class));

		if (un_nsubj.get(0).get(NamedEntityTagAnnotation.class).equals("PERSON")
				&& q_nsubj.get(0).get(PartOfSpeechAnnotation.class).equals("PRP"))
			return true;
		if (un_nsubj.get(0).originalText().equals(q_nsubj.get(0).originalText()))
			return true;
		return false;
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

	public static boolean checkComparisonTemplate(ArrayList<String> signs, String equations) {
		int relatedNumberCount = 0;

		for (String s : signs) {
			if (s.equals("zero")) {
				continue;
			} else {
				relatedNumberCount++;
			}
		}
		if (relatedNumberCount > 3)
			return false;
		if (equations.contains(" - ")) {
			int pos_equal = equations.indexOf('=');
			int pos_minus = equations.indexOf('-');
			if (pos_minus > pos_equal)
				return true;
		}

		return false;
	}
}
