package org.statnlp.example.math.util;

public class POSUtil {
	public static boolean isNoun(String pos) {
		if (pos.toLowerCase().startsWith("n"))
			return true;
		return false;
	}

	public static boolean isVerb(String pos) {
		if (pos.toLowerCase().startsWith("v"))
			return true;
		return false;
	}

	public static boolean isMD(String pos) {
		if (pos.toLowerCase().startsWith("md"))
			return true;
		return false;
	}

	public static boolean isAux(String verb) {
		if ("be".equalsIgnoreCase(verb) || "has".equalsIgnoreCase(verb) || "have".equalsIgnoreCase(verb)
				|| "do".equalsIgnoreCase(verb))
			return true;
		return false;
	}

	public static boolean isComparativeAdj(String pos) {
		if (pos.equalsIgnoreCase("jjr") || pos.equalsIgnoreCase("jjs"))
			return true;
		return false;
	}

	public static boolean isNonComparativeAdj(String pos) {
		if (pos.equalsIgnoreCase("jj"))
			return true;
		return false;
	}

	/**
	 * @param pos
	 * @return
	 */
	public static boolean isCC(String pos) {

		return pos.equalsIgnoreCase("cc");
	}

	public static boolean isIN(String pos) {

		return pos.equalsIgnoreCase("in");
	}

	public boolean isPast(String verb) {
		if (verb.equalsIgnoreCase("VBD") || verb.equalsIgnoreCase("VBN"))
			return true;
		return false;
	}

	/**
	 * @return
	 */
	public static boolean isNumber(String pos) {

		return pos.equalsIgnoreCase("cd");
	}

	/**
	 * @param pos
	 * @return
	 */
	public static boolean isPrep(String pos) {

		return pos.equalsIgnoreCase("IN");
	}
}