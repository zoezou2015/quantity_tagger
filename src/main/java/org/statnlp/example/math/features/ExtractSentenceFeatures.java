package org.statnlp.example.math.features;

import java.util.ArrayList;

import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.util.StructureTagger;
import org.statnlp.example.math.util.UnknownFinder;

public class ExtractSentenceFeatures {
	private StructureTagger structureTagger;
	private UnknownFinder unknownFinder;
	private RelatedQuantitiesFinder relatedQuantitiesFinder;

	public ExtractSentenceFeatures() {
		if (this.structureTagger == null) {
			this.structureTagger = new StructureTagger();
			this.unknownFinder = new UnknownFinder();
			this.relatedQuantitiesFinder = new RelatedQuantitiesFinder();
		}
	}

	public void StructureTagger(ProblemRepresentation prep) {
		this.structureTagger.process(prep);
	}

	public void FindUnknowns(ProblemRepresentation prep) {
		this.unknownFinder.findUnknowns(prep);
	}

	public void FindRelatedQuantity(ProblemRepresentation prep) {
		this.relatedQuantitiesFinder.FindRelatedQuantites(prep, new ArrayList<>());
	}

}
