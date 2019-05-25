package org.statnlp.example.math.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class VerbPolarityHelper {
	private Map<String, Double> verbPolarityMap;
	private static final String filename = "data/math/AddSub/verb_polarity.txt";
	private static VerbPolarityHelper vpHelper = null;

	public VerbPolarityHelper(String filename) throws NumberFormatException, IOException {
		this.verbPolarityMap = new HashMap<>();
		File f = new File(filename);
		for (String line : FileUtils.readLines(f)) {
			String[] keyval = line.split("\t");
			this.verbPolarityMap.put(keyval[0], Double.parseDouble(keyval[1]));
		}
	}

	public double getPolarity(String verb) {
		if (this.verbPolarityMap.containsKey(verb))
			return this.verbPolarityMap.get(verb);
		return 0.0;
	}

	public static VerbPolarityHelper getInstance() {
		if (vpHelper == null) {
			try {
				vpHelper = new VerbPolarityHelper(filename);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return vpHelper;
	}

}
