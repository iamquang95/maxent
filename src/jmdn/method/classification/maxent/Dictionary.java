/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Dictionary {
	protected Map<Integer, Element> dictionary = null;
	protected Option option = null;
	protected Data data = null;

	/**
	 * Default class constructor.
	 */
	public Dictionary() {
		dictionary = new HashMap<Integer, Element>();
	}

	/**
	 * Class constructor.
	 * 
	 * @param option
	 *            the reference to the option object
	 * @param data
	 *            the reference to the data object
	 */
	public Dictionary(Option option, Data data) {
		this.option = option;
		this.data = data;
		dictionary = new HashMap<Integer, Element>();
	}

	/**
	 * Clearing the dictionary.
	 */
	protected void clear() {
		dictionary.clear();
	}

	/**
	 * Reading the dictionary from an input stream.
	 * 
	 * @param bufferedReader
	 *            the input stream from which the dictionary is read
	 * @throws IOException
	 *             IO exception
	 */
	protected void readDictionary(BufferedReader bufferedReader) throws IOException {
		clear();

		String line;

		// reading the size of the dictionary
		if ((line = bufferedReader.readLine()) == null) {
			System.out.println("No information about the size of the dictionary!");
			return;
		}

		int dictSize = Integer.parseInt(line);
		if (dictSize <= 0) {
			System.out.println("Invalid dictionary size!");
		}

		System.out.println("Reading the dictionary ...");

		// main loop for reading dictionary content
		for (int i = 0; i < dictSize; i++) {
			line = bufferedReader.readLine();

			if (line == null) {
				System.out.println("An invalid dictionary line!");
				return;
			}

			StringTokenizer strTok = new StringTokenizer(line, " \t\r\n");
			int len = strTok.countTokens();
			if (len < 2) {
				// an invalid line
				continue;
			}

			StringTokenizer cpTok = new StringTokenizer(strTok.nextToken(), ":");
			int cp = Integer.parseInt(cpTok.nextToken());
			int cpCount = Integer.parseInt(cpTok.nextToken());

			// create a new element
			Element elem = new Element(cpCount, 1);

			while (strTok.hasMoreTokens()) {
				StringTokenizer lbTok = new StringTokenizer(strTok.nextToken(), ":");

				int label = Integer.parseInt(lbTok.nextToken());
				int count = Integer.parseInt(lbTok.nextToken());
				int fidx = Integer.parseInt(lbTok.nextToken());
				CountFIdx cntFIdx = new CountFIdx(count, fidx);

				elem.lbCntFidxes.put(new Integer(label), cntFIdx);
			}

			// insert the element to the dictionary
			dictionary.put(new Integer(cp), elem);
		}

		System.out.println("Reading dictionary (" + Integer.toString(dictionary.size()) + " entries) completed!");

		// read the line ###...
		bufferedReader.readLine();
	}

	/**
	 * Saving the dictionary to an output stream.
	 * 
	 * @param fout
	 *            the output stream to which the dictionary is saved
	 * @throws IOException
	 *             IO exception
	 */
	protected void writeDictionary(PrintWriter fout) throws IOException {
		Iterator<Integer> it;
		int count = 0;

		for (it = dictionary.keySet().iterator(); it.hasNext();) {
			Integer cpInt = (Integer) it.next();
			Element elem = (Element) dictionary.get(cpInt);

			if (elem.chosen == 1) {
				count++;
			}
		}

		// write the dictionary size
		fout.println(Integer.toString(count));

		for (it = dictionary.keySet().iterator(); it.hasNext();) {
			Integer cpInt = (Integer) it.next();
			Element elem = (Element) dictionary.get(cpInt);

			if (elem.chosen == 0) {
				continue;
			}

			// write the context predicate and its count
			fout.print(cpInt.toString() + ":" + Integer.toString(elem.count));

			for (Iterator<Integer> lbIt = elem.lbCntFidxes.keySet().iterator(); lbIt.hasNext();) {
				Integer labelInt = (Integer) lbIt.next();
				CountFIdx cntFIdx = (CountFIdx) elem.lbCntFidxes.get(labelInt);

				if (cntFIdx.fidx < 0) {
					continue;
				}

				fout.print(" " + labelInt.toString() + ":" + Integer.toString(cntFIdx.count) + ":"
						+ Integer.toString(cntFIdx.fidx));
			}

			fout.println();
		}

		// write the line ###...
		fout.println(Option.modelSeparator);
	}

	/**
	 * Adding a context predicate (and the label it supports) to the dictionary.
	 * 
	 * @param contextPredicate
	 *            the context predicate
	 * @param label
	 *            the label associated with the context predicate
	 * @param count
	 *            the count of the pair <context predicate and the label>
	 */
	protected void addDictionary(int contextPredicate, int label, int count) {
		Element elem = (Element) dictionary.get(new Integer(contextPredicate));

		if (elem == null) {
			// if the context predicate is not found
			elem = new Element();
			elem.count = count;

			CountFIdx cntFIdx = new CountFIdx(count, -1);
			elem.lbCntFidxes.put(new Integer(label), cntFIdx);

			// insert the new element to the dict
			dictionary.put(new Integer(contextPredicate), elem);

		} else {
			// update the total count
			elem.count += count;

			CountFIdx cntFIdx = (CountFIdx) elem.lbCntFidxes.get(new Integer(label));
			if (cntFIdx == null) {
				// the label not found
				cntFIdx = new CountFIdx(count, -1);
				elem.lbCntFidxes.put(new Integer(label), cntFIdx);

			} else {
				// if label found, update the count only
				cntFIdx.count += count;
			}
		}
	}

	/**
	 * Generating the dictionary.
	 */
	protected void generateDictionary() {
		if (data.trnData == null) {
			System.out.println("No data available for generating the dictionary!");
			return;
		}

		// scan all data observations of the training data
		for (int i = 0; i < data.trnData.size(); i++) {
			Observation obsr = (Observation) data.trnData.get(i);

			for (int j = 0; j < obsr.contextPredicates.length; j++) {
				addDictionary(obsr.contextPredicates[j], obsr.humanLabel, 1);
			}
		}
	}

	/**
	 * Getting the size of the dictionary.
	 * 
	 * @return The size of the dictionary
	 */
	protected int size() {
		if (dictionary == null) {
			return 0;
		} else {
			return dictionary.size();
		}
	}
}
