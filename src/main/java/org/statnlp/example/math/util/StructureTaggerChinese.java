package org.statnlp.example.math.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.statnlp.example.math.type.AnnotatedSentence;
import org.statnlp.example.math.type.ProblemRepresentation;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class StructureTaggerChinese {

	public static void main(String[] args) {
		// String text = "克林顿说，华盛顿将逐步落实对韩国的经济援助。" +
		// "金大中对克林顿的讲话报以掌声：克林顿总统在会谈中重申，他坚定地支持韩国摆脱经济危机。";
		String text = "中山 小学 四年级 有 245 人 分别 参加 了 3 个 兴趣小组 。 其中 ， 参加 美术 兴趣小组 有 175 人 ， 参加 文艺 兴趣小组 有 25 人 ， 参加 体育 兴趣小组 有 多少 人 。";
		Annotation document = new Annotation(text);
		Properties pros = new Properties();
		try {
			pros.load(new FileInputStream("data/math/Math23K/StanfordCoreNLP-chinese.properties"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// StanfordCoreNLP corenlp = new
		// StanfordCoreNLP("StanfordCoreNLP-chinese.properties");
		StanfordCoreNLP corenlp = new StanfordCoreNLP(pros);
		corenlp.annotate(document);
		parserOutput(document);

	}

	private StanfordCoreNLP pipline_chinese;

	public StructureTaggerChinese() {
		// this.pipline_chinese = new
		// StanfordCoreNLP("StanfordCoreNLP-chinese.properties");
		Properties pros = new Properties();
		try {
			pros.load(new FileInputStream("data/math/Math23K/StanfordCoreNLP-chinese.properties"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pipline_chinese = new StanfordCoreNLP(pros);
	}

	/**
	 * Does 1. Tokenization 2. Sentence Boundary Detection 3. Lemmatization 4.
	 * Part-Of-Speech tagging 5. PCFG parsing 6. Dependency parsing 7. Co-reference
	 * resolution
	 */
	public void processChinese(ProblemRepresentation prep) {
		String text = prep.getText();
		Annotation document = new Annotation(text);
		this.pipline_chinese.annotate(document);
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		ArrayList<AnnotatedSentence> annotatedSentences = new ArrayList<>();

		for (CoreMap sent : sentences) {
			AnnotatedSentence annotatedSentence = new AnnotatedSentence(sent.get(CoreAnnotations.TextAnnotation.class),
					sent);
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

	public static void parserOutput(Annotation document) {
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values
		// with custom types
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		System.out.println("sent length: " + sentences.size());
		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

				System.out.println(word + "\t" + pos + "\t" + ne);
			}

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			System.out.println("语法树：");
			System.out.println(tree.toString());

			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			System.out.println("依存句法：");
			System.out.println(dependencies.toString());
		}

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		Map<Integer, CorefChain> graph = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
	}
}
