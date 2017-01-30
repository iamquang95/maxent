/**
 * 
 */

package jmdn.method.classification.maxent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Element {
	protected int count = 0; // the number of occurrences of this context
								// predicate
	protected int chosen = 0; // indicating whether or not it is incorporated
								// into the model

	protected Map<Integer, CountFIdx> lbCntFidxes = null; // map of labels to
															// CountFeatureIdxes

	protected List<Feature> cpFeatures = null; // features associated with this
												// context predicates
	protected boolean isScanned = false; // be scanned or not

	/**
	 * Default class constructor.
	 */
	public Element() {
		lbCntFidxes = new HashMap<Integer, CountFIdx>();
		cpFeatures = new ArrayList<Feature>();
	}

	/**
	 * Class constructor.
	 * 
	 * @param count
	 *            the number of occurrences of this context predicate
	 * @param chosen
	 *            a flag indicating whether or not this context predicate is
	 *            incorporated into the model
	 */
	public Element(int count, int chosen) {
		this();

		this.count = count;
		this.chosen = chosen;
	}
}
