package org.statnlp.example.math.util;

import java.util.ArrayList;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;

import edu.stanford.nlp.ling.CoreLabel;

public class TypeDetecter {
	private WordNetHelper wnh = WordNetHelper.getInstance();

	public ArrayList<CoreLabel> findType(CoreLabel token, AnnotatedSentence sent, ArrayList<CoreLabel> prevType,
			ProblemRepresentation prep) {
		/**
		 * Simple deterministic algorithm
		 */
		boolean stop = false;
		boolean ccFound = false;
		boolean hasNp = false;
		boolean lookForAdjective = false;
		boolean changedAdj = false;
		boolean isOfAfter = false;
		ArrayList<CoreLabel> type = new ArrayList<>();
		try {
			if (sent.isQuestion()) {

				CoreLabel newToken = null;
				boolean how = false;
				boolean many = false;
				boolean much = false;
				boolean what = false;
				/***
				 * add what also in condition
				 */
				if (sent.getRawSentence().toLowerCase().contains("how")
						|| sent.getRawSentence().toLowerCase().contains("what")) {
					for (CoreLabel s_token : sent.getTokenSequence()) {
						if (sent.getLemma(s_token).equalsIgnoreCase("how")) {
							how = true;
						} else if (sent.getLemma(s_token).equalsIgnoreCase("many")) {
							many = true;
							newToken = s_token;
							break;
						} else if (how && !sent.getLemma(s_token).equalsIgnoreCase("many")) {
							much = true;
							newToken = s_token;
							break;
						} else if (sent.getLemma(s_token).equalsIgnoreCase("what")) {
							what = true;
							newToken = s_token;
							break;
						}
					}
					type.addAll(this.findTypeForQuestion(newToken, sent, prevType, prep));
					return type;
				}
			}

			String prev = "";
			int tId = token.index() + 1;
			while (!stop && sent.hasToken(tId)) {
				String pos = sent.getPOS(tId).toLowerCase();

				if (POSUtil.isNoun(pos)) {
					if (!ccFound) {
						if (!hasNp || (hasNp && pos.equalsIgnoreCase("nns")))
							prev = sent.getLemma(tId);
						type.add(sent.getToken(tId));
						hasNp = true;
					} else {
						if (ccFound && lookForAdjective) {
							stop = true;// case like 2 turnip and 3 banana
							continue;
						}
						// found a plural noun
						if (pos.startsWith("nns") || (pos.startsWith("nn") && changedAdj)) {
							// if not present in the type add and stop
							boolean match = false;
							for (CoreLabel l : type) {
								if (sent.getLemma(l.index()).equalsIgnoreCase(sent.getLemma(tId))) {
									match = true;
									break;
								}
							}

							if (!match) {
								stop = false;
								prev = sent.getLemma(tId);
								type.add(sent.getToken(tId));
							}
						}
					}
					if (isOfAfter) {
						if (pos.startsWith("nns") || (pos.startsWith("nn") && changedAdj)) {
							// if not present in the type add and stop
							boolean match = false;
							for (CoreLabel l : type) {
								if (sent.getLemma(l.index()).equalsIgnoreCase(sent.getLemma(tId))) {
									match = true;
									break;
								}
							}

							if (!match) {
								stop = false;
								prev = sent.getLemma(tId);
								type.add(sent.getToken(tId));
							}
						}
					}
				} else if (POSUtil.isNonComparativeAdj(pos)) {
					if (hasNp && !wnh.isAntonym(sent.getLemma(tId), prev)) {
						stop = true; // adj after noun "'games last' year"
					} else if (!ccFound) {
						type.add(sent.getToken(tId));
					} else if (ccFound) {
						lookForAdjective = false;// not situations like 20 turnip and 30 watermelon
						changedAdj = true;
					}
				} else if (POSUtil.isCC(pos)) {
					ccFound = true;
					isOfAfter = false;
				} else if (pos.toLowerCase().equalsIgnoreCase("cd") && ccFound) {
					lookForAdjective = true;
					isOfAfter = false;
				} else if (sent.getWord(tId).toLowerCase().equalsIgnoreCase("of") && !ccFound) {
					isOfAfter = true;
					if (sent.hasToken(tId + 1) && sent.getWord(tId + 1).equals("the")) {
						tId++;
					}
					if (sent.hasToken(tId + 1) && sent.getNE(tId + 1).equals("PERSON")) {
						tId++;
					}
					if (sent.hasToken(tId + 1) && sent.getWord(tId + 1).equals("'s")) {
						tId++;
					}
				} else {
					stop = true;
				}
				tId++;
			}

			/*
			 * tId--; String pos = s.getPOS(tId);
			 * if(s.getLemma(tId).equalsIgnoreCase("be")&& !lookForAdjective){ tId++;
			 * if(s.hasToken(tId)){ pos = s.getPOS(tId); if(POSUtil.isVerb(pos)){
			 * if(wnh.isAdjective(s.getLemma(tId))) type.add(s.getToken(tId)); // were
			 * broken } } }
			 */
			if (type.isEmpty()) {
				// look for $
				int start = token.index() - 1;
				if (sent.hasToken(start) && sent.getLemma(start).equalsIgnoreCase("$")) {
					type.add(sent.getToken(start));
				} else if (sent.hasToken(start - 1) && sent.getLemma(start - 1).equalsIgnoreCase("$")) {
					type.add(sent.getToken(start - 1));
				}

			}

		} catch (RuntimeException e) {
			throw e;
		}
		/** end while loop */
		// System.out.println("sent: " + sent.getProblemId() + " " +
		// sent.getRawSentence());
		// System.out.println("token: " + sent.getWord(token) + " type: " + type);

		return type;
	}

