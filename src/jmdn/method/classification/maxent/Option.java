/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.*;
import java.util.StringTokenizer;

import jmdn.base.util.filesystem.DirFileUtil;
import jmdn.base.util.filesystem.FileLoader;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Option {
	// model directory
	protected String modelDirectory = ".";
	// model file
	protected String modelFile = "model.txt";
	protected static String modelSeparator = "##########";
	// option file
	protected String optionFile = "option.txt";

	// training data, testing data file
	protected String trainDataFile = "train.labeled";
	protected String testDataFile = "test.labeled";
	protected static String labelSeparator = "/";

	// training log
	protected String trainLogFile = "trainlog.txt";
	protected boolean isLogging = true;

	protected int numTrainExps = 0; // number of training examples
	protected int numTestExps = 0; // number of testing examples
	protected int numLabels = 0; // number of class labels
	protected int numCps = 0; // number of context predicates
	protected int numFeatures = 0; // number of features

	// thresholds for context predicate and feature cut-off
	protected int cpRareThreshold = 1;
	protected int fRareThreshold = 1;

	// training options
	protected int numIterations = 100; // number of training iterations
	protected double initLambdaVal = 0.0; // intial value for feature weights
	protected double sigmaSquare = 100; // for smoothing
	protected double epsForConvergence = 0.0001; // for checking training
													// termination
	protected int mForHessian = 7; // for L-BFGS corrections
	protected int debugLevel = 1; // control output status information

	// evaluation options
	protected boolean evaluateDuringTraining = true; // evaluate during training
	protected boolean saveBestModel = true; // save the best model with testing
											// data

	/**
	 * Default class constructor.
	 */
	public Option() {
		// do nothing
	}

	/**
	 * Class constructor.
	 * 
	 * @param modelDirectory
	 *            the model directory
	 */
	public Option(String modelDirectory) {
		this.modelDirectory = DirFileUtil.normalizeDirectory(modelDirectory);
	}

	/**
	 * Reading options from the option file.
	 * 
	 * @return True if reading successfully, false otherwise
	 */
	protected boolean readOptions() {
		try {
			BufferedReader fin = FileLoader.getBufferredReader(modelDirectory, optionFile, "UTF8");

			System.out.println("Reading options ...");

			// read option lines
			String line;
			while ((line = fin.readLine()) != null) {
				String trimLine = line.trim();
				if (trimLine.startsWith("#")) {
					// comment line
					continue;
				}

				StringTokenizer strTok = new StringTokenizer(line, "= \t\r\n");
				int len = strTok.countTokens();
				if (len != 2) {
					// invalid parameter line, ignore it
					continue;
				}

				String strOpt = strTok.nextToken();
				String strVal = strTok.nextToken();

				if (strOpt.compareToIgnoreCase("trainDataFile") == 0) {
					trainDataFile = strVal;

				} else if (strOpt.compareToIgnoreCase("testDataFile") == 0) {
					testDataFile = strVal;

				} else if (strOpt.compareToIgnoreCase("isLogging") == 0) {
					if (!(strVal.compareToIgnoreCase("true") == 0 || strVal.compareToIgnoreCase("false") == 0)) {
						continue;
					}
					isLogging = Boolean.valueOf(strVal).booleanValue();

				} else if (strOpt.compareToIgnoreCase("cpRareThreshold") == 0) {
					int numTemp = Integer.parseInt(strVal);
					cpRareThreshold = numTemp;

				} else if (strOpt.compareToIgnoreCase("fRareThreshold") == 0) {
					int numTemp = Integer.parseInt(strVal);
					fRareThreshold = numTemp;

				} else if (strOpt.compareToIgnoreCase("numIterations") == 0) {
					int numTemp = Integer.parseInt(strVal);
					numIterations = numTemp;

				} else if (strOpt.compareToIgnoreCase("initLambdaVal") == 0) {
					double numTemp = Double.parseDouble(strVal);
					initLambdaVal = numTemp;

				} else if (strOpt.compareToIgnoreCase("sigmaSquare") == 0) {
					double numTemp = Double.parseDouble(strVal);
					sigmaSquare = numTemp;

				} else if (strOpt.compareToIgnoreCase("epsForConvergence") == 0) {
					double numTemp = Double.parseDouble(strVal);
					epsForConvergence = numTemp;

				} else if (strOpt.compareToIgnoreCase("mForHessian") == 0) {
					int numTemp = Integer.parseInt(strVal);
					mForHessian = numTemp;

				} else if (strOpt.compareToIgnoreCase("evaluateDuringTraining") == 0) {
					if (!(strVal.compareToIgnoreCase("true") == 0 || strVal.compareToIgnoreCase("false") == 0)) {
						continue;
					}
					evaluateDuringTraining = Boolean.valueOf(strVal).booleanValue();

				} else if (strOpt.compareToIgnoreCase("saveBestModel") == 0) {
					if (!(strVal.compareToIgnoreCase("true") == 0 || strVal.compareToIgnoreCase("false") == 0)) {
						continue;
					}
					saveBestModel = Boolean.valueOf(strVal).booleanValue();

				} else {
					// for future use
				}

			}

			fin.close();

			System.out.println("Reading options completed!");

		} catch (IOException e) {
			System.out.println(e.toString());
			return false;
		}

		return true;
	}

	/**
	 * Opening the training log file for saving.
	 * 
	 * @return The output stream of the training log file for saving
	 */
	protected PrintWriter openTrainLogFile() {
		String filename = modelDirectory + File.separator + trainLogFile;
		PrintWriter fout;

		try {
			fout = new PrintWriter(new FileWriter(filename));

		} catch (IOException e) {
			System.out.println(e.toString());
			return null;
		}

		return fout;
	}

	/**
	 * Getting the model directory.
	 * 
	 * @return The model directory
	 */
	public String getModelDirectory() {
		return modelDirectory;
	}

	/**
	 * Opening the model file for reading.
	 * 
	 * @return The input stream of the model file for reading
	 */
	protected BufferedReader openModelFile() {
		return FileLoader.getBufferredReader(modelDirectory, modelFile, "UTF8");
	}

	/**
	 * Creating the model file for storing the trained model.
	 * 
	 * @return The output stream of the model file for saving
	 */
	protected PrintWriter createModelFile() {
		String filename = modelDirectory + File.separator + modelFile;
		PrintWriter fout;

		try {
			fout = new PrintWriter(new FileWriter(filename));

		} catch (IOException e) {
			System.out.println(e.toString());
			return null;
		}

		return fout;
	}

	/**
	 * Saving the options to the option file.
	 * 
	 * @param fout
	 *            the output stream for saving options
	 */
	protected void writeOptions(PrintWriter fout) {
		fout.println("OPTION VALUES:");
		fout.println("==============");
		fout.println("Model directory: " + modelDirectory);
		fout.println("Model file: " + modelFile);
		fout.println("Option file: " + optionFile);
		fout.println("Training log file: " + trainLogFile + " (this one)");
		fout.println("Training data file: " + trainDataFile);
		fout.println("Testing data file: " + testDataFile);
		fout.println("Number of training examples " + Integer.toString(numTrainExps));
		fout.println("Number of testing examples " + Integer.toString(numTestExps));
		fout.println("Number of class labels: " + Integer.toString(numLabels));
		fout.println("Number of context predicates: " + Integer.toString(numCps));
		fout.println("Number of features: " + Integer.toString(numFeatures));
		fout.println("Rare threshold for context predicates: " + Integer.toString(cpRareThreshold));
		fout.println("Rare threshold for features: " + Integer.toString(fRareThreshold));
		fout.println("Number of training iterations: " + Integer.toString(numIterations));
		fout.println("Initial value of feature weights: " + Double.toString(initLambdaVal));
		fout.println("Sigma square: " + Double.toString(sigmaSquare));
		fout.println("Epsilon for convergence: " + Double.toString(epsForConvergence));
		fout.println("Number of corrections in L-BFGS: " + Integer.toString(mForHessian));
		if (evaluateDuringTraining) {
			fout.println("Evaluation during training: true");
		} else {
			fout.println("Evaluation during training: false");
		}
		if (saveBestModel) {
			fout.println("Save the best model towards testing data: true");
		} else {
			fout.println("Save the best model towards testing data: false");
		}
		fout.println();
	}
}
