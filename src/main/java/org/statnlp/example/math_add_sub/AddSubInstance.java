package org.statnlp.example.math_add_sub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.statnlp.commons.types.Sentence;
import org.statnlp.example.base.BaseInstance;
import org.statnlp.example.math.type.ProblemRepresentation;
import org.statnlp.example.math.type.Quantity;

public class AddSubInstance extends BaseInstance<AddSubInstance, ArrayList<String>, ArrayList<String>> {

	public static final long serialVersionUID = -7547398939391168363L;
	private int iIndex;
	private String lEquations;
	private String predEquations;
	private double lSolutions;
	private Sentence sQuestion;
	private Sentence quesPart;
	private HashMap<String, Integer> num2pos;
	private HashMap<String, String> num2sign;
	private ArrayList<String> numberSign;
	private ArrayList<String> prediction;
	private ArrayList<Integer> numberPos;
	private List<Quantity> quantities;
	private ProblemRepresentation probRep;

	public AddSubInstance(int instanceId, double weight, int iIndex, String lEquations, double lSolutions,
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
		this.input = numbers;
		this.numberPos = numberPos;

	}

	public void setProblemRepresentation(ProblemRepresentation probRep) {
		this.probRep = probRep;
	}

	public ProblemRepresentation getProblemRepresentation() {
		return this.probRep;
	}

	public double getGoldSolution() {
		return this.lSolutions;
	}

	public Sentence getProblemText() {
		return this.sQuestion;
	}

	public void setQestionPart(Sentence quesPart) {
		this.quesPart = quesPart;
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

	public void setQuantities(List<Quantity> quantities) {
		this.quantities = quantities;
	}

	public List<Quantity> getQuantities() {
		return this.quantities;
	}

	@Override
	public AddSubInstance duplicate() {
		AddSubInstance ins = new AddSubInstance(this._instanceId, this._weight, this.iIndex, this.lEquations,
				this.lSolutions, this.sQuestion, this.num2pos, this.num2sign, this.input, this.output, this.numberPos);
		ins.setPrediction(this.prediction);
		ins.setQestionPart(this.quesPart);
		ins.setQuantities(this.quantities);
		ins.setProblemRepresentation(this.probRep);
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

	@Override
	public int size() {
		return this.input.size();
	}

}
