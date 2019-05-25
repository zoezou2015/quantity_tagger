package org.statnlp.example.math.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreLabel;

public class ProblemRepresentation implements Serializable {

	private static final long serialVersionUID = -770654422101836167L;
	private String text;
	private Map<String, Boolean> corefMap;
	private ArrayList<AnnotatedSentence> annotatedSentences;
	private int numberofQuantities;
	private Integer numberOfunknowns;
	private Map<Integer, Quantity> quantityMap;
	private Map<Integer, Quantity> unknownMap;
	private int lIndex;
	private String lEquation;
	private String predEquation;
	private double lSolution;
	private ArrayList<String> output;
	private ArrayList<String> predictions;
	private String signForUnknown;
	private Map<String, String> signForQuantities;
	private List<Quantity> listOfQuantities;
	private boolean differenceCue = false;
	private ComparisonSample comparisonSample;
	private boolean wholeCue = false;
	private boolean changeCue = false;
	private List<CoreLabel> questionPart;
	private ArrayList<String> quantityVaule;
	private ArrayList<String> numbers;
	private HashMap<String, Integer> num2pos;

	public ProblemRepresentation(int lIndex, String text, String lEquation, double lSolution) {
		this.text = text;
		this.lIndex = lIndex;
		this.lEquation = lEquation;
		this.lSolution = lSolution;
		this.numberofQuantities = 0;
		this.numberOfunknowns = 0;
		this.quantityMap = new HashMap<>();
		this.corefMap = new HashMap<>();
		this.unknownMap = new HashMap<>();
		this.quantityVaule = new ArrayList<>();
	}

	public ArrayList<String> getQuantityValue() {
		return this.quantityVaule;
	}

	public boolean getDifferenceCue() {
		return this.differenceCue;
	}

	public void setDifferenceCue(boolean differenceCue) {
		this.differenceCue = differenceCue;
	}

	public boolean getWholeCue() {
		return this.wholeCue;
	}

	public void setWholeCue(boolean wholeCue) {
		this.wholeCue = wholeCue;
	}

	public boolean getChangeCue() {
		return this.changeCue;
	}

	public void setChangeCue(boolean changeCue) {
		this.changeCue = changeCue;
	}

	public void setComparisonSample(ComparisonSample comparisonSample) {
		if (!this.differenceCue) {
			throw new RuntimeException("The problem should be activated as comparison!");
		}
		this.comparisonSample = comparisonSample;
	}

	public ComparisonSample getComparisonSample() {
		if (!this.differenceCue) {
			throw new RuntimeException("The problem should be activated as comparison!");
		}
		return this.comparisonSample;
	}

	public int getProblemId() {
		return this.lIndex;
	}

	public double getGoldSolution() {
		return this.lSolution;
	}

	public String getEquation() {
		return this.lEquation;
	}

	public void setPredEquation(String predEquations) {
		this.predEquation = predEquations;
	}

	public String getPredEquation() {
		return this.predEquation;
	}

	public String getText() {
		return this.text;
	}

	public void addCoref(int sId1, int tId1, int s, int t) {
		this.corefMap.put(sId1 + ":" + tId1 + ":" + s + ":" + t, true);
	}

	/**
	 * @param annotatedSentences
	 *            the annotatedSentences to set
	 */
	public void setAnnotatedSentences(ArrayList<AnnotatedSentence> annotatedSentences) {
		this.annotatedSentences = annotatedSentences;
	}

	/**
	 * @return the annotatedSentences
	 */
	public ArrayList<AnnotatedSentence> getAnnotatedSentences() {
		return annotatedSentences;
	}

	/**
	 * @return the nuberOfQuantities
	 */
	public int getNumberOfQuantities() {
		return this.numberofQuantities;
	}

	public List<Quantity> getQuantities() {
		if (this.listOfQuantities == null) {
			List<Quantity> q = new LinkedList<>();
			q.addAll(this.quantityMap.values());
			q.addAll(this.unknownMap.values());
			return q;
		} else {
			return this.listOfQuantities;
		}

	}

	public void setQuantities(List<Quantity> quantities) {
		this.listOfQuantities = quantities;
	}

	/**
	 * @param word
	 * @return the id for that constant quantity
	 */
	public Quantity addConstantQuantity(String value, int sId, int tokenId) {
		this.numberofQuantities++;
		this.quantityVaule.add(value);
		Quantity quantity = new Quantity(value, sId, tokenId);
		this.quantityMap.put(numberofQuantities, quantity);
		return quantity;
	}

	/**
	 * @param word
	 * @return the id for that constant quantity
	 */
	public Quantity addUnknown(int sId, int tokenId) {
		this.numberofQuantities++;
		this.numberOfunknowns++;
		Quantity q = new Quantity("X", sId, tokenId);
		this.quantityVaule.add("X");
		q.setUnknown();
		q.setUnknownId("X" + numberofQuantities);
		this.unknownMap.put(numberofQuantities, q);
		return q;
	}

	/**
	 * @return the numberOfunknowns
	 */
	public int getNumberOfunknowns() {
		return numberOfunknowns;
	}

	public String getSignForUnknown() {
		return this.signForUnknown;
	}

	public void setSignForUnknown(String signForUnknown) {
		this.signForUnknown = signForUnknown;
	}

	public void setSignForQuantities(HashMap<String, String> number2sign) {
		this.signForQuantities = number2sign;
	}

	public List<Quantity> getUnknownQuantities() {
		return new ArrayList<>(this.unknownMap.values());
	}

	public List<CoreLabel> getQuestionPart() {
		return this.questionPart;
	}

	public void setQuestionPart(List<CoreLabel> questionPart) {
		this.questionPart = questionPart;
	}

	public ArrayList<String> getNumbers() {
		return numbers;
	}

	public void setNumbers(ArrayList<String> numbers) {
		this.numbers = new ArrayList<>();
		for (String num : numbers) {
			num = num.split("_")[0].trim();
			this.numbers.add(num);
		}
	}

	public HashMap<String, Integer> getNum2pos() {
		return num2pos;
	}

	public void setNum2pos(HashMap<String, Integer> num2pos) {
		this.num2pos = num2pos;
	}

	@Override
	public String toString() {
		return "ProblemRepresentation [text=" + text + ",\n numberOfunknowns=" + numberOfunknowns
				+ ",\n numberofQuantities=" + numberofQuantities + ",\n quantityMap=" + quantityMap + ",\n unknownMap="
				+ unknownMap + "]";
	}

}
