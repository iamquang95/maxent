/**
 * 
 */

package jmdn.method.classification.maxent;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Feature {
	protected int index = -1; // the feature's index
	protected String stringId = ""; // the string identifier of the feature
	protected int label = -1; // the label of the feature
	protected int contextPredicate = -1; // the context predicate
	protected float value = 1; // the feature value
	protected double weight = 0.0; // the feature weight

	/**
	 * Default class constructor.
	 */
	public Feature() {
		index = -1;
		stringId = "";
		label = -1;
		contextPredicate = -1;
		value = 1;
		weight = 0.0;
	}

	/**
	 * Class constructor.
	 * 
	 * @param label
	 *            the label of the feature
	 * @param contextPredicate
	 *            the context predicate of the feature
	 */
	public Feature(int label, int contextPredicate) {
		FeatureInit(label, contextPredicate);
	}

	/**
	 * Class constructor.
	 * 
	 * @param label
	 *            the label of the feature
	 * @param contextPredicate
	 *            the context predicate of the feature
	 * @param featureMap
	 *            the feature map
	 */
	public Feature(int label, int contextPredicate, Map<String, Integer> featureMap) {
		FeatureInit(label, contextPredicate);
		strId2IdxAdd(featureMap);
	}

	// create a feature from a string (e.g., reading from model file)
	/**
	 * Class constructor.
	 * 
	 * @param string
	 *            the string representing the feature
	 * @param cpStr2Int
	 *            the map from context predicate string to the id of that
	 *            context predicate
	 * @param lbStr2Int
	 *            the map from label string to the id of that label
	 */
	public Feature(String string, Map<String, Integer> cpStr2Int, Map<String, Integer> lbStr2Int) {
		FeatureInit(string, cpStr2Int, lbStr2Int);
	}

	/**
	 * Class constructor.
	 * 
	 * @param string
	 *            the string representing the feature
	 * @param cpStr2Int
	 *            the map from context predicate string to the id of that
	 *            context predicate
	 * @param lbStr2Int
	 *            the map from label string to the id of that label
	 * @param featureMap
	 *            the feature map
	 */
	public Feature(String string, Map<String, Integer> cpStr2Int, Map<String, Integer> lbStr2Int,
			Map<String, Integer> featureMap) {
		FeatureInit(string, cpStr2Int, lbStr2Int);
		strId2IdxAdd(featureMap);
	}

	/**
	 * Initializing the feature.
	 * 
	 * @param label
	 *            the label of the feature
	 * @param contextPredicate
	 *            the context predicate of the feature
	 */
	protected final void FeatureInit(int label, int contextPredicate) {
		this.label = label;
		this.contextPredicate = contextPredicate;
		stringId = Integer.toString(label) + " " + Integer.toString(contextPredicate);
	}

	/**
	 * Initializing the feature.
	 * 
	 * @param string
	 *            the string representing the feature
	 * @param cpStr2Int
	 *            the map from context predicate string to the id of that
	 *            context predicate
	 * @param lbStr2Int
	 *            the map from label string to the id of that label
	 */
	protected final void FeatureInit(String string, Map<String, Integer> cpStr2Int, Map<String, Integer> lbStr2Int) {
		StringTokenizer strTok = new StringTokenizer(string, " \t\r\n");
		// <label> <context predicate> <index> <weight>

		int len = strTok.countTokens();
		if (len != 4) {
			return;
		}

		String labelStr = strTok.nextToken();
		String cpStr = strTok.nextToken();
		int index = Integer.parseInt(strTok.nextToken());
		float value = 1;
		double weight = Double.parseDouble(strTok.nextToken());

		Integer labelInt = (Integer) lbStr2Int.get(labelStr);
		Integer cpInt = (Integer) cpStr2Int.get(cpStr);
		FeatureInit(labelInt.intValue(), cpInt.intValue());

		this.index = index;
		this.value = value;
		this.weight = weight;
	}

	/**
	 * Mapping from the string identifier of the feature to its index.
	 * 
	 * @param featureMap
	 *            the feature map
	 * @return The index of the feature
	 */
	protected final int strId2Idx(Map<String, Integer> featureMap) {
		Integer idxInt = (Integer) featureMap.get(stringId);
		if (idxInt != null) {
			this.index = idxInt.intValue();
		}

		return this.index;
	}

	/**
	 * Mapping from the string identifier of the feature to its index, and
	 * adding feature to the feature map if the mapping does not exist.
	 * 
	 * @param featureMap
	 *            the feature map
	 * @return The index of the feature
	 */
	protected final int strId2IdxAdd(Map<String, Integer> featureMap) {
		strId2Idx(featureMap);

		if (index < 0) {
			index = featureMap.size();
			featureMap.put(stringId, new Integer(index));
		}

		return index;
	}

	/**
	 * Getting the index of the feature.
	 * 
	 * @param featureMap
	 *            the feature map
	 * @return The index of the feature
	 */
	protected int index(Map<String, Integer> featureMap) {
		return strId2Idx(featureMap);
	}

	/**
	 * Converting feature to string: [label] [context predicate] [index]
	 * [weight].
	 * 
	 * @param cpInt2Str
	 *            the map from the identifier of the context predicates to their
	 *            text strings
	 * @param lbInt2Str
	 *            the map from the identifier of the labels to their text
	 *            strings
	 * @return A text string representing the feature
	 */
	public String toString(Map<Integer, String> cpInt2Str, Map<Integer, String> lbInt2Str) {
		String featureStr = "";

		String labelStr = (String) lbInt2Str.get(new Integer(label));
		if (labelStr != null) {
			featureStr += labelStr + " ";
		}

		String cpStr = (String) cpInt2Str.get(new Integer(contextPredicate));
		if (cpStr != null) {
			featureStr += cpStr + " ";
		}

		featureStr += Integer.toString(index) + " " + Double.toString(weight);

		return featureStr;
	}
}
