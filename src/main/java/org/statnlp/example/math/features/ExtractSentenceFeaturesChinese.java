package org.statnlp.example.math.features;

import java.util.ArrayList;

import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.util.StructureTaggerChinese;
import org.statnlp.example.math.util.UnknownFinderChinese;

public class ExtractSentenceFeaturesChinese {
	private StructureTaggerChinese structureTaggerChinese;
	private UnknownFinderChinese unknownFinderChinese;
	private RelatedQuantitiesFinderChinese relatedQuantitiesFinderChinese;

	public ExtractSentenceFeaturesChinese() {
		if (this.structureTaggerChinese == null) {
			this.structureTaggerChinese = new StructureTaggerChinese();
			this.unknownFinderChinese = new UnknownFinderChinese();
			this.relatedQuantitiesFinderChinese = new RelatedQuantitiesFinderChinese();
		}
	}

	public void StructureTagger(ProblemRepresentation prep) {
		this.structureTaggerChinese.processChinese(prep);
	}

	public void FindUnknowns(ProblemRepresentation prep) {
		this.unknownFinderChinese.findUnknownsChinese(prep);
	}

	public void FindRelatedQuantity(ProblemRepresentation prep, ArrayList<String> mismatch) {
		this.relatedQuantitiesFinderChinese.FindRelatedQuantities(prep, mismatch);
	}

}
