package org.statnlp.example.math.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.statnlp.example.math.type.AnnotatedSentence;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class AssociatedWordFiner {

	public AssociatedWordFiner() {
	}

	public Set<Integer> findAssociatedWord(List<Integer> labels, AnnotatedSentence sent, String pos) {
		Set<Integer> ret = new HashSet<>();
		SemanticGraph g = sent.getDependencyGraph();
		int minIndex = 10000;
		for (Integer i : labels) {
			ArrayList<IndexedWord> nbrs = new ArrayList<>();
			try {
				nbrs.addAll(g.getChildList(g.getNodeByIndex(i)));
				nbrs.addAll(g.getParentList(g.getNodeByIndex(i)));
				for (IndexedWord word : nbrs) {
					if (sent.getPOS(word.index()).toLowerCase().startsWith(pos)) {
						if (!pos.startsWith("vb")
								|| !g.getChildrenWithReln(g.getNodeByIndex(i), GrammaticalRelation.valueOf("advcl"))
										.contains(word)) {
							if (pos.startsWith("vb")) {
								Set<IndexedWord> x = g.getParentsWithReln(word, GrammaticalRelation.valueOf("xcomp"));
								ArrayList<IndexedWord> xcomp = new ArrayList<>(x);
								if (xcomp.size() == 1 && POSUtil.isVerb(sent.getPOS(xcomp.get(0).index()))
										&& !POSUtil.isAux(sent.getLemma(xcomp.get(0).index()))) {
									ret.add(xcomp.get(0).index());
								} else {
									ret.add(word.index());
								}
							} else {
								ret.add(word.index());
							}
						}
					}
				}
				if (i < minIndex)
					minIndex = i;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!pos.startsWith("vb") || ret.size() == 0)
			for (int i = minIndex; i > 0; i--) {
				if (sent.getPOS(i).toLowerCase().startsWith(pos)) {
					ret.add(i);
					break;
				}
			}
		return ret;
	}

	Set<Integer> findAssociatedWordForQuestion(List<Integer> labels, AnnotatedSentence sent, String pos) {
		Set<Integer> ret = new HashSet<>();
		SemanticGraph g = sent.getDependencyGraph();
		int minIndex = 10000;
		for (Integer i : labels) {
			ArrayList<IndexedWord> nbrs = new ArrayList<>();

			try {
				nbrs.addAll(g.getChildList(g.getNodeByIndex(i)));
				nbrs.addAll(g.getParentList(g.getNodeByIndex(i)));
			} catch (Exception e) {

			}
			if (i < minIndex)
				minIndex = i;
			for (IndexedWord word : nbrs) {
				if (sent.getPOS(word.index()).toLowerCase().startsWith(pos))
					ret.add(word.index());
			}
		}
		if (!pos.startsWith("vb") && ret.size() == 0) {
			for (int i = minIndex; i < sent.getTokenSequence().size(); i++) {
				if (sent.getPOS(i).toLowerCase().startsWith(pos)) {
					ret.add(i);
					break;
				}
			}
		}

		if (pos.equalsIgnoreCase("vb") && ret.size() == 1) {
			int rem = -1;
			for (Integer id : ret) {
				if (sent.getLemma(id).equalsIgnoreCase("do")) {
					boolean changed = false;
					Set<IndexedWord> p = g.getParentsWithReln(g.getNodeByIndex(id), GrammaticalRelation.valueOf("aux"));
					for (IndexedWord ind : p) {
						if (POSUtil.isVerb(sent.getPOS(ind.index()))) {
							ret.add(ind.index());
							changed = true;
							rem = id;
						}
					}
					if (!changed) {
						p = g.getChildrenWithReln(g.getNodeByIndex(id), GrammaticalRelation.valueOf("ccomp"));
						for (IndexedWord ind : p) {
							if (POSUtil.isVerb(sent.getPOS(ind.index()))) {
								ret.add(ind.index());
								rem = id;
							}
						}
					}
				}
			}
			ret.remove(rem);
		}
		return ret;
	}

	public Set<Integer> findAssociatedWordWithRel(Set<Integer> verb, ArrayList<Integer> typeIds, AnnotatedSentence sent,
			String rel) {
		Set<Integer> ret = new HashSet<>();
		Set<IndexedWord> nn = new HashSet<>();
		Set<IndexedWord> ccomp = new HashSet<>();
		for (Integer v : verb) {
			nn = sent.getDependencyGraph().getChildrenWithReln(sent.getDependencyGraph().getNodeByIndex(v),
					GrammaticalRelation.valueOf(rel));
		}
		boolean hasVbrel = !nn.isEmpty();
		for (Integer v : typeIds) {
			try {
				Set<IndexedWord> temp = sent.getDependencyGraph().getChildrenWithReln(
						sent.getDependencyGraph().getNodeByIndex(v), GrammaticalRelation.valueOf(rel));
				if (hasVbrel) {
					for (IndexedWord word : temp) {
						if (sent.getLemma(word.index()).equalsIgnoreCase("all")
								|| sent.getLemma(word.index()).equalsIgnoreCase("total")) {
							nn.add(word);
						}
					}
				} else {
					nn.addAll(temp);
				}
			} catch (Exception e) {

			}
		}

		for (IndexedWord index : nn) {
			ret.add(index.index());
		}

		if (rel.equalsIgnoreCase("nsubj")) {
			// to handle the case where "is"/"are" is the verb
			ret.removeAll(typeIds);
			ret.removeAll(verb);
			if (ret.isEmpty()) {
				for (Integer v : verb) {
					ccomp = sent.getDependencyGraph().getChildrenWithReln(sent.getDependencyGraph().getNodeByIndex(v),
							GrammaticalRelation.valueOf("ccomp"));
				}
				for (IndexedWord c : ccomp) {
					nn.addAll(sent.getDependencyGraph().getChildrenWithReln(c, GrammaticalRelation.valueOf("nsubj")));
				}

				for (IndexedWord index : nn) {
					if (POSUtil.isNoun(sent.getPOS(index.index())))
						ret.add(index.index());
				}

				ret.removeAll(typeIds);
				ret.removeAll(verb);
			}
			Iterator<Integer> it = ret.iterator();
			while (it.hasNext()) {
				String pos = sent.getPOS(it.next()).toLowerCase();
				if (pos.startsWith("rb") || pos.startsWith("jj"))
					it.remove();
			}
		}

		return ret;
	}
}
