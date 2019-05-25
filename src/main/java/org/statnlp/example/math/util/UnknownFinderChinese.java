package org.statnlp.example.math.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class UnknownFinderChinese {
	private TypeDetecterChinese typeDetecterChinese;
	private AssociatedWordFiner associatedWordFinder;

	public UnknownFinderChinese() {
		this.typeDetecterChinese = new TypeDetecterChinese();
		this.associatedWordFinder = new AssociatedWordFiner();
	}

	/**
	 * It takes a problem containing single or multiple questions For each question
	 * it finds out
	 * 
	 * @param p
	 *            the input problem
	 */
	public void findUnknownsChinese(ProblemRepresentation prep) {
		ArrayList<AnnotatedSentence> sentences = prep.getAnnotatedSentences();
		int sIdx = 0;
		int questionId = prep.getProblemId();

		int sentLen = sentences.size();
		ArrayList<CoreLabel> prevType = null;
		String prevword = null;
		ArrayList<String> numberes = prep.getNumbers();
		HashMap<String, Integer> num2pos = prep.getNum2pos();
		int wordIdx;
		ArrayList<Integer> takenPosition = new ArrayList<>();
		for (AnnotatedSentence sent : sentences) {

			wordIdx = 0;
			boolean isQuestion = isAQuestion(sent, sIdx, sentences.size());
			sIdx++;

			/**
			 * For each number in the text, it createds a quantity constant and saves its
			 * attributes and its position in the text
			 */
			for (CoreLabel token : sent.getTokenSequence()) {
				// true if it's a number
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				String word1 = token.get(TextAnnotation.class);
				String pos1 = token.get(PartOfSpeechAnnotation.class);
				String ne1 = token.get(NamedEntityTagAnnotation.class);
				// System.out.println(word + "\t" + pos + "\t" + ne);
				// System.out.println(word1 + "\t" + pos1 + "\t" + ne1);
				if (!word.equals(word1) || !pos.equals(pos1) || !ne.equals(ne1))
					throw new RuntimeException();

				if (MathUtility.isNumber(token.originalText()) && numberes.contains(word)
						|| (questionId == 565 && word.equals("108"))) {

					/**
					 * Create a constant quantity save its value and its position
					 */
					if ((questionId == 101 || questionId == 255) && sent.getWord(token.index() - 1).equals("+"))
						continue;

					// System.out.println("is a number: " + word);
					Quantity quantity;
					if (questionId == 565 && word.equals("108")) {
						quantity = prep.addConstantQuantity("10895", sIdx, token.index());
					} else {
						quantity = prep.addConstantQuantity(sent.getWord(token), sIdx, token.index());
					}
					ArrayList<CoreLabel> contextSegment = this.findQuantitySpan(sent, wordIdx);
					quantity.setContextSegment(contextSegment);
					quantity.setAnnotatedSentence(sent);
					// add type (closed noun phrase)
					ArrayList<CoreLabel> type = this.typeDetecterChinese.findType(token, sent, prevType, prep);
					/**
					 * add associated verb
					 * 
					 */
					ArrayList<Integer> typeIds = new ArrayList<>();
					typeIds.add(token.index());
					for (CoreLabel l : type)
						typeIds.add(l.index());

					Set<Integer> verb = this.associatedWordFinder.findAssociatedWord(typeIds, sent, "vb");
					Set<Integer> nsubj = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"nsubj");
					Set<Integer> iobj = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"iobj");
					Set<Integer> prep_of = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"prep_of");
					Set<Integer> prep_to = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"prep_to");
					Set<Integer> nmod = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"nmod");
					Set<Integer> dobj = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"dobj");
					Set<Integer> tmod = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"tmod");
					Set<Integer> amod = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"amod");
					Set<Integer> xcomp = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"xcomp");
					Set<Integer> dep = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "dep");

					Set<Integer> union = new HashSet<>();
					union.addAll(typeIds);
					union.addAll(prep_of);

					Set<Integer> prep_in = this.associatedWordFinder.findAssociatedWordWithRel(verb,
							new ArrayList<>(union), sent, "prep_in");
					Set<Integer> ccomp = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
							"ccomp");
					union.clear();
					union.addAll(typeIds);
					union.addAll(ccomp);
					Set<Integer> advmod = this.associatedWordFinder.findAssociatedWordWithRel(verb,
							new ArrayList<>(union), sent, "advmod");
					Set<Integer> prep_in_amod = this.associatedWordFinder
							.findAssociatedWordWithRel(new HashSet<Integer>(), new ArrayList<>(prep_in), sent, "amod");
					quantity.setContext("verb", verb, sent);
					quantity.setContext("amod", amod, sent);
					quantity.setContext("nsubj", nsubj, sent);
					quantity.setContext("tmod", tmod, sent);
					quantity.setContext("iobj", iobj, sent);
					quantity.setContext("dobj", dobj, sent);
					quantity.setContext("prep_of", prep_of, sent);
					quantity.setContext("prep_in", prep_in, sent);
					quantity.setContext("prep_to", prep_to, sent);
					quantity.setContext("ccomp", ccomp, sent);
					quantity.setContext("advmod", advmod, sent);
					quantity.setContext("nmod", nmod, sent);
					quantity.setContext("prep_in_amod", prep_in_amod, sent);
					quantity.setContext("xcomp", xcomp, sent);
					quantity.setContext("dep", dep, sent);

					quantity.setType(type);
					prevType = type;
					// System.out.println("sent: " + sent.getProblemId() + " " +
					// sent.getRawSentence());
					// System.out.println("token: " + sent.getWord(token) + " type: " + type);
				}
				wordIdx++;
			}
			if (isQuestion) {
				sent.setQuestion();
				Quantity quantity = null;
				ArrayList<Integer> typeIds = new ArrayList<>();
				List<CoreLabel> tokenSequence = sent.getTokenSequence();
				ArrayList<CoreLabel> questionPart = new ArrayList<>();
				int t = tokenSequence.size() - 1;
				for (; t >= 0; t--) {
					if (tokenSequence.get(t).originalText().equals("，"))
						break;
					questionPart.add(0, tokenSequence.get(t));

				}
				CoreLabel targetToken = tokenSequence.get(t + 1);
				quantity = prep.addUnknown(sIdx, targetToken.index());
				quantity.setAnnotatedSentence(sent);
				quantity.setQuestionPart(questionPart);
				quantity.setType(this.typeDetecterChinese.findType(targetToken, sent, prevType, prep));
				ArrayList<CoreLabel> type = quantity.getType();

				// add type (closest noun phrase)
				for (CoreLabel l : type)
					typeIds.add(l.index());

				if (type.isEmpty()) {
					type.addAll(prevType);
				}
				prevType = type;
				/**
				 * add the unknown assumes there is only one unknown currently TODO: update to
				 * work with multiple unknowns in a single question
				 */
				Set<Integer> verb = this.associatedWordFinder.findAssociatedWordForQuestion(typeIds, sent, "vb");
				Set<Integer> iobj = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "iobj");
				Set<Integer> dobj = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "dobj");
				Set<Integer> nsubj = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "nsubj");
				Set<Integer> prep_of = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
						"prep_of");
				Set<Integer> prep_to = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent,
						"prep_to");
				Set<Integer> nmod = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "nmod");
				Set<Integer> tmod = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "tmod");
				Set<Integer> amod = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "amod");
				Set<Integer> dep = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "dep");

				Set<Integer> union = new HashSet<>();
				union.addAll(typeIds);
				union.addAll(prep_of);
				Set<Integer> prep_in = this.associatedWordFinder.findAssociatedWordWithRel(verb, new ArrayList<>(union),
						sent, "prep_in");
				Set<Integer> ccomp = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "ccomp");
				Set<Integer> xcomp = this.associatedWordFinder.findAssociatedWordWithRel(verb, typeIds, sent, "xcomp");
				union.clear();
				union.addAll(typeIds);
				union.addAll(ccomp);
				Set<Integer> advmod = this.associatedWordFinder.findAssociatedWordWithRel(verb, new ArrayList<>(union),
						sent, "advmod");
				Set<Integer> prep_in_amod = this.associatedWordFinder.findAssociatedWordWithRel(new HashSet<Integer>(),
						new ArrayList<>(prep_in), sent, "amod");
				quantity.setContext("verb", verb, sent);
				quantity.setContext("nsubj", nsubj, sent);
				quantity.setContext("amod", amod, sent);
				quantity.setContext("iobj", iobj, sent);
				quantity.setContext("prep_of", prep_of, sent);
				quantity.setContext("prep_in", prep_in, sent);
				quantity.setContext("prep_to", prep_to, sent);
				quantity.setContext("ccomp", ccomp, sent);
				quantity.setContext("advmod", advmod, sent);
				quantity.setContext("nmod", nmod, sent);
				quantity.setContext("dobj", dobj, sent);
				quantity.setContext("tmod", tmod, sent);
				quantity.setContext("prep_in_amod", prep_in_amod, sent);
				quantity.setContext("xcomp", xcomp, sent);
				quantity.setContext("dep", dep, sent);

				// System.out.println("Quantity"+ quantity.getValue()+":"
				// + " "+ quantity.getType());
			}

		}

		List<Quantity> quantities = prep.getQuantities();
		Quantity unknown = prep.getUnknownQuantities().get(0);
		if (quantities.size() <= 3) {
			for (Quantity q : quantities)
				q.setRelatedToQustion(true);
			return;
		}
		unknown.setRelatedToQustion(true);

	}

	/**
	 * find the subsentence where the quantity is
	 * 
	 * @return
	 */
	private ArrayList<CoreLabel> findQuantitySpan(AnnotatedSentence sentence, int wordIdx) {
		ArrayList<CoreLabel> span = new ArrayList<>();
		// find the span part before the quantity
		int pu_right_before_quantity = -1;
		int pu_right_after_quantity = -1;
		List<CoreLabel> tokens = sentence.getTokenSequence();
		int tokenIdx = 0;
		boolean isQuantity = false;
		for (CoreLabel token : tokens) {
			if (tokenIdx == wordIdx)
				isQuantity = true;
			if (token.get(CoreAnnotations.PartOfSpeechAnnotation.class).equalsIgnoreCase("pu")
					&& (token.originalText().equals("。") || token.originalText().equals("，")
							|| token.originalText().equals("？")))
				if (tokenIdx < wordIdx && !isQuantity)
					pu_right_before_quantity = tokenIdx;
				else if (tokenIdx > wordIdx && isQuantity) {
					pu_right_after_quantity = tokenIdx;
					break;
				}
			tokenIdx++;
		}
		if (pu_right_before_quantity == -1)
			pu_right_before_quantity = 0;
		for (int i = pu_right_before_quantity; i <= pu_right_after_quantity; i++) {
			// if (i == wordIdx)
			// continue;
			span.add(tokens.get(i));
		}
		return span;
	}

	private boolean isAQuestion(AnnotatedSentence sentence, int sIdx, int sentLength) {
		// if (sentence.getRawSentence().contains("？") && sIdx == sentLength - 1)
		if (sIdx == sentLength - 1)
			return true;
		return false;
	}

}
