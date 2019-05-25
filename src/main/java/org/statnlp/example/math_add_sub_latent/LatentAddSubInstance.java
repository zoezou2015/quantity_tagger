package org.statnlp.example.math_add_sub_latent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.statnlp.commons.types.Sentence;
import org.statnlp.example.base.BaseInstance;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

public class LatentAddSubInstance extends BaseInstance<LatentAddSubInstance, Sentence, ArrayList<String>> {

	public static final long serialVersionUID = -7547398939391168363L;
	private int iIndex;
	private String lEquations;
	private String predEquations;
	private double lSolutions;
	private Sentence sQuestion;
	private Sentence partText;
	private Sentence quesPart;
	private HashMap<String, Integer> num2pos;
	private HashMap<String, String> num2sign;
	private ArrayList<String> numberSign;
	private ArrayList<String> prediction;
	private ArrayList<Integer> numberPos;
	private ArrayList<Integer> partTextPos;
	private ArrayList<String> numbers;
	private ArrayList<String> span;
	private List<Quantity> quantities;
	private ProblemRepresentation probRep;

	public LatentAddSubInstance(int instanceId, double weight, int iIndex, String lEquations, double lSolutions,
			Sentence sQuestion, HashMap<String, Integer> num2pos, HashMap<String, String> num2sign,
			ArrayList<String> numbers, ArrayList<String> signs, ArrayList<Integer> numberPos) {
		super(instanceId, weight);
		this.iIndex = iIndex;
		this.lEquations = lEquations;
		this.lSolutions = lSolutions;
		this.sQuestion = sQuestion;
		this.num2pos = num2pos;
		this.num2sign = num2sign;
		this.output = signs;
		this.input = sQuestion;
		this.numbers = numbers;
		this.numberPos = numberPos;
		this.partTextPos = new ArrayList<>();
	}

	public double getGoldSolution() {
		return this.lSolutions;
	}

	public Sentence getProblemText() {
		return this.sQuestion;
	}

	public Sentence getPartProblemText() {
		return this.partText;
	}

	public void setPartProblemText(Sentence partText) {
		this.partText = partText;
	}

	public void setQestionPart(Sentence quesPart) {
		this.quesPart = quesPart;
	}

	public void setPartTextPos(ArrayList<Integer> partTextPos) {
		// System.out.println(partTextPos);
		this.partTextPos = partTextPos;
	}

	public ArrayList<Integer> getPartTextPos() {
		return this.partTextPos;
	}

	public Sentence getQestionPart() {
		return this.quesPart;
	}

	public int getProblemId() {
		return this.iIndex;
	}

	public String getEquation() {
		return this.lEquations;
	}

	public void setPredEquation(String predEquations) {
		this.predEquations = predEquations;
	}

	public String getPredEquation() {
		return this.predEquations;
	}

	public HashMap<String, Integer> getNumber2Pos() {
		return this.num2pos;
	}

	public HashMap<String, String> getNumber2Sign() {
		return this.num2sign;
	}

	public ArrayList<Integer> getNumberPos() {
		return this.numberPos;
	}

	@Override
	public ArrayList<String> getOutput() {
		return this.output;
	}

	public ArrayList<String> getSpan() {
		return this.span;
	}

	public void setSpan(ArrayList<String> span) {
		this.span = span;
	}

	public ArrayList<String> getNumbers() {
		return this.numbers;
	}

	@Override
	public LatentAddSubInstance duplicate() {
		LatentAddSubInstance ins = new LatentAddSubInstance(this._instanceId, this._weight, this.iIndex,
				this.lEquations, this.lSolutions, this.sQuestion, this.num2pos, this.num2sign, this.numbers,
				this.output, this.numberPos);
		ins.setPrediction(this.prediction);
		ins.setQestionPart(this.quesPart);
		ins.setQuantities(this.quantities);
		ins.setProblemRepresentation(this.probRep);
		ins.setPartTextPos(this.partTextPos);
		return ins;
	}

	@Override
	public void setPrediction(Object prediction) {
		this.prediction = (ArrayList<String>) prediction;
	}

	@Override
	public ArrayList<String> getPrediction() {
		return this.prediction;
	}

	public void setProblemRepresentation(ProblemRepresentation probRep) {
		this.probRep = probRep;
	}

	public ProblemRepresentation getProblemRepresentation() {
		return this.probRep;
	}

	public void setQuantities(List<Quantity> quantities) {
		this.quantities = quantities;
	}

	public List<Quantity> getQuantities() {
		return this.quantities;
	}

	@Override
	public int size() {
		return this.input.length();
	}

}
