/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class FeatureGenerator {
	protected List<Feature> features = null;
	protected Map<String, Integer> featureMap = null;
	protected Option option = null;
	protected Data data = null;
	protected Dictionary dictionary = null;

	// for scanning features only
	protected List<Feature> currentFeatures = null;
	protected int currentFeatureIdx = 0;

	/**
	 * Class constructor.
	 * 
	 * @param option
	 *            the reference to the option object
	 * @param data
	 *            the reference to the data object
	 * @param dictionary
	 *            the reference to the dictionary object
	 */
	public FeatureGenerator(Option option, Data data, Dictionary dictionary) {
		this.option = option;
		this.data = data;
		this.dictionary = dictionary;
	}

	/**
	 * Adding a feature to the feature list.
	 * 
	 * @param feature
	 *            the feature to be added
	 */
	protected void addFeature(Feature feature) {
		feature.strId2IdxAdd(featureMap);
		features.add(feature);
	}

	/**
	 * Generating the features.
	 */
	protected void generateFeatures() {
		if (features != null) {
			features.clear();
		} else {
			features = new ArrayList<Feature>();
		}

		if (featureMap != null) {
			featureMap.clear();
		} else {
			featureMap = new HashMap<String, Integer>();
		}

		if (currentFeatures != null) {
			currentFeatures.clear();
		} else {
			currentFeatures = new ArrayList<Feature>();
		}

		if (data.trnData == null || dictionary.dictionary == null) {
			System.out.println("No data or dictionary for generating features!");
			return;
		}

		// scan over data list
		for (int i = 0; i < data.trnData.size(); i++) {
			Observation obsr = (Observation) data.trnData.get(i);

			for (int j = 0; j < obsr.contextPredicates.length; j++) {
				Element elem;
				CountFIdx cntFIdx;

				elem = (Element) dictionary.dictionary.get(new Integer(obsr.contextPredicates[j]));
				if (elem != null) {
					if (elem.count <= option.cpRareThreshold) {
						// skip this context predicate, it is too rare
						continue;
					}

					cntFIdx = (CountFIdx) elem.lbCntFidxes.get(new Integer(obsr.humanLabel));
					if (cntFIdx != null) {
						if (cntFIdx.count <= option.fRareThreshold) {
							// skip this feature, it is too rare
							continue;
						}

					} else {
						// not found in the dictionary, then skip
						continue;
					}

				} else {
					// not found in the dictionary, then skip
					continue;
				}

				// update the feature
				Feature f = new Feature(obsr.humanLabel, obsr.contextPredicates[j]);
				f.strId2Idx(featureMap);
				if (f.index < 0) {
					// new feature, add to the feature list
					addFeature(f);

					// update the feature index in the dictionary
					cntFIdx.fidx = f.index;
					elem.chosen = 1;
				}
			}
		}

		option.numFeatures = features.size();
	}

	/**
	 * Getting the number of features.
	 * 
	 * @return The number of features
	 */
	protected int numFeatures() {
		if (features == null) {
			return 0;
		} else {
			return features.size();
		}
	}

	/**
	 * Reading features from an input stream.
	 * 
	 * @param bufferedReader
	 *            the input stream from which the features are read
	 * @throws IOException
	 *             IO exception
	 */
	protected void readFeatures(BufferedReader bufferedReader) throws IOException {
		if (features != null) {
			features.clear();
		} else {
			features = new ArrayList<Feature>();
		}

		if (featureMap != null) {
			featureMap.clear();
		} else {
			featureMap = new HashMap<String, Integer>();
		}

		if (currentFeatures != null) {
			currentFeatures.clear();
		} else {
			currentFeatures = new ArrayList<Feature>();
		}

		String line;

		// get the number of features
		if ((line = bufferedReader.readLine()) == null) {
			System.out.println("Unknown number of features!");
			return;
		}
		int numFeatures = Integer.parseInt(line);
		if (numFeatures <= 0) {
			System.out.println("Invalid number of features!");
			return;
		}

		System.out.println("Reading features ...");

		// main loop for reading features
		for (int i = 0; i < numFeatures; i++) {
			line = bufferedReader.readLine();
			if (line == null) {
				// invalid feature line, ignore it
				continue;
			}

			StringTokenizer strTok = new StringTokenizer(line, " ");
			if (strTok.countTokens() != 4) {
				// invalid feature line, ignore it
				continue;
			}

			// create a new feature by parsing the line
			Feature f = new Feature(line, data.getCpStr2Int(), data.getLbStr2Int());

			Integer fidx = (Integer) featureMap.get(f.stringId);
			if (fidx == null) {
				// insert the feature into the feature map
				featureMap.put(f.stringId, new Integer(f.index));
				features.add(f);
			}
		}

		System.out.println("Reading " + Integer.toString(features.size()) + " features completed!");

		// read the line ###...
		bufferedReader.readLine();

		option.numFeatures = features.size();
	}

	/**
	 * Saving features to an output stream.
	 * 
	 * @param fout
	 *            the output stream to which the features will be saved
	 * @throws IOException
	 *             IO exception
	 */
	protected void writeFeatures(PrintWriter fout) throws IOException {
		// write the number of features
		fout.println(Integer.toString(features.size()));

		for (int i = 0; i < features.size(); i++) {
			Feature f = (Feature) features.get(i);
			fout.println(f.toString(data.getCpInt2Str(), data.getLbInt2Str()));
		}

		// write the line ###...
		fout.println(Option.modelSeparator);
	}

	/**
	 * Resetting for scanning features.
	 */
	protected void scanReset() {
		currentFeatureIdx = 0;
	}

	/**
	 * Starting to scan feature of a given observation.
	 * 
	 * @param observation
	 *            the observation from which the features will be scanned
	 */
	protected void startScanFeatures(Observation observation) {
		currentFeatures.clear();
		currentFeatureIdx = 0;

		// scan over all context predicates
		for (int i = 0; i < observation.contextPredicates.length; i++) {
			Element elem = (Element) dictionary.dictionary.get(new Integer(observation.contextPredicates[i]));
			if (elem == null) {
				continue;
			}

			if (!(elem.isScanned)) {
				// scan all labels for features
				Iterator<Integer> it = elem.lbCntFidxes.keySet().iterator();
				while (it.hasNext()) {
					Integer labelInt = (Integer) it.next();
					CountFIdx cntFIdx = (CountFIdx) elem.lbCntFidxes.get(labelInt);

					if (cntFIdx.fidx >= 0) {
						Feature feature = new Feature();
						feature.FeatureInit(labelInt.intValue(), observation.contextPredicates[i]);
						feature.index = cntFIdx.fidx;

						elem.cpFeatures.add(feature);
					}
				}

				elem.isScanned = true;
			}

			for (int j = 0; j < elem.cpFeatures.size(); j++) {
				currentFeatures.add(elem.cpFeatures.get(j));
			}
		}
	}

	/**
	 * Indicating there is a next feature or not.
	 * 
	 * @return True if there is a next feature, false otherwise
	 */
	protected boolean hasNextFeature() {
		return (currentFeatureIdx < currentFeatures.size());
	}

	/**
	 * Getting the next feature.
	 * 
	 * @return The next feature
	 */
	protected Feature nextFeature() {
		Feature feature = (Feature) currentFeatures.get(currentFeatureIdx);
		currentFeatureIdx++;

		return feature;
	}
}
