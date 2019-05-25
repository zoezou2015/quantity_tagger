package org.statnlp.example.math.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;

public class Quantity implements Serializable {
	private static final long serialVersionUID = -6083831531610821067L;
	private String value;
	private ArrayList<CoreLabel> type;
	private int sentenceId;
	private int tokenId;
	private int sentencePos;
	private boolean isUnknown;
	private String unknownId;
	private Map<String, List<CoreLabel>> context;
	private boolean isPart = false;
	private Quantity partOf = null;
	private String sign;
	private boolean isRelatedToQuestion = false;
	private Boolean allMarker = null;
	AnnotatedSentence sentence;
	private boolean differenceSmallerQ = false;
	private boolean differenceLargerQ = false;
	private List<CoreLabel> questionPart;
	private List<CoreLabel> contextSegment;

	public Quantity(String value, int sentenceId, int tokenId) {
		this.value = value;
		this.sentenceId = sentenceId;
		this.tokenId = tokenId;
		this.context = new HashMap<>();
	}

	public int getTokenId() {
		return this.tokenId;
	}

	public int getSentenceId() {
		return this.sentenceId;
	}

	public List<CoreLabel> getQuestionPart() {
		return this.questionPart;
	}

	public void setQuestionPart(List<CoreLabel> questionPart) {
		this.questionPart = questionPart;
	}

	public void setAnnotatedSentence(AnnotatedSentence sentence) {
		this.sentence = sentence;
	}

	public AnnotatedSentence getAnnotatedSentence() {
		return this.sentence;
	}

	public boolean isRelatedToQuestion() {
		return this.isRelatedToQuestion;
	}

	public void setRelatedToQustion(boolean related) {
		this.isRelatedToQuestion = related;
	}

	public void setUnknown() {
		this.isUnknown = true;
	}

	public boolean isUnknown() {
		return this.isUnknown;
	}

	public void setSign(String sign, boolean isTraining) {
		this.sign = sign;
		if (isTraining)
			if (sign.equals("zero"))
				this.isRelatedToQuestion = false;
			else
				this.isRelatedToQuestion = true;
	}

	public String getStringValue() {
		return this.value;
	}

	public String getSign() {
		return this.sign;
	}

	public int getSentencePos() {
		return this.sentencePos;
	}

	public void setSentencePos(int sentencePos) {
		this.sentencePos = sentencePos;
	}

	public String getUniqueId() {
		return "@" + this.sentenceId + "@" + this.tokenId;
	}

	public Double getDoubleValue() {
		if (this.isUnknown)
			return null;
		return Double.parseDouble(this.value);
	}

	public void setContext(String rel, Set<Integer> words, AnnotatedSentence sent) {
		List<CoreLabel> labels = new ArrayList<>();
		for (Integer i : words) {
			labels.add(sent.getToken(i));
		}
		this.context.put(rel, labels);
	}

	/**
	 * @return the type
	 */
	public ArrayList<CoreLabel> getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ArrayList<CoreLabel> type) {
		this.type = type;
	}

	/**
	 * @param isPart
	 *            the isPart to set
	 */
	public void setPart(boolean isPart) {
		this.isPart = isPart;
	}

	/**
	 * @return the partOf
	 */
	public Quantity getPartOf() {
		return partOf;
	}

	/**
	 * @param partOf
	 *            the partOf to set
	 */
	public void setPartOf(Quantity partOf) {
		this.partOf = partOf;
	}

	public void setUnknownId(String id) {
		this.unknownId = id;
	}

	/**
	 * @return
	 */
	public String getUnknownId() {
		return this.unknownId;
	}

	public List<CoreLabel> getAssociatedEntity(String rel) {

		return this.context.get(rel);
	}

	public boolean isMarkedWithAll(ProblemRepresentation irep) {
		if (this.allMarker != null)
			return this.allMarker;

		boolean wholeMarkedWithCue = false;

		for (CoreLabel c : this.getAssociatedEntity("dobj")) {
			if (c.lemma().equalsIgnoreCase("total") || c.lemma().equalsIgnoreCase("overall")) {
				wholeMarkedWithCue = true;
			}
		}

		for (CoreLabel c : this.getAssociatedEntity("prep_in")) {
			if (c.lemma().equalsIgnoreCase("total")) {
				wholeMarkedWithCue = true;
			} else if (c.lemma().equalsIgnoreCase("all")) {
				wholeMarkedWithCue = true;
			}
		}
		for (CoreLabel l : this.getAssociatedEntity("advmod")) {
			if (l.lemma().equalsIgnoreCase("together"))
				wholeMarkedWithCue = true;
		}

		for (CoreLabel l : this.getAssociatedEntity("amod")) {
			if (l.lemma().equalsIgnoreCase("all"))
				wholeMarkedWithCue = true;
		}

		for (CoreLabel l : this.getAssociatedEntity("dep")) {
			if (l.lemma().equalsIgnoreCase("altogether"))
				wholeMarkedWithCue = true;
		}

		for (CoreLabel l : this.getAssociatedEntity("verb")) {
			if (l.lemma().equalsIgnoreCase("combine"))
				wholeMarkedWithCue = true;
		}

		try {
			if (!wholeMarkedWithCue && this.isUnknown) {
				AnnotatedSentence sen = irep.getAnnotatedSentences().get(this.sentenceId - 1);
				int ts = sen.getTokenSequence().size();
				CoreLabel token = sen.getToken(ts - 1);
				if (token.lemma().equalsIgnoreCase("all") || token.lemma().equalsIgnoreCase("overall"))
					wholeMarkedWithCue = true;
				if (sen.getLemma(1).equalsIgnoreCase("altogether"))
					wholeMarkedWithCue = true;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.allMarker = wholeMarkedWithCue;

		return wholeMarkedWithCue;
	}

	public boolean isMarkedWithTotalOf(ProblemRepresentation irep) {
		boolean wholeMarkedWithCue = false;

		for (CoreLabel c : this.getAssociatedEntity("dobj")) {
			if (c.lemma().equalsIgnoreCase("total") || c.lemma().equalsIgnoreCase("overall")) {
				wholeMarkedWithCue = true;
			}
		}

		return wholeMarkedWithCue;
	}

	public boolean hasNonBeVerb() {
		for (CoreLabel c : this.getAssociatedEntity("verb")) {
			if (!c.lemma().equalsIgnoreCase("be") && !c.lemma().equalsIgnoreCase("has")
					&& !c.lemma().equalsIgnoreCase("have") && !c.lemma().equalsIgnoreCase("contain")
					&& !c.lemma().equalsIgnoreCase("remain") && !c.lemma().equalsIgnoreCase("hold")) {
				return true;
			}
		}

		return false;
	}

	public void setLargeQuantity(boolean isLargeQ) {
		this.differenceLargerQ = isLargeQ;
	}

	public void setSmallQuantity(boolean isSmallQ) {
		this.differenceSmallerQ = isSmallQ;
	}

	public boolean isSmallQuantity() {
		return this.differenceSmallerQ;
	}

	public boolean isLargeQuantity() {
		return this.differenceLargerQ;
	}

	public void setContextSegment(List<CoreLabel> contextSegment) {
		this.contextSegment = contextSegment;
	}

	public List<CoreLabel> getContextSegment() {
		return this.contextSegment;
	}
}
