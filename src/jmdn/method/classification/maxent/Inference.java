/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import jmdn.base.struct.pair.PairIntDouble;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Inference {
	protected Model model = null;

	protected int numLabels = 0;

	// for classification
	protected double[] temp = null;

	/**
	 * Default class constructor.
	 */
	public Inference() {
		// do nothing
	}

	/**
	 * Initializing for doing inference.
	 */
	public void init() {
		numLabels = model.data.numLabels();
		temp = new double[numLabels];
	}

	/**
	 * Classifying for a given observation.
	 * 
	 * @param observation
	 *            the observation to be classified
	 */
	public synchronized void classify(Observation observation, boolean isCreateOutput, PrintWriter negWriter, PrintWriter posWriter) {
		int i;

		for (i = 0; i < numLabels; i++) {
			temp[i] = 0.0;
		}

		model.featureGenerator.startScanFeatures(observation);
		while (model.featureGenerator.hasNextFeature()) {
			Feature f = model.featureGenerator.nextFeature();

			temp[f.label] += model.lambda[f.index] * f.value;
		}

		double max = temp[0];
		int maxLabel = 0;
		for (i = 1; i < numLabels; i++) {
			if (max < temp[i]) {
				max = temp[i];
				maxLabel = i;
			}
		}

		observation.modelLabel = maxLabel;

		// For create data, only for 2 classes
		if (isCreateOutput) {
			double other = temp[0];
			if (Math.abs(other - max) < 0.0000000001) {
				other = temp[1];
			}
			double v1 = Math.exp(max);
			double v2 = Math.exp(other);
			double p1 = v1/(v1+v2);
			if (p1 >= 0.95) {
//				System.out.print((observation.humanLabel == observation.modelLabel) ? "Correct": "");
//				System.out.println(observation.originalData + "\n" + p1*100 + " " + maxLabel + "\n---------------");
				if (maxLabel == 0) {
					negWriter.write(observation.originalData + " " + maxLabel + "\n");
				} else {
					posWriter.write(observation.originalData + " " + maxLabel + "\n");
				}
			}
		}
	}

	/**
	 * Getting the class distribution for a given observation.
	 * 
	 * @param observation
	 *            the input observation
	 * @return A probability distribution over classes of the input observation
	 */
	public synchronized List<PairIntDouble> getDistribution(Observation observation) {
		List<PairIntDouble> probs = new ArrayList<PairIntDouble>();

		int i;

		for (i = 0; i < numLabels; i++) {
			temp[i] = 0.0;
		}

		model.featureGenerator.startScanFeatures(observation);
		while (model.featureGenerator.hasNextFeature()) {
			Feature feature = model.featureGenerator.nextFeature();

			temp[feature.label] += model.lambda[feature.index] * feature.value;
		}

		for (i = 0; i < numLabels; i++) {
			probs.add(new PairIntDouble(i, temp[i]));
		}

		return probs;
	}

	/**
	 * Doing inference for a list of observations.
	 * 
	 * @param data
	 *            a list of input data observations
	 */
	public void doInference(List<Observation> data) {
		try {
			PrintWriter negWriter = new PrintWriter(model.option.negOutputFile, "UTF-8");
			PrintWriter posWriter = new PrintWriter(model.option.posOutputFile, "UTF-8");
			for (int i = 0; i < data.size(); i++) {
				Observation obsr = data.get(i);

				classify(obsr, model.option.runningMode == 3, negWriter, posWriter);
			}
			negWriter.close();
			posWriter.close();
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}
}
