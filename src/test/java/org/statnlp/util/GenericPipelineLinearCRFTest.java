/**
 * 
 */
package org.statnlp.util;

import org.statnlp.util.GenericPipeline;

/**
 * To test the implementation of {@link GenericPipeline} on LinearCRF implementation
 */
public class GenericPipelineLinearCRFTest {
	
	public static void main(String[] args){
		// Testing default pipeline, which uses the following settings:
		// - Model used is linear-chain CRF (com.statnlp.example.linear_crf.LinearCRF*)
		// - Data files are assumed to be in CoNLL format (one word per row, with the last column being the label)
		// - Train using L-BFGS
		GenericPipeline pipeline = new GenericPipeline()
				.withTrainPath("data/train.data") // Specify the training data
				.withTestPath("data/test.data")	  // Specify the test data
				.withModelPath("test.model")	  // Specify where to save the model (if not specified no model will be written)
				.withLogPath("test.log")		  // Specify the log file
				.withAttemptMemorySaving(true)	  // Save memory and time
				.addTask("train")				  // Specify the tasks
				.addTasks("test", "evaluate")	  // You can use the plural or the singular
				.addTask("visualize")			  // And you can add tasks multiple times, which will be executed in order
				;
		pipeline.execute();						  // Execute the pipeline
	}

}
