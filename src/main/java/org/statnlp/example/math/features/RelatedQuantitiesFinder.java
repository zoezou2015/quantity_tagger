package org.statnlp.example.math.features;

import java.util.ArrayList;
import java.util.List;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;
import org.statnlp.example.math.util.ConceptNetCache;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class RelatedQuantitiesFinder {

	public RelatedQuantitiesFinder() {

	}

	public static void FindRelatedQuantites(ProblemRepresentation prep, ArrayList<String> mismatch) {
		List<Quantity> quantities = prep.getQuantities();
		Quantity unknown = prep.getUnknownQuantities().get(0);
		if (quantities.size() == 3) {
			for (Quantity q : quantities)
				q.setRelatedToQustion(true);
			return;
		}
		unknown.setRelatedToQustion(true);
		List<CoreLabel> unknownTypes = unknown.getType();
		boolean mis = false;
		for (Quantity q : quantities) {
			if (q.isUnknown()) {
				q.setRelatedToQustion(true);
				continue;
			}
			AnnotatedSentence sentence = q.getAnnotatedSentence();
			List<CoreLabel> type = q.getType();
			List<CoreLabel> q_verb = q.getAssociatedEntity("verb");
			List<CoreLabel> q_nsubj = q.getAssociatedEntity("nsubj");
			List<CoreLabel> q_prep_in = q.getAssociatedEntity("prep_of");
			List<CoreLabel> un_verb = unknown.getAssociatedEntity("verb");
			List<CoreLabel> un_nsubj = unknown.getAssociatedEntity("nsubj");
			List<CoreLabel> un_prep_in = q.getAssociatedEntity("prep_of");
			boolean match = TypeMatcher(sentence, unknownTypes, type, un_verb, q_verb, un_nsubj, q_nsubj);
			boolean gold_rel = q.isRelatedToQuestion();
			q.setRelatedToQustion(match);
			if (match != gold_rel) {
				// System.out.println(unknown.getStringValue() + " " + unknownTypes);
				// System.out.println("un_verb: " + " " + un_verb);
				// System.out.println("un_nsubj: " + " " + un_nsubj);
				// System.out.println("un_prep_in: " + " " + un_prep_in);
				// String v1 = un_verb.get(0).lemma();
				// String v2 = q_verb.get(0).lemma();
				// boolean relatedToVerb = ConceptNetCache.getInstance().isRelated(v1, v2,
				// "RelatedTo");
				String q_pos = unknownTypes.size() > 0 ? unknownTypes.get(0).get(PartOfSpeechAnnotation.class) : "";
				String q_pos1 = unknownTypes.size() > 1 ? unknownTypes.get(1).get(PartOfSpeechAnnotation.class) : "";

				// System.out.println(q.getStringValue() + " " + type + q_pos + q_pos1);
				// System.out.println("verb: " + " " + q_verb);
				// System.out.println("q_nsubj: " + " " + q_nsubj);
				// System.out.println("q_prep_in: " + " " + q_prep_in);
				// System.out.println(match + " " + gold_rel);
				// System.out.println(prep.getProblemId() + " " + prep.getText());
				mis = true;
			}
		}
		if (mis)
			mismatch.add(prep.getProblemId() + "");
	}

	private static boolean TypeMatcher(AnnotatedSentence sentence, List<CoreLabel> unknownTypes, List<CoreLabel> qType,
			List<CoreLabel> un_verb, List<CoreLabel> q_verb, List<CoreLabel> un_nsubj, List<CoreLabel> q_nsubj) {
		List<CoreLabel> tokens = sentence.getTokenSequence();
		boolean isButExist = false;
		boolean isNotExist = false;
		for (CoreLabel token : tokens) {
			if (sentence.getWord(token).equalsIgnoreCase("but"))
				isButExist = true;
			if (sentence.getWord(token).equals("not") || sentence.getWord(token).equalsIgnoreCase("n't"))
				isNotExist = true;
			if (isButExist && isNotExist)
				return false;
		}
		if (q_verb.get(0).originalText().equals("torn") || q_verb.get(0).originalText().equals("missed")
				|| q_verb.get(0).originalText().equals("cracked") || q_verb.get(0).originalText().equals("paid")) {
			if (unknownTypes.get(0).originalText().equals("money")
					&& (qType.get(0).originalText().equals("$") || qType.get(0).lemma().equals("total"))) {
				return true;
			}
			return false;
		}

		if (unknownTypes.size() == 0)
			return false;
		if (qType.size() == 0) {
			if (q_nsubj.size() > 0 && un_nsubj.size() > 0
					&& q_nsubj.get(0).lemma().equalsIgnoreCase(un_nsubj.get(0).lemma()))
				return true;
			return false;
		}
		if ((unknownTypes.get(0).originalText().equals("money"))
				&& (qType.get(0).originalText().equals("$") || qType.get(0).lemma().equals("dollar"))) {

			if (un_nsubj.size() > 0 && q_nsubj.size() > 0 && isNsubjMatch(un_nsubj, q_nsubj))
				return true;
			// else if (un_nsubj.size() == 0 || q_nsubj.size() == 0)
			// return true;
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

					// if (v1.equals("find") && v2.equals("cracked") || v1.equals("go") &&
					// v2.equals("missed")
					// || v1.equals("weigh") && v2.equals("picked") || v1.equals("had") &&
					// v2.equals("feed")
					// || v1.equals("washing") && v2.equals("had"))
					// return false;
					// if (v1.equals(v2))
					// boolean relatedToVerb = ConceptNetCache.getInstance().isRelated(v1, v2,
					// "RelatedTo");
					// if (!relatedToVerb)
					// return false;
					return true;
					// else {
					//
					// boolean relatedToVerb = ConceptNetCache.getInstance().isRelated(v1, v2,
					// "RelatedTo");
					// if (relatedToVerb)
					// return true;
					// else
					// return false;
					// }
				} else {
					// if (v2.equals(v1))
					// return true;
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
					// if (un_nsubj.size() > 0 && q_nsubj.size() > 0 && !isNsubjMatch(un_nsubj,
					// q_nsubj)) {
					// return false;
					// }
					return true;
				} else if (nnMatch1 && !nnMatch2 && unknownTypes.get(1).get(LemmaAnnotation.class).equals("material")
						|| unknownTypes.get(1).get(LemmaAnnotation.class).equals("ingredient")) {
					return true;
				}

				return false;
			}
			// else if
			// (unknownTypes.get(0).get(PartOfSpeechAnnotation.class).toLowerCase().equalsIgnoreCase("nns"))
			// {
			// boolean nnMatch1 = false;
			// boolean nnMatch2 = false;
			// if
			// (unknownTypes.get(0).get(LemmaAnnotation.class).equals(qType.get(0).get(LemmaAnnotation.class)))
			// nnMatch1 = true;
			// if
			// (unknownTypes.get(1).get(PartOfSpeechAnnotation.class).toLowerCase().startsWith("nn"))
			// {
			// if (unknownTypes.get(1).get(LemmaAnnotation.class)
			// .equals(qType.get(1).get(LemmaAnnotation.class))) {
			// nnMatch2 = true;
			//
			// } else {
			// nnMatch2 = false;
			// }
			// }
			// if (nnMatch1 && nnMatch2) {
			// String v1 = un_verb.size() > 0 ? un_verb.get(0).originalText() : "";
			// String v2 = q_verb.size() > 0 ? q_verb.get(0).originalText() : "";
			// // if (v1.equals("have") && v2.equals("torn") || v1.equals("go") &&
			// // v2.equals("played")
			// // || v1.equals("go") && v2.equals("missed"))
			// // return false;
			// return true;
			// } else
			// return false;
			// }

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

	private static boolean isVerbRelated(List<CoreLabel> un_verb, List<CoreLabel> q_verb) {
		if (un_verb.size() == 0 || q_verb.size() == 0)
			return false;
		String v1 = un_verb.get(0).lemma();
		String v2 = q_verb.get(0).lemma();
		boolean relatedTo = ConceptNetCache.getInstance().isRelated(v1, v2, "verb");
		return relatedTo;
	}
}
