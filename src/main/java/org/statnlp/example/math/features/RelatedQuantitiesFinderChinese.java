package org.statnlp.example.math.features;

import java.util.ArrayList;
import java.util.List;

import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

public class RelatedQuantitiesFinderChinese {
	public RelatedQuantitiesFinderChinese() {

	}

	public static void FindRelatedQuantities(ProblemRepresentation prep, ArrayList<String> mismatch) {
		List<Quantity> quantities = prep.getQuantities();
		Quantity unknown = prep.getUnknownQuantities().get(0);
		if (quantities.size() <= 3) {
			for (Quantity q : quantities)
				q.setRelatedToQustion(true);
			return;
		}
		unknown.setRelatedToQustion(true);

		boolean mis = false;
		for (Quantity q : quantities) {
			if (q.isUnknown())
				continue;
			boolean gold_rel = q.isRelatedToQuestion();
			if (!gold_rel) {
				mis = true;
			}
		}
		if (mis)
			mismatch.add(prep.getProblemId() + "");
	}
}
