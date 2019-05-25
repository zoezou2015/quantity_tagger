package org.statnlp.example.math.features;

import java.util.List;

import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

public class WholeSampleFeatureChinese {
	public static void addFeatures(ProblemRepresentation probRep) {
		if (probRep.getDifferenceCue())
			return;
		List<Quantity> quantities = probRep.getQuantities();
		if (quantities.size() != 3)
			return;
		Quantity unknown = probRep.getUnknownQuantities().get(0);
		String problemText = probRep.getText();

		if ((problemText.contains("一共") || problemText.contains("共有")) && quantities.size() == 3) {
			probRep.setWholeCue(true);
		}
	}

}
