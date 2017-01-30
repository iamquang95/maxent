/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Model {
	protected Option option = null;
	protected Data data = null;
	protected Dictionary dictionary = null;
	protected FeatureGenerator featureGenerator = null;
	protected Train train = null;
	protected Inference inference = null;
	protected Evaluation evaluation = null;

	// feature weight
	protected double[] lambda = null;

	/**
	 * Default class constructor.
	 */
	public Model() {
		// do nothing
	}

	/**
	 * Class constructor.
	 * 
	 * @param option
	 *            the reference to the option object
	 * @param data
	 *            the reference to the data object
	 * @param dictionary
	 *            the reference to the dictionary object
	 * @param featureGenerator
	 *            the reference to the feature generator object
	 * @param train
	 *            the reference to the training object
	 * @param inference
	 *            the reference to the inference object
	 * @param evaluation
	 *            the reference to the evaluation object
	 */
	public Model(Option option, Data data, Dictionary dictionary, FeatureGenerator featureGenerator, Train train,
			Inference inference, Evaluation evaluation) {
		this.option = option;
		this.data = data;
		this.dictionary = dictionary;
		this.featureGenerator = featureGenerator;
		this.evaluation = evaluation;

		if (train != null) {
			this.train = train;
			this.train.model = this;
			this.train.init();
		}

		if (inference != null) {
			this.inference = inference;
			this.inference.model = this;
			this.inference.init();
		}

		if (evaluation != null) {
			this.evaluation = evaluation;
			this.evaluation.model = this;
			this.evaluation.init();
		}
	}

	/**
	 * Training the model.
	 * 
	 * @param fout
	 *            the output stream for logging information during training
	 */
	public void doTrain(PrintWriter fout) {
		if (lambda == null) {
			lambda = new double[featureGenerator.numFeatures()];
		}

		// call this to train
		train.doTrain(fout);

		// call this to update the feature weights
		updateFeatures();
	}

	/**
	 * Updating the feature weights.
	 */
	protected void updateFeatures() {
		for (int i = 0; i < featureGenerator.features.size(); i++) {
			Feature feature = (Feature) featureGenerator.features.get(i);
			feature.weight = lambda[feature.index];
		}
	}

	/**
	 * Initializing for doing inference.
	 */
	public void initInference() {
		if (lambda == null) {
			lambda = new double[featureGenerator.numFeatures()];

			// reading feature weights from the feature list
			for (int i = 0; i < featureGenerator.features.size(); i++) {
				Feature feature = (Feature) featureGenerator.features.get(i);
				lambda[feature.index] = feature.weight;
			}
		}
	}

	/**
	 * Doing inference for new data.
	 * 
	 * @param data
	 *            the list of observations to be inferred
	 */
	public void doInference(List<Observation> data) {
		if (lambda == null) {
			lambda = new double[featureGenerator.numFeatures()];

			// reading feature weights from the feature list
			for (int i = 0; i < featureGenerator.features.size(); i++) {
				Feature feature = (Feature) featureGenerator.features.get(i);
				lambda[feature.index] = feature.weight;
			}
		}

		inference.doInference(data);
	}

}
