package org.statnlp.example.math.type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.statnlp.example.math.util.POSUtil;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;

public class AnnotatedSentence implements Serializable {

	private static final long serialVersionUID = 8475156785141683138L;
	private String sentence;
	private CoreMap tSentence;
	private HashMap<Integer, CoreLabel> tokens;
	private List<CoreLabel> tokenSequence;
	private SemanticGraph dependencyGraph;
	private Tree tree;
	private boolean isQuestion = false;
	private int problemId;
	private List<CoreLabel> questionPart;

	public AnnotatedSentence(String sentence, CoreMap tSentence) {
		this.sentence = sentence;
		this.tSentence = tSentence;
		this.tokenSequence = tSentence.get(CoreAnnotations.TokensAnnotation.class);
		this.tokens = new HashMap<>();
		for (CoreLabel label : this.tokenSequence) {
			this.tokens.put(label.index(), label);
		}

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		// GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		// this is the parse tree of the current sentence
		this.tree = tSentence.get(TreeCoreAnnotations.TreeAnnotation.class);
		// GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		// Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

		// this is the Stanford dependency graph of the current sentence
		// this.dependencyGraph = new SemanticGraph(tdl);
		this.dependencyGraph = tSentence
				.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
		// System.out.println(this.dependencyGraph);

	}

	/**
	 * @return the tokenSequence
	 */
	public List<CoreLabel> getTokenSequence() {
		return this.tokenSequence;
	}

	public String getWord(int index) {
		CoreLabel token = this.tokens.get(index);
		if (token == null)
			throw new IllegalArgumentException(String.format("Token with Id %s is missing", index));
		return token.get(TextAnnotation.class);
	}

	public void setQuestion() {
		this.isQuestion = true;
	}

	public boolean isQuestion() {
		return this.isQuestion;
	}

	public List<CoreLabel> getQuestionPart() {
		if (this.isQuestion)
			return this.questionPart;
		else
			throw new RuntimeException("This is not a question sentence");
	}

	public void setQuestionPart(List<CoreLabel> questionPart) {
		this.questionPart = questionPart;
	}

	public String getWord(CoreLabel token) {
		return token.get(TextAnnotation.class);
	}

	/**
	 * @param tokenIdx
	 * @return
	 */
	public CoreLabel getToken(int tokenIdx) {
		return this.tokens.get(tokenIdx);
	}

	/**
	 * @param token
	 * @return POS tag
	 */
	public String getPOS(CoreLabel token) {
		return token.get(PartOfSpeechAnnotation.class);
	}

	public String getPOS(int index) {
		CoreLabel token = this.tokens.get(index);
		if (token == null)
			throw new IllegalArgumentException(String.format("Token with Id %s is missing", index));
		return token.get(PartOfSpeechAnnotation.class);
	}

	public String getNE(int index) {
		CoreLabel token = this.tokens.get(index);
		if (token == null)
			throw new IllegalArgumentException(String.format("Token with Id %s is missing", index));
		return token.get(NamedEntityTagAnnotation.class);
	}

	public String getNE(CoreLabel token) {
		return token.get(NamedEntityTagAnnotation.class);
	}

	public String getLemma(int index) {
		CoreLabel token = this.tokens.get(index);
		if (token == null)
			throw new IllegalArgumentException(String.format("Token with Id %s is missing", index));
		return token.lemma();
	}

	/**
	 * @param token
	 * @return Lemma
	 */
	public String getLemma(CoreLabel token) {
		return this.getLemma(token.index());
	}

	/**
	 * @return the dependencyGraph
	 */
	public SemanticGraph getDependencyGraph() {
		return dependencyGraph;
	}

	public String getRawSentence() {
		return this.sentence;
	}

	/**
	 * @param label
	 * @return
	 */
	public String getFullLemma(CoreLabel label) {
		if (!POSUtil.isVerb(this.getPOS(label)))
			return this.getLemma(label);
		String lemma = label.lemma();
		try {
			Set<IndexedWord> child = this.getDependencyGraph().getChildrenWithReln(
					this.getDependencyGraph().getNodeByIndex(label.index()), GrammaticalRelation.valueOf("prt"));

			for (IndexedWord id : child) {
				lemma += " " + this.getLemma(id.index());

			}

		} catch (Exception e) {
		}
		return lemma;
	}

	public void setProblemId(int id) {
		this.problemId = id;
	}

	public int getProblemId() {
		return this.problemId;
	}

	public boolean hasToken(int id) {
		return this.tokens.containsKey(id);
	}

	public Tense getTense(int index) {
		String pos = this.getPOS(index);
		// return Tense.Null for non-verb words
		if (!pos.toLowerCase().startsWith("v"))
			return Tense.NULL;

		Set<IndexedWord> childs = dependencyGraph.getChildrenWithReln(dependencyGraph.getNodeByIndex(index),
				GrammaticalRelation.valueOf("auxpass"));
		for (IndexedWord id : childs) {
			if (this.getWord(id.index()).equalsIgnoreCase("are") || this.getWord(id.index()).equalsIgnoreCase("is")
					|| this.getWord(id.index()).equalsIgnoreCase("has")
					|| this.getWord(id.index()).equalsIgnoreCase("have")) {
				return Tense.PRESENT;
			}
		}

		if (pos.equalsIgnoreCase("VBD") || pos.equalsIgnoreCase("VBN"))
			return Tense.PAST;

		Set<IndexedWord> child = dependencyGraph.getChildrenWithReln(dependencyGraph.getNodeByIndex(index),
				GrammaticalRelation.valueOf("aux"));
		for (IndexedWord id : child) {
			if (this.getWord(id.index()).equalsIgnoreCase("will")) {
				return Tense.FUTURE;
			}
		}

		return Tense.PRESENT;
	}
}
