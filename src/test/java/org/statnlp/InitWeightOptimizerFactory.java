/**
 * 
 */
package org.statnlp;

import java.util.HashMap;

import org.statnlp.commons.ml.opt.Optimizer;
import org.statnlp.commons.ml.opt.OptimizerFactory;
import org.statnlp.hypergraph.StringIndex;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * 
 */
public class InitWeightOptimizerFactory extends OptimizerFactory {
	
	private static final long serialVersionUID = 462325492055929006L;
	private OptimizerFactory realOptimizerFactory;
	private TIntObjectHashMap<TIntObjectHashMap<TIntDoubleHashMap>> featureWeightMap;
	private HashMap<String, HashMap<String, HashMap<String, Double>>> featureWeightMapStr;
	
	public InitWeightOptimizerFactory(HashMap<String, HashMap<String, HashMap<String, Double>>> featureWeightMap){
		this(featureWeightMap, null);
	}
	
	public InitWeightOptimizerFactory(HashMap<String, HashMap<String, HashMap<String, Double>>> featureWeightMap, OptimizerFactory realOptimizer){
		super();
		this.featureWeightMapStr = featureWeightMap;
		this.realOptimizerFactory = realOptimizer;
	}
	
	public InitWeightOptimizerFactory(TIntObjectHashMap<TIntObjectHashMap<TIntDoubleHashMap>> featureWeightMap){
		this(featureWeightMap, null);
	}
	
	public InitWeightOptimizerFactory(TIntObjectHashMap<TIntObjectHashMap<TIntDoubleHashMap>> featureWeightMap, OptimizerFactory realOptimizer){
		super();
		this.featureWeightMap = featureWeightMap;
		this.realOptimizerFactory = realOptimizer;
	}

	/* (non-Javadoc)
	 * @see com.statnlp.commons.ml.opt.OptimizerFactory#create(int)
	 */
	@Override
	public Optimizer create(int numWeights) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public Optimizer create(int numWeights, TIntObjectHashMap<TIntObjectHashMap<TIntIntHashMap>> featureIntMap, StringIndex stringIndex){
		if(featureWeightMap == null && !stringIndex.hasReverseIndex()){
			stringIndex.buildReverseIndex();
		}
		double[] initialWeights = new double[numWeights];
		for(int type: featureIntMap.keys()){
			TIntObjectHashMap<TIntIntHashMap> outputToInputInt = featureIntMap.get(type);
			TIntObjectHashMap<TIntDoubleHashMap> outputToInputWeight = null;
			HashMap<String, HashMap<String, Double>> outputToInputWeightStr = null;
			if(featureWeightMap != null){
				outputToInputWeight = featureWeightMap.get(type);
			} else {
				outputToInputWeightStr = featureWeightMapStr.get(stringIndex.get(type));
			}
			for(int output: outputToInputInt.keys()){
				TIntIntHashMap inputToInt = outputToInputInt.get(output);
				TIntDoubleHashMap inputToWeight = null;
				HashMap<String, Double> inputToWeightStr = null;
				if(outputToInputWeight != null){
					inputToWeight = outputToInputWeight.get(output);
				} else {
					inputToWeightStr = outputToInputWeightStr.get(stringIndex.get(output));
				}
				for(int input: inputToInt.keys()){
					if(inputToWeight != null){
						initialWeights[inputToInt.get(input)] = inputToWeight.get(input);
					} else {
						initialWeights[inputToInt.get(input)] = inputToWeightStr.get(stringIndex.get(input));
					}
				}
			}
		}
		if(realOptimizerFactory == null){
			return new InitWeightOptimizer(initialWeights);
		} else {
			return new InitWeightOptimizer(initialWeights, realOptimizerFactory.create(numWeights));
		}
	}

}
