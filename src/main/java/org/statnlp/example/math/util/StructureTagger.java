package org.statnlp.example.math.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StructureTagger {

	private StanfordCoreNLP pipeline;

	public StructureTagger() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		this.pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * Does 1. Tokenization 2. Sentence Boundary Detection 3. Lemmatization 4.
	 * Part-Of-Speech tagging 5. PCFG parsing 6. Dependency parsing 7. Co-reference
	 * resolution
	 */
	public void process(ProblemRepresentation prep) {
		String text = prep.getText();
		Annotation document = new Annotation(text);
		this.pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		ArrayList<AnnotatedSentence> annotatedSentences = new ArrayList<>();
		for (CoreMap sent : sentences) {
			AnnotatedSentence annotatedSentence = new AnnotatedSentence(sent.get(TextAnnotation.class), sent);
			annotatedSentence.setProblemId(prep.getProblemId());
			annotatedSentences.add(annotatedSentence);
		}

		/**
		 * Add co-reference
		 */
		Map<Integer, CorefChain> corefChain = document.get(CorefChainAnnotation.class);
		if (corefChain != null) {

			List<Integer> source = new LinkedList<>();
			List<Integer> target = new LinkedList<>();
			List<CorefMention> except = new LinkedList<>();
			for (Entry<Integer, CorefChain> chain : corefChain.entrySet()) {
				boolean matched = false;
				int corefChainId = -1;
				for (CorefMention elem : chain.getValue().getMentionsInTextualOrder()) {
					if (elem.mentionSpan.matches("[hH]is [a-z]*?")) {
						int sentIdx = elem.position.get(0);
						int tokenEndIdx = elem.endIndex - 1;
						AnnotatedSentence sent = annotatedSentences.get(sentIdx - 1);
						if (tokenEndIdx + 3 < sent.getTokenSequence().size() && tokenEndIdx > 0) {
							String sub = "";
							for (int i = tokenEndIdx - 1; i < tokenEndIdx + 3; i++) {
								sub += sent.getWord(i) + " ";
							}
							// System.out.println(sub);
							if (sub.matches("[hH]is [a-z]*? [a-z]*? him ")
									|| sub.matches("[hH]is [a-z]*? borrowed .*")) {
								matched = true;

								// find cluster ID
								for (Entry<Integer, CorefChain> chain1 : corefChain.entrySet()) {
									for (CorefMention elem1 : chain1.getValue().getMentionsInTextualOrder()) {
										if (elem1.position.get(0) == sentIdx && (elem1.endIndex) == tokenEndIdx) {
											corefChainId = elem1.corefClusterID;
											break;
										}
									}
									if (corefChainId != -1) {
										break;
									}
								}
							}
							if (matched && corefChainId != -1) {
								source.add(elem.corefClusterID);
								target.add(corefChainId);
								except.add(elem);
							}
						}
					}
				}
			}

			for (int i = 0; i < source.size(); i++) {
				List<CorefMention> remove = new LinkedList<>();
				List<CorefMention> s = corefChain.get(source.get(i)).getMentionsInTextualOrder();
				List<CorefMention> t = corefChain.get(target.get(i)).getMentionsInTextualOrder();
				for (CorefMention elem : s) {
					if (!elem.equals(except.get(i))) {
						t.add(elem);
						remove.add(elem);
					}
					// System.out.print("s: " + s + " e: " + except.get(i));
				}
				s.removeAll(remove);
				remove.clear();
			}

			for (Entry<Integer, CorefChain> chain : corefChain.entrySet()) {
				for (CorefMention elem1 : chain.getValue().getMentionsInTextualOrder()) {
					for (CorefMention elem2 : chain.getValue().getMentionsInTextualOrder()) {
						prep.addCoref(elem1.position.get(0), elem1.endIndex - 1, elem2.position.get(0),
								elem2.endIndex - 1);
					}
				}
			}

		}
		prep.setAnnotatedSentences(annotatedSentences);
	}
}
