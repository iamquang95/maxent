/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import jmdn.base.util.filesystem.FileLoader;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Data {
	private Option option = null;

	private Map<String, Integer> lbStr2Int = null;
	private Map<Integer, String> lbInt2Str = null;
	private Map<String, Integer> cpStr2Int = null;
	private Map<Integer, String> cpInt2Str = null;

	protected List<Observation> trnData = null;
	protected List<Observation> tstData = null;
	protected List<Observation> ulbData = null;

	/**
	 * Class constructor.
	 * 
	 * @param option
	 *            the reference to the option object
	 */
	public Data(Option option) {
		this.option = option;
	}

	/**
	 * Getting the reference to the map from label string to label id.
	 * 
	 * @return The reference to the map
	 */
	protected Map<String, Integer> getLbStr2Int() {
		return lbStr2Int;
	}

	/**
	 * Getting the reference to the map from label id to label string.
	 * 
	 * @return The reference to the map
	 */
	protected Map<Integer, String> getLbInt2Str() {
		return lbInt2Str;
	}

	/**
	 * Getting the reference to the map from context predicate string to context
	 * predicate id.
	 * 
	 * @return The reference to the map
	 */
	protected Map<String, Integer> getCpStr2Int() {
		return cpStr2Int;
	}

	/**
	 * Getting the reference to the map from context predicate id to context
	 * predicate string.
	 * 
	 * @return The reference to the map
	 */
	protected Map<Integer, String> getCpInt2Str() {
		return cpInt2Str;
	}

	/**
	 * Reading the map of context predicates from an input stream.
	 * 
	 * @param bufferedReader
	 *            the input stream from which the map of context predicates is
	 *            read
	 * @throws IOException
	 *             IO exception
	 */
	protected void readCpMaps(BufferedReader bufferedReader) throws IOException {
		if (cpStr2Int != null) {
			cpStr2Int.clear();
		} else {
			cpStr2Int = new HashMap<String, Integer>();
		}

		if (cpInt2Str != null) {
			cpInt2Str.clear();
		} else {
			cpInt2Str = new HashMap<Integer, String>();
		}

		String line;

		// get size of the map
		if ((line = bufferedReader.readLine()) == null) {
			System.out.println("No information about the size of context predicate map!");
			return;
		}

		int numCps = Integer.parseInt(line);
		if (numCps <= 0) {
			System.out.println("Invalid size of context predicate map!");
			return;
		}

		System.out.println("Reading the map of context predicates ...");

		for (int i = 0; i < numCps; i++) {
			line = bufferedReader.readLine();
			if (line == null) {
				System.out.println("Invalid context predicate map line");
				return;
			}

			StringTokenizer strTok = new StringTokenizer(line, " \t\r\n");
			if (strTok.countTokens() != 2) {
				continue;
			}

			String cpStr = strTok.nextToken();
			String cpInt = strTok.nextToken();

			cpStr2Int.put(cpStr, new Integer(cpInt));
			cpInt2Str.put(new Integer(cpInt), cpStr);
		}

		System.out.println("Reading the map of context predicates (" + Integer.toString(cpStr2Int.size())
				+ " entries) completed!");

		// read the line ###...
		bufferedReader.readLine();

		option.numCps = cpStr2Int.size();
	}

	/**
	 * Getting the number of context predicates.
	 * 
	 * @return The number of context predicates in the context predicate map
	 */
	protected int numCps() {
		if (cpStr2Int == null) {
			return 0;
		} else {
			return cpStr2Int.size();
		}
	}

	/**
	 * Saving the map of context predicates to an output stream.
	 * 
	 * @param dictionary
	 *            the dictionary for
	 * @param fout
	 * @throws IOException
	 */
	protected void writeCpMaps(Dictionary dictionary, PrintWriter fout) throws IOException {
		Iterator<String> it;

		if (cpStr2Int == null) {
			return;
		}

		int count = 0;
		for (it = cpStr2Int.keySet().iterator(); it.hasNext();) {
			String cpStr = (String) it.next();
			Integer cpInt = (Integer) cpStr2Int.get(cpStr);

			Element elem = (Element) dictionary.dictionary.get(cpInt);
			if (elem != null) {
				if (elem.chosen == 1) {
					count++;
				}
			}
		}

		// write the map size
		fout.println(Integer.toString(count));

		for (it = cpStr2Int.keySet().iterator(); it.hasNext();) {
			String cpStr = (String) it.next();
			Integer cpInt = (Integer) cpStr2Int.get(cpStr);

			Element elem = (Element) dictionary.dictionary.get(cpInt);
			if (elem != null) {
				if (elem.chosen == 1) {
					fout.println(cpStr + " " + cpInt.toString());
				}
			}
		}

		// write the line ###...
		fout.println(Option.modelSeparator);
	}

	/**
	 * Reading label map from an input stream.
	 * 
	 * @param bufferedReader
	 *            the input stream from which the label map is read
	 * @throws IOException
	 *             IO exception
	 */
	protected void readLbMaps(BufferedReader bufferedReader) throws IOException {
		if (lbStr2Int != null) {
			lbStr2Int.clear();
		} else {
			lbStr2Int = new HashMap<String, Integer>();
		}

		if (lbInt2Str != null) {
			lbInt2Str.clear();
		} else {
			lbInt2Str = new HashMap<Integer, String>();
		}

		String line;

		// get size of the map
		if ((line = bufferedReader.readLine()) == null) {
			System.out.println("No label map size information");
			return;
		}

		int numLabels = Integer.parseInt(line);
		if (numLabels <= 0) {
			System.out.println("Invalid label mapping size");
			return;
		}

		System.out.println("Reading the context predicate maps ...");

		for (int i = 0; i < numLabels; i++) {
			line = bufferedReader.readLine();
			if (line == null) {
				System.out.println("Invalid context predicate mapping line");
				return;
			}

			StringTokenizer strTok = new StringTokenizer(line, " \t\r\n");
			if (strTok.countTokens() != 2) {
				continue;
			}

			String lbStr = strTok.nextToken();
			String lbInt = strTok.nextToken();

			lbStr2Int.put(lbStr, new Integer(lbInt));
			lbInt2Str.put(new Integer(lbInt), lbStr);
		}

		System.out.println("Reading label maps (" + Integer.toString(lbStr2Int.size()) + " entries) completed!");

		// read the line ###...
		bufferedReader.readLine();

		option.numLabels = lbStr2Int.size();
	}

	/**
	 * Getting the number of class labels.
	 * 
	 * @return The number of class labels
	 */
	protected int numLabels() {
		if (lbStr2Int == null) {
			return 0;
		} else {
			return lbStr2Int.size();
		}
	}

	/**
	 * Saving label map to an output stream.
	 * 
	 * @param fout
	 *            the output stream for saving the label map
	 * @throws IOException
	 *             IO exception
	 */
	protected void writeLbMaps(PrintWriter fout) throws IOException {
		if (lbStr2Int == null) {
			return;
		}

		// write the map size
		fout.println(Integer.toString(lbStr2Int.size()));

		for (Iterator<String> it = lbStr2Int.keySet().iterator(); it.hasNext();) {
			String lbStr = (String) it.next();
			Integer lbInt = (Integer) lbStr2Int.get(lbStr);

			fout.println(lbStr + " " + lbInt.toString());
		}

		// write the line ###...
		fout.println(Option.modelSeparator);
	}

	/**
	 * Reading the training data from a file.
	 * 
	 * @param dataFile
	 *            the data file containing the training data
	 */
	protected void readTrnData(String dataFile) {
		if (cpStr2Int != null) {
			cpStr2Int.clear();
		} else {
			cpStr2Int = new HashMap<String, Integer>();
		}

		if (cpInt2Str != null) {
			cpInt2Str.clear();
		} else {
			cpInt2Str = new HashMap<Integer, String>();
		}

		if (lbStr2Int != null) {
			lbStr2Int.clear();
		} else {
			lbStr2Int = new HashMap<String, Integer>();
		}

		if (lbInt2Str != null) {
			lbInt2Str.clear();
		} else {
			lbInt2Str = new HashMap<Integer, String>();
		}

		if (trnData != null) {
			trnData.clear();
		} else {
			trnData = new ArrayList<Observation>();
		}

		// open data file
		BufferedReader bufferedReader;

		try {
			bufferedReader = FileLoader.getBufferredReader(dataFile, "UTF8");
			if (bufferedReader == null) {
				return;
			}

			System.out.println("Reading training data ...");

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				StringTokenizer strTok = new StringTokenizer(line, " \t\r\n");
				int len = strTok.countTokens();

				if (len <= 1) {
					// skip this invalid line
					continue;
				}

				List<String> strCps = new ArrayList<String>();
				for (int i = 0; i < len - 1; i++) {
					strCps.add(strTok.nextToken());
				}

				String labelStr = strTok.nextToken();

				List<Integer> intCps = new ArrayList<Integer>();

				for (int i = 0; i < strCps.size(); i++) {
					String cpStr = (String) strCps.get(i);
					Integer cpInt = (Integer) cpStr2Int.get(cpStr);
					if (cpInt != null) {
						intCps.add(cpInt);
					} else {
						intCps.add(new Integer(cpStr2Int.size()));
						cpStr2Int.put(cpStr, new Integer(cpStr2Int.size()));
						cpInt2Str.put(new Integer(cpInt2Str.size()), cpStr);
					}
				}

				Integer labelInt = (Integer) lbStr2Int.get(labelStr);
				if (labelInt == null) {
					labelInt = new Integer(lbStr2Int.size());
					lbStr2Int.put(labelStr, labelInt);
					lbInt2Str.put(labelInt, labelStr);
				}

				int[] cps = new int[intCps.size()];
				for (int i = 0; i < cps.length; i++) {
					cps[i] = ((Integer) intCps.get(i)).intValue();
				}

				Observation obsr = new Observation(labelInt.intValue(), cps);

				// add this observation to the data
				trnData.add(obsr);
			}

			bufferedReader.close();

			System.out.println("Reading " + Integer.toString(trnData.size()) + " training data examples completed!");

		} catch (IOException e) {
			System.out.println(e.toString());
			return;
		}

		option.numCps = cpStr2Int.size();
		option.numLabels = lbStr2Int.size();
		option.numTrainExps = trnData.size();
	}

	/**
	 * Reading test data from a file.
	 * 
	 * @param dataFile
	 *            the file containing test data
	 */
	protected void readTstData(String dataFile) {
		if (tstData != null) {
			tstData.clear();
		} else {
			tstData = new ArrayList<Observation>();
		}

		// open data file
		BufferedReader bufferedReader;

		try {
			bufferedReader = FileLoader.getBufferredReader(dataFile, "UTF8");
			if (bufferedReader == null) {
				return;
			}

			System.out.println("Reading testing data ...");

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				StringTokenizer strTok = new StringTokenizer(line, " \t\r\n");
				int len = strTok.countTokens();

				if (len <= 1) {
					// skip this invalid line
					continue;
				}

				List<String> strCps = new ArrayList<String>();
				for (int i = 0; i < len - 1; i++) {
					strCps.add(strTok.nextToken());
				}

				String labelStr = strTok.nextToken();

				List<Integer> intCps = new ArrayList<Integer>();

				for (int i = 0; i < strCps.size(); i++) {
					String cpStr = (String) strCps.get(i);
					Integer cpInt = (Integer) cpStr2Int.get(cpStr);
					if (cpInt != null) {
						intCps.add(cpInt);
					} else {
						// do nothing
					}
				}

				Integer labelInt = (Integer) lbStr2Int.get(labelStr);
				if (labelInt == null) {
					System.out.println("Reading testing observation, label not found or invalid");
					System.out.println("Label: " + labelStr);

					bufferedReader.close();

					return;
				}

				int[] cps = new int[intCps.size()];
				for (int i = 0; i < cps.length; i++) {
					cps[i] = ((Integer) intCps.get(i)).intValue();
				}

				Observation obsr = new Observation(labelInt.intValue(), cps);

				// add this observation to the data
				tstData.add(obsr);
			}

			bufferedReader.close();

			System.out.println("Reading " + Integer.toString(tstData.size()) + " testing data examples completed!");

		} catch (IOException e) {
			System.out.println(e.toString());
			return;
		}

		option.numTestExps = tstData.size();
	}
}
