package org.statnlp.example.math.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.stanford.nlp.ie.NumberNormalizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;

public class MathUtility {
	// sc.useDelimiter("[;|\\.|\\s|\\,|!|?|<<|>>|،|«|»|\\-|:|\\/|؟|\\\\|(|)]+");
	public static String[] getArray(String text) {
		StringTokenizer st = new StringTokenizer(text);
		ArrayList<String> s = new ArrayList<>();
		while (st.hasMoreTokens()) {
			s.add(st.nextToken());
		}
		return listToArr(s);
	}

	public static String[] listToArr(List<String> strs) {
		String[] ret = new String[strs.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strs.get(i);
		}
		return ret;
	}

	public static Tree getRoot(TreeGraphNode node) {
		Tree curNode = node;
		while (curNode.parent() != null) {
			curNode = curNode.parent();
		}
		return curNode;
	}

	public static int getNumber(TreeGraphNode node) {
		String s = node.toString();
		String[] strs = s.split("-");
		int ret = Integer.parseInt(strs[strs.length - 1]);
		return ret;
	}

	public static String listToString(ArrayList<String> tokens) {
		String ret = "";
		for (String s : tokens) {
			ret = ret + s + " ";
		}
		return ret;
	}

	public static String MatharrToString(ArrayList<String> tokens) {
		String ret = "";
		for (String ss : tokens) {
			ss = ss.replace(",", "");
			StringTokenizer st = new StringTokenizer(ss);
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				try {
					if (s.contains("/")) {
						// System.out.println("it contained");
						Scanner sc = new Scanner(s);
						sc.useDelimiter("[/| ]");
						double a = sc.nextDouble();
						// System.out.println("a is: "+a);
						double b = sc.nextDouble();
						// System.out.println("b is: "+b);
						double retD = ret.length() > 1 ? Double.parseDouble(ret) : 0;
						retD += +(a / b);
						ret = retD + "";
						return ret;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				ret = ret + s + " ";
			}

		}
		return ret;
	}

	public static String getNumStr(double num) {
		String numStr;
		if (num - Math.floor(num) == 0) {
			numStr = ((int) num) + "";
		} else {
			numStr = num + "";
		}
		return numStr;
	}

	public static List<String> getCopy(List<String> str2) {
		List<String> str1 = new ArrayList<>();
		getCopy(str1, str2);
		return str1;
	}

	public static void getCopy(List<String> str1, List<String> str2) {
		// str1 = new ArrayList<String>();
		for (String s : str2) {
			str1.add(s);
		}
	}

	public static boolean SEqual(Object s1, Object s2) {
		if (s1 == null && s2 == null) {
			return true;
		} else if (s1 == null) {
			return false;
		} else {
			return s1.equals(s2);
		}
	}

	public static boolean listEqual(List<String> l1, List<String> l2) {
		if (l1.size() != l2.size()) {
			return false;
		}
		for (Object o : l1) {
			if (!l2.contains(o)) {
				return false;
			}
		}
		return true;
	}

	public static boolean listSubset(List<String> l1, List<String> l2) {
		if (l1.size() > l2.size()) {
			return false;
		}
		for (Object o : l1) {
			if (!l2.contains(o)) {
				return false;
			}
		}
		return true;
	}

	public static boolean listSubset2(List<String> l1, List<String> l2) {
		return listEqual(l1, l2) || listEqual(l2, l1);
	}

	public static List<String> getUnion(List<String> l1, List<String> l2) {
		ArrayList<String> ret = new ArrayList<>();
		HashSet<String> reth = new HashSet<>();
		for (String s : l1) {
			reth.add(s);
		}
		for (String s : l2) {
			reth.add(s);
		}
		for (String s : reth) {
			ret.add(s);
		}
		return ret;
	}

	public static double getDouble(String s) {
		ArrayList<String> numStrs = new ArrayList<>();
		numStrs.add(s);
		String numStr = MatharrToString(numStrs);
		double num = NumberNormalizer.wordToNumber(numStr).doubleValue();
		return num;
	}

	public static boolean isNumber(ArrayList<String> numStrs) {
		if (numStrs.size() > 1) {
			return false;
		}
		for (char c : numStrs.get(0).toCharArray()) {
			if (c != '.' && (c < '0' || c > '9')) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNumber(String numStrs) {
		if (numStrs.equals(""))
			return false;
		ArrayList<String> numString = new ArrayList<>();
		numString.add(numStrs);
		for (char c : numString.get(0).toCharArray()) {
			if (c != '.' && (c < '0' || c > '9')) {
				return false;
			}
		}
		try {
			Double.parseDouble(numStrs);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
