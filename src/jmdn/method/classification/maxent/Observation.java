/**
 * 
 */

package jmdn.method.classification.maxent;

import java.util.List;
import java.util.Map;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Observation {
	protected String originalData = "";
	protected int[] contextPredicates = null;
	protected int humanLabel = -1;
	protected int modelLabel = -1;

	/**
	 * Default class constructor.
	 */
	public Observation() {
		// do nothing
	}

	/**
	 * Class constructor.
	 * 
	 * @param contextPredicates
	 *            a list of context predicates
	 */
	public Observation(int[] contextPredicates) {
		this.contextPredicates = new int[contextPredicates.length];
		System.arraycopy(contextPredicates, 0, this.contextPredicates, 0, contextPredicates.length);
	}

	/**
	 * Class constructor.
	 * 
	 * @param contextPredicates
	 *            a list of context predicates
	 */
	public Observation(List<Integer> contextPredicates) {
		this.contextPredicates = new int[contextPredicates.size()];

		for (int i = 0; i < contextPredicates.size(); i++) {
			this.contextPredicates[i] = ((Integer) contextPredicates.get(i)).intValue();
		}
	}

	/**
	 * Class constructor.
	 * 
	 * @param humanLabel
	 *            the human label of the observation
	 * @param contextPredicates
	 *            a list of context predicates
	 */
	public Observation(int humanLabel, int[] contextPredicates) {
		this.humanLabel = humanLabel;
		this.contextPredicates = new int[contextPredicates.length];
		System.arraycopy(contextPredicates, 0, this.contextPredicates, 0, contextPredicates.length);
	}

	/**
	 * Getting the whole observation represented in a text string.
	 * 
	 * @param lbInt2Str
	 *            The map from label index to label string
	 * @return A text string representing the observation
	 */
	public String toString(Map<Integer, String> lbInt2Str) {
		String res = originalData;

		String modelLabelStr = (String) lbInt2Str.get(new Integer(modelLabel));
		if (modelLabelStr != null) {
			res += Option.labelSeparator + modelLabelStr;
		}

		return res;
	}
}
