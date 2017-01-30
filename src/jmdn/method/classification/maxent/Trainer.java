/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Trainer {
	/**
	 * The main method for training and testing.
	 * 
	 * @param args
	 *            the argument list (see help)
	 * @throws IOException
	 *             IO exception
	 */
	public static void main(String[] args) throws IOException {
		if (!checkArgs(args)) {
			displayHelp();
			return;
		}

		String modelDir = args[2];
		boolean isAll = (args[0].compareToIgnoreCase("-all") == 0);
		boolean isTrn = (args[0].compareToIgnoreCase("-trn") == 0);
		boolean isTst = (args[0].compareToIgnoreCase("-tst") == 0);

		// create option object
		Option option = new Option(modelDir);
		option.readOptions();

		Data data;
		Dictionary dictionary;
		FeatureGenerator featureGenerator;
		Train train;
		Inference inference;
		Evaluation evaluation;
		Model model;

		PrintWriter foutModel;
		BufferedReader finModel;

		if (isAll) {
			// both training and testing

			PrintWriter flog = option.openTrainLogFile();
			if (flog == null) {
				System.out.println("Couldn't create training log file!");
				return;
			}

			foutModel = option.createModelFile();
			if (foutModel == null) {
				System.out.println("Couldn't create model file!");
				return;
			}

			data = new Data(option);
			data.readTrnData(option.modelDirectory + File.separator + option.trainDataFile);
			data.readTstData(option.modelDirectory + File.separator + option.testDataFile);

			dictionary = new Dictionary(option, data);
			dictionary.generateDictionary();

			featureGenerator = new FeatureGenerator(option, data, dictionary);
			featureGenerator.generateFeatures();

			data.writeCpMaps(dictionary, foutModel);
			data.writeLbMaps(foutModel);

			train = new Train();
			inference = new Inference();
			evaluation = new Evaluation();

			model = new Model(option, data, dictionary, featureGenerator, train, inference, evaluation);
			model.doTrain(flog);

			model.doInference(model.data.tstData);
			model.evaluation.evaluate(flog);

			dictionary.writeDictionary(foutModel);
			featureGenerator.writeFeatures(foutModel);

			foutModel.close();
		}

		if (isTrn) {
			// training only

			PrintWriter flog = option.openTrainLogFile();
			if (flog == null) {
				System.out.println("Couldn't create training log file");
				return;
			}

			foutModel = option.createModelFile();
			if (foutModel == null) {
				System.out.println("Couldn't create model file");
				return;
			}

			data = new Data(option);
			data.readTrnData(option.modelDirectory + File.separator + option.trainDataFile);

			dictionary = new Dictionary(option, data);
			dictionary.generateDictionary();

			featureGenerator = new FeatureGenerator(option, data, dictionary);
			featureGenerator.generateFeatures();

			data.writeCpMaps(dictionary, foutModel);
			data.writeLbMaps(foutModel);

			train = new Train();

			model = new Model(option, data, dictionary, featureGenerator, train, null, null);
			model.doTrain(flog);

			dictionary.writeDictionary(foutModel);
			featureGenerator.writeFeatures(foutModel);

			foutModel.close();
		}

		if (isTst) {
			// testing only

			finModel = option.openModelFile();
			if (finModel == null) {
				System.out.println("Couldn't open model file");
				return;
			}

			data = new Data(option);
			data.readCpMaps(finModel);
			data.readLbMaps(finModel);
			data.readTstData(option.modelDirectory + File.separator + option.testDataFile);

			dictionary = new Dictionary(option, data);
			dictionary.readDictionary(finModel);

			featureGenerator = new FeatureGenerator(option, data, dictionary);
			featureGenerator.readFeatures(finModel);

			inference = new Inference();
			evaluation = new Evaluation();

			model = new Model(option, data, dictionary, featureGenerator, null, inference, evaluation);

			model.doInference(model.data.tstData);
			model.evaluation.evaluate(null);

			finModel.close();
		}

	}

	/**
	 * Checking valid arguments.
	 * 
	 * @param args
	 *            the list of input arguments
	 * @return True if the arguments are valid, false otherwise
	 */
	private static boolean checkArgs(String[] args) {
		if (args.length < 3) {
			return false;
		}

		if (!(args[0].compareToIgnoreCase("-all") == 0 || args[0].compareToIgnoreCase("-trn") == 0 || 
				args[0].compareToIgnoreCase("-tst") == 0)) {
			return false;
		}

		if (args[1].compareToIgnoreCase("-d") != 0) {
			return false;
		}

		return true;
	}

	/**
	 * Displaying help information.
	 */
	private static void displayHelp() {
		System.out.println("Usage:");
		System.out.println("\tTrainer -all/-trn/-tst -d <model directory>");
	}
}
