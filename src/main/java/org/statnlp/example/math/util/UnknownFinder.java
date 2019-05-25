package org.statnlp.example.math.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

import edu.stanford.nlp.ling.CoreLabel;

/**
 * This class is to find the unknowns in the problems
 * 
 * @author 1001937
 *
 */
public class UnknownFinder {
	private TypeDetecter typeDetecter;
	private AssociatedWordFiner associatedWordFinder;

	public UnknownFinder() {
		this.typeDetecter = new TypeDetecter();
		this.associatedWordFinder = new AssociatedWordFiner();
	}

	/**
	 * It takes a problem containing single or multiple questions For each question
	 * it finds out
	 * 
	 * @param p
	 *            the input problem
	 */
	public void findUnknowns(ProblemRepresentation prep) {
		ArrayList<AnnotatedSentence> sentences = prep.getAnnotatedSentences();
		int sIdx = 0;
		ArrayList<CoreLabel> prevType = null;
		for (AnnotatedSentence sent : sentences) {
			boolean isQuestion = isAQuestion(sent, sIdx, sentences.size());
			sIdx++;

			/**
			 * For each number in the text, it createds a quantity constant and saves its
			 * attributes and its position in the text
			 */
			for (CoreLabel token : sent.getTokenSequence()) {
				// true if it's a number
				if (sent.getPOS(token).equalsIgnoreCase("CD") && !sent.getNE(token).equalsIgnoreCase("TIME")) {
					/**
					 * Create a constant quantity save its value and its position
					 */

					Quantity quantity = prep.addConstantQuantity(sent.getWord(token), sIdx, token.index());
					quantity.setAnnotatedSentence(sent);
					// add type (closed noun phrase)
					ArrayList<CoreLabel> type = this.typeDetecter.findType(token, sent, prevType, prep);
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

					boolean isPart = false;

					if (sent.hasToken(token.index() + 1) && sent.hasToken(token.index() + 2)) {
						String lemma1 = sent.getLemma(token.index() + 1);
						String lemma2 = sent.getLemma(token.index() + 2);
						if (POSUtil.isVerb(sent.getPOS(token.index() + 1))
								|| POSUtil.isVerb(sent.getPOS(token.index() + 2))) {
							if (lemma1.equalsIgnoreCase("be") || lemma1.equalsIgnoreCase("has")
									|| lemma1.equalsIgnoreCase("have"))
								isPart = true;
							if (lemma2.equalsIgnoreCase("be") || lemma2.equalsIgnoreCase("has")
									|| lemma2.equalsIgnoreCase("have"))
								isPart = true;
						}
						if (lemma1.equalsIgnoreCase("of") && lemma2.equalsIgnoreCase("they")) {
							isPart = true;
						}
					}

					if (isPart && prep.getNumberOfQuantities() >= 2) {
						int id = prep.getNumberOfQuantities();
						Quantity q = prep.getQuantities().get(id - 2);
						quantity.setPart(true);
						quantity.setPartOf(q);
					}

					if (type.isEmpty()) {
						if (prevType == null) {
							int snid = 1;
							for (AnnotatedSentence sn : prep.getAnnotatedSentences()) {
								if (snid > sIdx)
									break;
								int right = -1;
								if (snid == sIdx)
									right = token.index();
								prevType = this.typeDetecter.findObj(sn, right);
								if (!prevType.isEmpty())
									break;
							}
							type.addAll(prevType);
						} else {
							if (prep.getNumberOfQuantities() > 1) {
								if (sent.getLemma(token.index() + 1).equals("of")) {
									boolean added = false;
									for (int i = token.index() + 2; i < sent.getTokenSequence().size(); i++) {
										for (Quantity qu : prep.getQuantities()) {
											if (qu.getType() == null)
												continue;
											for (CoreLabel l : qu.getType()) {
												if (l.lemma().equalsIgnoreCase(sent.getLemma(i))) {
													type.addAll(qu.getType());
													added = true;
													break;
												}
											}
											if (added)
												break;
										}
										if (added)
											break;
									}
									if (added)
										type.addAll(prevType);
								} else {
									type.addAll(prevType);
								}
							} else {
								type.addAll(prevType);
							}
						}
					}
					quantity.setType(type);
					prevType = type;
					// System.out.println("sent: " + sent.getProblemId() + " " +
					// sent.getRawSentence());
					// System.out.println("token: " + sent.getWord(token) + " type: " + type);
				}

			}
			if (isQuestion) {
				sent.setQuestion();
				Quantity quantity = null;
				ArrayList<Integer> typeIds = new ArrayList<>();
				/**
				 * add the unknown assumes there is only one unknown currently TODO: update to
				 * work with multiple unknowns in a single question
				 */
				boolean how = false;
				boolean many = false;
				boolean much = false;
				boolean what = false;
				/***
				 * add what also in condition
				 */
				if (sent.getRawSentence().toLowerCase().contains("how")
						|| sent.getRawSentence().toLowerCase().contains("what")) {
					CoreLabel targetToken = null;
					CoreLabel q = null;
					for (CoreLabel token : sent.getTokenSequence()) {
						if (how && many) {
							break;
						} else if (sent.getLemma(token).equalsIgnoreCase("how")) {
							how = true;
							q = token;
						} else if (sent.getLemma(token).equalsIgnoreCase("many")) {
							many = true;
						} else if (how && !sent.getLemma(token).equalsIgnoreCase("many")) {
							much = true;
						} else if (sent.getLemma(token).equalsIgnoreCase("what")) {
							what = true;
							q = token;
						} else if (what)
							break;
						targetToken = token;
					}
					quantity = prep.addUnknown(sIdx, targetToken.index());
					quantity.setAnnotatedSentence(sent);
					// add type (closest noun phrase)
					quantity.setType(this.typeDetecter.findType(targetToken, sent, prevType, prep));
					ArrayList<CoreLabel> type = quantity.getType();

					typeIds.add(q.index());
					if (how)
						typeIds.add(targetToken.index() - 1);// how
					for (CoreLabel l : type)
						typeIds.add(l.index());

					if (type.isEmpty()) {
						type.addAll(prevType);
					}
					prevType = type;
					// System.out.println("sent: " + sent.getProblemId() + " " +
					// sent.getRawSentence());
					// System.out.println("token: " + sent.getWord(targetToken) + " type: " + type);
				}
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

	}

	private boolean isAQuestion(AnnotatedSentence sentence, int sIdx, int sentLength) {

		if (sentence.getRawSentence().contains("?") && sIdx == sentLength - 1)
			return true;
		return false;
	}
}