	public ArrayList<CoreLabel> findTypeForQuestion(CoreLabel token, AnnotatedSentence sent,
			ArrayList<CoreLabel> prevType, ProblemRepresentation prep) {
		/**
		 * Simple deterministic algorithm
		 */
		boolean stop = false;
		boolean ccFound = false;
		boolean hasNp = false;
		boolean lookForAdjective = false;
		boolean changedAdj = false;
		boolean isOfAfter = false;
		ArrayList<CoreLabel> type = new ArrayList<>();
		try {
			String prev = "";
			int tId = token.index() + 1;
			while (!stop && sent.hasToken(tId)) {
				String pos = sent.getPOS(tId).toLowerCase();

				if (POSUtil.isNoun(pos)) {
					if (!ccFound) {
						if (!hasNp || (hasNp && pos.equalsIgnoreCase("nns")))
							prev = sent.getLemma(tId);
						type.add(sent.getToken(tId));
						hasNp = true;
					} else {
						if (ccFound && lookForAdjective) {
							stop = true;// case like 2 turnip and 3 banana
							continue;
						}
						// found a plural noun
						if (pos.startsWith("nns") || (pos.startsWith("nn") && changedAdj)) {
							// if not present in the type add and stop
							boolean match = false;
							for (CoreLabel l : type) {
								if (sent.getLemma(l.index()).equalsIgnoreCase(sent.getLemma(tId))) {
									match = true;
									break;
								}
							}

							if (!match) {
								stop = false;
								prev = sent.getLemma(tId);
								type.add(sent.getToken(tId));
							}
						}
					}
					if (isOfAfter) {
						if (pos.startsWith("nns") || (pos.startsWith("nn") && changedAdj)) {
							// if not present in the type add and stop
							boolean match = false;
							for (CoreLabel l : type) {
								if (sent.getLemma(l.index()).equalsIgnoreCase(sent.getLemma(tId))) {
									match = true;
									break;
								}
							}

							if (!match) {
								stop = false;
								prev = sent.getLemma(tId);
								type.add(sent.getToken(tId));
							}
						}
					}
				} else if (POSUtil.isNonComparativeAdj(pos)) {
					if (hasNp && !wnh.isAntonym(sent.getLemma(tId), prev)) {
						stop = true; // adj after noun "'games last' year"
					} else if (!ccFound) {
						type.add(sent.getToken(tId));
					} else if (ccFound) {
						lookForAdjective = false;// not situations like 20 turnip and 30 watermelon
						changedAdj = true;
					}
				} else if (POSUtil.isCC(pos)) {
					ccFound = true;
					isOfAfter = false;
				} else if (pos.toLowerCase().equalsIgnoreCase("cd") && ccFound) {
					lookForAdjective = true;
					isOfAfter = false;
				} else if (sent.getWord(tId).toLowerCase().equalsIgnoreCase("of") && !ccFound) {
					isOfAfter = true;
					if (sent.hasToken(tId + 1) && sent.getWord(tId + 1).equals("the")) {
						tId++;
					}
					if (sent.hasToken(tId + 1) && sent.getNE(tId + 1).equals("PERSON")) {
						tId++;
					}
					if (sent.hasToken(tId + 1) && sent.getWord(tId + 1).equals("'s")) {
						tId++;
					}
				} else {
					stop = true;
				}
				tId++;
			}

			/*
			 * tId--; String pos = s.getPOS(tId);
			 * if(s.getLemma(tId).equalsIgnoreCase("be")&& !lookForAdjective){ tId++;
			 * if(s.hasToken(tId)){ pos = s.getPOS(tId); if(POSUtil.isVerb(pos)){
			 * if(wnh.isAdjective(s.getLemma(tId))) type.add(s.getToken(tId)); // were
			 * broken } } }
			 */
			if (type.isEmpty()) {
				// look for $
				int start = token.index() - 1;
				if (sent.hasToken(start) && sent.getLemma(start).equalsIgnoreCase("$")) {
					type.add(sent.getToken(start));
				} else if (sent.hasToken(start - 1) && sent.getLemma(start - 1).equalsIgnoreCase("$")) {
					type.add(sent.getToken(start - 1));
				}
			}
			if (sent.getRawSentence().contains("money") || sent.getRawSentence().contains("spend")
					|| sent.getRawSentence().contains("spent") || sent.getRawSentence().contains("bill")
					|| sent.getRawSentence().contains("worth") || sent.getRawSentence().contains("cost")) {
				for (CoreLabel label : sent.getTokenSequence())
					if (label.toString().equals("dollars")) {
						type.add(label);
						break;
					} else if (prevType != null && prevType.size() > 0 && (prevType.get(0).originalText().equals("$")
							|| prevType.get(0).originalText().equals("money"))) {
						type.addAll(prevType);
						break;
					} else {
						ArrayList<AnnotatedSentence> annotatedSentences = prep.getAnnotatedSentences();
						for (AnnotatedSentence sentence : annotatedSentences) {
							for (CoreLabel label_s : sentence.getTokenSequence()) {
								if (label_s.lemma().equalsIgnoreCase("$")
										|| label_s.lemma().equalsIgnoreCase("dollar")) {
									type.add(label_s);
									return type;
								}
							}
						}
					}

				// System.out.println("sent: " + sent.getRawSentence());
				// System.out.println("token: " + sent.getWord(token) + " type: " + type);
			}
		} catch (RuntimeException e) {
			throw e;
		}
		/** end while loop */
		// System.out.println("sent: " + sent.getProblemId() + " " +
		// sent.getRawSentence());
		// System.out.println("token: " + sent.getWord(token) + " type: " + type);

		return type;
	}

	public ArrayList<CoreLabel> findObj(AnnotatedSentence sentence, int right) {
		ArrayList<CoreLabel> obj = new ArrayList<>();
		for (CoreLabel token : sentence.getTokenSequence()) {

		}
		return obj;
	}
}
