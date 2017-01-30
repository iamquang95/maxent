/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.PrintWriter;
import jmdn.base.math.optimization.LBFGS;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Train {
	// the model object
	protected Model model = null;

	private int numLabels = 0;
	private int numFeatures = 0;

	private double[] lambda = null;
	private double[] tempLambda = null;

	// for L-BFGS
	private double[] gradLogLi = null;
	private double[] diag = null;
	private double[] temp = null;
	// private double[] ws = null;
	private int[] iprint = null;
	private int[] iflag = null;

	/**
	 * Default class constructor.
	 */
	public Train() {
		// do nothing
	}

	/**
	 * Initializing for the training process.
	 */
	public void init() {
		numLabels = model.data.numLabels();
		numFeatures = model.featureGenerator.numFeatures();
		if (numLabels <= 0 || numFeatures <= 0) {
			System.out.println("Invalid number of labels or features");
			return;
		}

		lambda = model.lambda;
		tempLambda = new double[numFeatures];

		gradLogLi = new double[numFeatures];
		diag = new double[numFeatures];

		temp = new double[numLabels];

		// int wsSize = numFeatures * (2 * model.option.mForHessian + 1) + 2 * model.option.mForHessian;
		// ws = new double[wsSize];

		iprint = new int[2];
		iflag = new int[1];

	}

	/**
	 * Normalizing a given vector.
	 * 
	 * @param vect
	 *            the vector to be normalized
	 * @return The normalized vector
	 */
	protected static double norm(double[] vect) {
		double res = 0.0;
		for (int i = 0; i < vect.length; i++) {
			res += vect[i] * vect[i];
		}
		return Math.sqrt(res);
	}

	/**
	 * Training the model.
	 * 
	 * @param fout
	 *            the output stream for logging
	 */
	public void doTrain(PrintWriter fout) {
		long start_train, end_train, elapsed_train;
		long start_iter, end_iter, elapsed_iter;

		// initialization
		init();

		double f;
		double xtol = 1.0e-16;
		int numIter = 0;

		// for L-BFGS
		iprint[0] = model.option.debugLevel - 2;
		iprint[1] = model.option.debugLevel - 1;

		iflag[0] = 0;

		// counter
		int i;

		// get initial values for lambda
		for (i = 0; i < numFeatures; i++) {
			lambda[i] = model.option.initLambdaVal;
		}

		System.out.println("Start to train ...");
		if (model.option.isLogging) {
			model.option.writeOptions(fout);
			fout.println("Start to train ...");
		}

		// starting time of the training process
		start_train = System.currentTimeMillis();

		double maxAccuracy = 0.0;
		int maxAccuracyIter = -1;

		// the training loop
		do {

			// starting time of iteration
			start_iter = System.currentTimeMillis();

			// call this to compute two things:
			// 1. log-likelihood value
			// 2. the gradient vector of log-likelihood function
			f = computeLogLiGradient(lambda, gradLogLi, numIter + 1, fout);

			// negate f and its gradient because L-BFGS minimizes the objective
			// function
			// while we would like to maximize it
			f *= -1;
			for (i = 0; i < numFeatures; i++) {
				gradLogLi[i] *= -1;
			}

			// calling L-BFGS
			try {
				LBFGS.lbfgs(numFeatures, model.option.mForHessian, lambda, f, gradLogLi, false, diag, iprint,
						model.option.epsForConvergence, xtol, iflag);
			} catch (LBFGS.ExceptionWithIflag e) {
				System.out.println("L-BFGS failed!");
				if (model.option.isLogging) {
					fout.println("L-BFGS failed!");
				}

				break;
			}

			numIter++;

			// get the end time of the current iteration
			end_iter = System.currentTimeMillis();
			elapsed_iter = end_iter - start_iter;
			System.out.println("\tIteration elapsed: " + Double.toString((double) elapsed_iter / 1000) + " seconds");
			if (model.option.isLogging) {
				fout.println("\tIteration elapsed: " + Double.toString((double) elapsed_iter / 1000) + " seconds");
			}

			// evaluate during training
			if (model.option.evaluateDuringTraining) {
				// inference on testing data
				model.doInference(model.data.tstData);

				// evaluation
				double accuracy = model.evaluation.evaluate(fout);
				if (accuracy > maxAccuracy) {
					maxAccuracy = accuracy;
					maxAccuracyIter = numIter;

					// save the best model towards testing evaluation
					if (model.option.saveBestModel) {
						for (i = 0; i < numFeatures; i++) {
							tempLambda[i] = lambda[i];
						}
					}
				}

				System.out.println("\tCurrent max accuracy: " + Double.toString(maxAccuracy) + " (at iteration "
						+ Integer.toString(maxAccuracyIter) + ")");
				if (model.option.isLogging) {
					fout.println("\tCurrent max accuracy: " + Double.toString(maxAccuracy) + " (at iteration "
							+ Integer.toString(maxAccuracyIter) + ")");
				}

				// get the end time of the current iteration
				end_iter = System.currentTimeMillis();
				elapsed_iter = end_iter - start_iter;
				System.out.println("\tIteration elapsed (including testing & evaluation): "
						+ Double.toString((double) elapsed_iter / 1000) + " seconds");
				if (model.option.isLogging) {
					fout.println("\tIteration elapsed (including testing & evaluation): "
							+ Double.toString((double) elapsed_iter / 1000) + " seconds");

					fout.flush();
				}
			}

		} while (iflag[0] != 0 && numIter < model.option.numIterations);

		// get the end time of the training process
		end_train = System.currentTimeMillis();
		elapsed_train = end_train - start_train;
		System.out.println("\tThe training process elapsed: " + Double.toString((double) elapsed_train / 1000)
				+ " seconds");
		if (model.option.isLogging) {
			fout.println("\tThe training process elapsed: " + Double.toString((double) elapsed_train / 1000)
					+ " seconds");
		}

		if (model.option.evaluateDuringTraining && model.option.saveBestModel) {
			for (i = 0; i < numFeatures; i++) {
				lambda[i] = tempLambda[i];
			}
		}
	}

	/**
	 * Computing the log-likelihood function.
	 * 
	 * @param lambda
	 *            the lambda vector (i.e., the vector of weights of the
	 *            features)
	 * @param gradLogLi
	 *            the vector gradident
	 * @param numIteration
	 *            the current iteration of the training process
	 * @param fout
	 *            the output stream for logging
	 * @return The value of the log-likelihood function
	 */
	private double computeLogLiGradient(double[] lambda, double[] gradLogLi, int numIteration, PrintWriter fout) {
		double logLi = 0.0;

		int ii, i;

		for (i = 0; i < numFeatures; i++) {
			gradLogLi[i] = -1 * lambda[i] / model.option.sigmaSquare;
			logLi -= (lambda[i] * lambda[i]) / (2 * model.option.sigmaSquare);
		}

		// go through all training data examples/observations
		for (ii = 0; ii < model.data.trnData.size(); ii++) {
			Observation obsr = (Observation) model.data.trnData.get(ii);

			for (i = 0; i < numLabels; i++) {
				temp[i] = 0.0;
			}

			// log-likelihood value of the current data observation
			double obsrLogLi = 0.0;

			// start to scan all features at the current obsr
			model.featureGenerator.startScanFeatures(obsr);

			while (model.featureGenerator.hasNextFeature()) {
				Feature feature = model.featureGenerator.nextFeature();

				if (feature.label == obsr.humanLabel) {
					gradLogLi[feature.index] += feature.value;
					obsrLogLi += lambda[feature.index] * feature.value;
				}

				temp[feature.label] += lambda[feature.index] * feature.value;
			}

			double Zx = 0.0;
			for (i = 0; i < numLabels; i++) {
				Zx += Math.exp(temp[i]);
			}

			model.featureGenerator.scanReset();
			while (model.featureGenerator.hasNextFeature()) {
				Feature feature = model.featureGenerator.nextFeature();

				gradLogLi[feature.index] -= feature.value * Math.exp(temp[feature.label]) / Zx;
			}

			obsrLogLi -= Math.log(Zx);
			logLi += obsrLogLi;
		}

		System.out.println();
		System.out.println("Iteration: " + Integer.toString(numIteration));
		System.out.println("\tLog-likelihood                 = " + Double.toString(logLi));
		double gradLogLiNorm = Train.norm(gradLogLi);
		System.out.println("\tNorm (log-likelihood gradient) = " + Double.toString(gradLogLiNorm));
		double lambdaNorm = Train.norm(lambda);
		System.out.println("\tNorm (lambda)                  = " + Double.toString(lambdaNorm));

		if (model.option.isLogging) {
			fout.println();
			fout.println("Iteration: " + Integer.toString(numIteration));
			fout.println("\tLog-likelihood                 = " + Double.toString(logLi));
			fout.println("\tNorm (log-likelihood gradient) = " + Double.toString(gradLogLiNorm));
			fout.println("\tNorm (lambda)                  = " + Double.toString(lambdaNorm));
		}

		return logLi;
	}
}
