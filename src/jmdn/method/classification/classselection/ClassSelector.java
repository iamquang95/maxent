/**
 * 
 */

package jmdn.method.classification.classselection;

import java.util.*;

import jmdn.base.struct.pair.PairIntDouble;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class ClassSelector {
	private List<PairIntDouble> probabilities = null;
	private Map<Integer, Double> originalValueMap = null;

	/**
	 * Default class constructor.
	 */
	public ClassSelector() {
		probabilities = new ArrayList<PairIntDouble>();
		originalValueMap = new HashMap<Integer, Double>();
	}

	/**
	 * Class constructor.
	 * 
	 * @param probabilities
	 *            a list of probability distribution for class selection
	 */
	public ClassSelector(List<PairIntDouble> probabilities) {
		this();

		for (int i = 0; i < probabilities.size(); i++) {
			PairIntDouble pair = probabilities.get(i);
			this.probabilities.add(pair);
			this.originalValueMap.put(pair.first, pair.second);
		}
	}

	/**
	 * Class constructor.
	 * 
	 * @param probabilities
	 *            a map of probability distribution for class selection
	 */
	public ClassSelector(Map<Integer, Double> probabilities) {
		this();

		for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
			this.probabilities.add(new PairIntDouble(entry.getKey(), entry.getValue()));
		}
	}

	/**
	 * Getting the size of the class distribution.
	 * 
	 * @return The size of the class distribution
	 */
	public int size() {
		return probabilities.size();
	}

	/**
	 * Normalizing the probability distribution.
	 */
	public void normalize() {
		int count = size();

		double min = probabilities.get(0).second;
		for (int i = 1; i < count; i++) {
			double temp = probabilities.get(i).second;
			if (min > temp) {
				min = temp;
			}
		}

		if (min < 0) {
			min = Math.abs(min);
			for (int i = 0; i < count; i++) {
				PairIntDouble pair = probabilities.get(i);
				pair.second += min;
				probabilities.set(i, pair);
			}
		}

		double sum = 0.0;
		for (int i = 0; i < count; i++) {
			sum += probabilities.get(i).second;
		}

		if (sum <= 0) {
			return;
		}

		for (int i = 0; i < count; i++) {
			PairIntDouble pair = probabilities.get(i);
			pair.second /= sum;
			probabilities.set(i, pair);
		}
	}

	/**
	 * Sorting the probability distribution.
	 */
	public void sort() {
		Collections.sort(probabilities);
	}

	/**
	 * Selecting appropriate classes. First, the probability distribution is
	 * normalized and sorted in descending order. The class selection starts
	 * with the class with the highest probability at the left most. The
	 * selection process will be stopped if one of the following criteria is
	 * satisfied: (1) the number of selected classes = maxNumClassesSelected;
	 * (2) the sum of probability values of the selected classes is greater than
	 * cumulativeThreshold; or (3) the ratio between the probability of the last
	 * selected class and that of the class right after it is greater than
	 * times.
	 * 
	 * @param maxNumClassesSelected
	 *            the maximum number of classes that can be selected
	 * @param cumulativeThreshold
	 *            the probability cumulative threshold
	 * @param times
	 *            the ratio between one probability and the one on the right (in
	 *            the sorted probability distribution)
	 * @return A list of selected classes and their probability values
	 */
	public List<PairIntDouble> select(int maxNumClassesSelected, double cumulativeThreshold, double times) {
		List<PairIntDouble> results = new ArrayList<PairIntDouble>();

		normalize();
		sort();

		int count = size();
		if (maxNumClassesSelected > count) {
			maxNumClassesSelected = count;
		}

		double cumulativeValue = 0.0;
		double lastValue = 0.0;

		for (int i = count - 1; i >= count - maxNumClassesSelected; i--) {
			PairIntDouble pair = probabilities.get(i);

			if (cumulativeValue > cumulativeThreshold) {
				break;
			}

			double currentValue = pair.second;
			if (currentValue > 0 && (lastValue / currentValue) > times) {
				break;
			}

			PairIntDouble newPair = new PairIntDouble(pair.first, originalValueMap.get(pair.first));
			results.add(newPair);

			cumulativeValue += pair.second;
			lastValue = currentValue;
		}

		return results;
	}

	/**
	 * Selecting the class with the highest probability.
	 * 
	 * @return The selected class and its probability value
	 */
	public List<PairIntDouble> selectOne() {
		List<PairIntDouble> results = new ArrayList<PairIntDouble>();

		PairIntDouble pairMax = new PairIntDouble(-1, 0.0);
		for (int i = 0; i < probabilities.size(); i++) {
			PairIntDouble pairTemp = probabilities.get(i);
			if (pairTemp.second > pairMax.second) {
				pairMax = pairTemp;
			}
		}

		if (pairMax.second > 0.0) {
			results.add(pairMax);
		}

		return results;
	}
}
