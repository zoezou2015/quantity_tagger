package org.statnlp.example.math.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;

public class WordNetHelper {

	private static final WordNetHelper wnh = new WordNetHelper();

	private WordNetHelper() {
		// System.setProperty("wordnet.database.dir",
		// "/usr/local/Cellar/wordnet/3.1/dict");
		System.setProperty("wordnet.database.dir", "/usr/local/WordNet-3.0/dict");
	}

	public boolean hasCommonAncestor(String lemma1, String lemma2, int length) {

		WordNetDatabase database = WordNetDatabase.getFileInstance();

		Set<Synset> syn1 = new HashSet<>(Arrays.asList(database.getSynsets(lemma1)));
		Set<Synset> syn2 = new HashSet<>(Arrays.asList(database.getSynsets(lemma2)));

		Set<Synset> intersection;
		intersection = new HashSet<>();
		intersection.addAll(syn1);
		intersection.retainAll(syn2);
		if (!intersection.isEmpty())
			return true;

		for (int i = 1; i <= length; i++) {
			syn1.addAll(this.getHypernyms(syn1));
			syn2.addAll(this.getHypernyms(syn2));
			intersection = new HashSet<>();
			intersection.addAll(syn1);
			intersection.retainAll(syn2);
			if (!intersection.isEmpty()) {
				// System.out.println(intersection);
				return true;
			}
		}
		return false;
	}

	// vehicle is the hypernym of car
	public Set<Synset> getHypernyms(Set<Synset> syns) {
		Set<Synset> ret = new HashSet<>();
		for (Synset syn : syns) {
			if (syn instanceof NounSynset) {
				NounSynset nounSynset = (NounSynset) (syn);
				ret.addAll(new HashSet<Synset>(Arrays.asList(nounSynset.getHypernyms())));
			} else if (syn instanceof VerbSynset) {
				VerbSynset verbSynset = (VerbSynset) (syn);
				ret.addAll(new HashSet<Synset>(Arrays.asList(verbSynset.getHypernyms())));
			}
		}
		return ret;
	}

	// bus is a hyponym of transport
	public boolean isAHyponym(String lemma1, String lemma2, int length) {
		WordNetDatabase database = WordNetDatabase.getFileInstance();

		Set<Synset> syn1 = new HashSet<>(Arrays.asList(database.getSynsets(lemma1)));
		Set<Synset> syn2 = new HashSet<>(Arrays.asList(database.getSynsets(lemma2)));

		Set<Synset> intersection;
		intersection = new HashSet<>();
		intersection.addAll(syn2);
		intersection.retainAll(syn1);
		if (!intersection.isEmpty())
			return true;

		for (int i = 1; i <= length; i++) {
			syn2 = this.getHypernyms(syn2);
			intersection = new HashSet<>();
			intersection.addAll(syn2);
			intersection.retainAll(syn1);
			if (!intersection.isEmpty()) {

				return true;
			}
		}
		return false;
	}

	// fan yi ci
	public boolean isAntonym(String lemma1, String lemma2) {
		WordNetDatabase database = WordNetDatabase.getFileInstance();

		Set<Synset> syn1 = new HashSet<>(Arrays.asList(database.getSynsets(lemma1)));
		Set<Synset> syn2 = new HashSet<>(Arrays.asList(database.getSynsets(lemma2)));

		for (Synset s : syn1) {
			WordSense[] an = s.getAntonyms(lemma1);

			for (WordSense word : an) {
				if (word.getWordForm().equalsIgnoreCase(lemma2))
					return true;
			}
		}

		for (Synset s : syn2) {
			WordSense[] an = s.getAntonyms(lemma2);
			for (WordSense word : an) {
				if (word.getWordForm().equalsIgnoreCase(lemma1))
					return true;
			}
		}
		return false;
	}

	public boolean entails(String verb1, String verb2) {
		WordNetDatabase database = WordNetDatabase.getFileInstance();

		Set<Synset> syn1 = new HashSet<>(Arrays.asList(database.getSynsets(verb1, SynsetType.VERB)));
		Set<Synset> syn2 = new HashSet<>(Arrays.asList(database.getSynsets(verb2, SynsetType.VERB)));

		Set<VerbSynset> s1 = new HashSet<>();
		Set<VerbSynset> s2 = new HashSet<>();

		for (Synset s : syn1) {
			VerbSynset vs = (VerbSynset) s;
			VerbSynset[] an = vs.getEntailments();
			s1.addAll(Arrays.asList(an));
		}

		HashSet<Synset> intersection = new HashSet<>();
		intersection.addAll(s1);
		intersection.retainAll(syn2);
		if (!intersection.isEmpty())
			return true;

		for (Synset s : syn2) {
			VerbSynset vs = (VerbSynset) s;
			VerbSynset[] an = vs.getHypernyms();
			s2.addAll(Arrays.asList(an));
		}
		intersection.addAll(s1);
		intersection.retainAll(s2);
		if (!intersection.isEmpty())
			return true;
		return false;

	}

	public boolean isAdjective(String word) {
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		return database.getSynsets(word, SynsetType.ADJECTIVE).length > 0;
	}

	public static WordNetHelper getInstance() {
		return wnh;
	}
}
