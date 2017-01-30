/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jmdn.method.classification.classselection.ClassSelector;
import jmdn.base.struct.pair.PairIntDouble;
import jmdn.base.struct.pair.PairStrDouble;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Classification {
	private Option option = null;
	private Data data = null;
	private Dictionary dictionary = null;
	private FeatureGenerator featureGenerator = null;
	private Inference inference = null;
	private Model model = null;

	private boolean initialized = false;

	private BufferedReader finModel = null;

	private List<Integer> intCps = null;

	/**
	 * Class constructor.
	 * 
	 * @param modelDirectory
	 *            the directory containing the trained model
	 */
	public Classification(String modelDirectory) {
		option = new Option(modelDirectory);
		option.readOptions();
	}

	/**
	 * Indicating the model has been initialized or not.
	 * 
	 * @return True if the model has been initialized already, false otherwise
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Initializing for classification.
	 */
	public void init() {
		try {
			// open model file
			finModel = option.openModelFile();
			if (finModel == null) {
				System.out.println("Couldn't open model file");
				return;
			}

			data = new Data(option);
			// read context predicate map
			data.readCpMaps(finModel);
			// read label map
			data.readLbMaps(finModel);

			dictionary = new Dictionary(option, data);
			// read dictionary
			dictionary.readDictionary(finModel);

			featureGenerator = new FeatureGenerator(option, data, dictionary);
			// read features
			featureGenerator.readFeatures(finModel);

			// create an inference object
			inference = new Inference();

			// create a model object
			model = new Model(option, data, dictionary, featureGenerator, null, inference, null);
			model.initInference();

			// close model file
			finModel.close();

		} catch (IOException e) {
			System.out.println("Couldn't load the model, check the model file again");
			System.out.println(e.toString());
		}

		intCps = new ArrayList<Integer>();

		initialized = true;
	}

	/**
	 * Getting the option object.
	 * 
	 * @return The reference to the option object
	 */
	public Option getOption() {
		return option;
	}

	/**
	 * Doing classification for a text string consisting of context predicates
	 * extracted from a data observation.
	 * 
	 * @param contextPredicates
	 *            the text string consisting of context predicates
	 * @return A class label that is most appropriate for the observation
	 */
	public String classify(String contextPredicates) {
		String modelLabel = "";
		int i;

		intCps.clear();

		StringTokenizer strTok = new StringTokenizer(contextPredicates, " \t\r\n");
		int count = strTok.countTokens();

		for (i = 0; i < count; i++) {
			String cpStr = strTok.nextToken();
			Integer cpInt = (Integer) data.getCpStr2Int().get(cpStr);
			if (cpInt != null) {
				intCps.add(cpInt);
			}
		}

		Observation obsr = new Observation(intCps);

		// doing classification
		inference.classify(obsr);

		String lbStr = (String) data.getLbInt2Str().get(new Integer(obsr.modelLabel));
		if (lbStr != null) {
			modelLabel = lbStr;
		}

		return modelLabel;
	}

	/**
	 * Doing classification for a list of text strings. Each text string
	 * consists of context predicates extracted from a data observation.
	 * 
	 * @param data
	 *            the list of text strings
	 * @return A list of class labels corresponding to the list of text strings
	 *         (data)
	 */
	public List<String> classify(List<String> data) {
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < data.size(); i++) {
			list.add(classify(data.get(i)));
		}

		return list;
	}

	/**
	 * Doing classification for a text string consisting of context predicates
	 * extracted from a data observation. The classification may return one or
	 * several class labels that are appropriate for the observation. see the
	 * method ClassSelector.select for a deeper understanding of this method.
	 * 
	 * @param contextPredicates
	 *            the text string containing context predicates
	 * @param maxNumClassesSelected
	 *            the maximum number of classes can be selected
	 * @param cumulativeThreshold
	 *            the cumulative threshold for the sum of probability values of
	 *            the selected classes
	 * @param times
	 *            the threshold for the ratio between the probability of the
	 *            last selected class and that of the class right after it
	 * @return A list of class labels that are appropriate for the observation
	 */
	public List<PairStrDouble> classifyMultiLabels(String contextPredicates, int maxNumClassesSelected,
			double cumulativeThreshold, double times) {
		List<PairStrDouble> labelsAndWeights = new ArrayList<PairStrDouble>();

		int i;

		intCps.clear();

		StringTokenizer strTok = new StringTokenizer(contextPredicates, " \t\r\n");
		int count = strTok.countTokens();

		for (i = 0; i < count; i++) {
			String cpStr = strTok.nextToken();
			Integer cpInt = (Integer) data.getCpStr2Int().get(cpStr);
			if (cpInt != null) {
				intCps.add(cpInt);
			}
		}

		Observation obsr = new Observation(intCps);

		List<PairIntDouble> probs = inference.getDistribution(obsr);

		ClassSelector selector = new ClassSelector(probs);

		List<PairIntDouble> selectedClasses = selector.select(maxNumClassesSelected, cumulativeThreshold, times);

		for (i = 0; i < selectedClasses.size(); i++) {
			PairIntDouble pair = selectedClasses.get(i);

			String lbStr = (String) data.getLbInt2Str().get(pair.first);
			if (lbStr != null) {
				labelsAndWeights.add(new PairStrDouble(lbStr, pair.second));
			}
		}

		return labelsAndWeights;
	}
}
