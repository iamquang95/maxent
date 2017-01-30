/**
 * 
 */

package jmdn.method.classification.maxent;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Xuan-Hieu Phan (pxhieu@gmail.com)
 * @version 1.1
 * @since 25-08-2014
 */
public class Evaluation {

	protected Model model = null;
	protected int numLabels = 0;

	protected int[] humanLabelCounts = null;
	protected int[] modelLabelCounts = null;
	protected int[] humanModelCounts = null;

	/**
	 * Default class constructor.
	 */
	public Evaluation() {
		// do nothing
	}

	/**
	 * Initializing for doing evaluation.
	 */
	public void init() {
		numLabels = model.data.numLabels();

		humanLabelCounts = new int[numLabels];
		modelLabelCounts = new int[numLabels];
		humanModelCounts = new int[numLabels];
	}

	/**
	 * Doing evaluation.
	 * 
	 * @return The classification accuracy
	 */
	protected double evaluate() {
		if (model.data.tstData.size() <= 0) {
			return 0.0;
		}

		for (int i = 0; i < humanLabelCounts.length; i++) {
			humanLabelCounts[i] = 0;
			modelLabelCounts[i] = 0;
			humanModelCounts[i] = 0;
		}

		int matchingCount = 0;

		for (int i = 0; i < model.data.tstData.size(); i++) {
			Observation obsr = (Observation) model.data.tstData.get(i);
			if (obsr.humanLabel == obsr.modelLabel) {
				matchingCount++;
			}
		}

		return (double) (matchingCount * 100) / model.data.tstData.size();
	}

	/**
	 * Doing evaluation.
	 * 
	 * @param fout
	 *            an output stream for logging
	 * @return F1 score
	 */
	public double evaluate(PrintWriter fout) {
		int i;

		if (model.data.tstData.size() <= 0) {
			return 0.0;
		}

		for (i = 0; i < humanLabelCounts.length; i++) {
			humanLabelCounts[i] = 0;
			modelLabelCounts[i] = 0;
			humanModelCounts[i] = 0;
		}

		for (i = 0; i < model.data.tstData.size(); i++) {
			Observation obsr = (Observation) model.data.tstData.get(i);

			humanLabelCounts[obsr.humanLabel]++;
			modelLabelCounts[obsr.modelLabel]++;
			if (obsr.humanLabel == obsr.modelLabel) {
				humanModelCounts[obsr.humanLabel]++;
			}
		}

		NumberFormat fm = new DecimalFormat("#.00");

		System.out.println();
		System.out.println("\tPer-class performance evaluation:");
		System.out.println();
		System.out.println("\t\tClass\tHuman\tModel\tMatch\tPre.(%)\tRec.(%)\tF1-score");
		System.out.println("\t\t-----\t-----\t-----\t-----\t-------\t-------\t--------");
		if (fout != null && model.option.isLogging) {
			fout.println();
			fout.println("\tPer-class performance evaluation:");
			fout.println();
			fout.println("\t\tClass\tHuman\tModel\tMatch\tPre.(%)\tRec.(%)\tF1-score");
			fout.println("\t\t-----\t-----\t-----\t-----\t-------\t-------\t--------");
		}

		int count = 0;
		double precision, recall, f1, total1Pre = 0.0, total1Rec = 0.0, total1F1, total2Pre, total2Rec, total2F1;
		int totalHuman = 0, totalModel = 0, totalMatch = 0;

		for (i = 0; i < numLabels; i++) {
			if (modelLabelCounts[i] > 0) {
				precision = (double) humanModelCounts[i] / modelLabelCounts[i];
				totalModel += modelLabelCounts[i];
				total1Pre += precision;
			} else {
				precision = 0.0;
			}

			if (humanLabelCounts[i] > 0) {
				recall = (double) humanModelCounts[i] / humanLabelCounts[i];
				totalHuman += humanLabelCounts[i];
				total1Rec += recall;
				count++;
			} else {
				recall = 0.0;
			}

			totalMatch += humanModelCounts[i];

			if (recall + precision > 0) {
				f1 = (double) 2 * precision * recall / (precision + recall);
			} else {
				f1 = 0.0;
			}

			String classStr = Integer.toString(i);
			String labelStr = (String) model.data.getLbInt2Str().get(new Integer(i));
			if (labelStr != null) {
				classStr = labelStr;
			}

			System.out.println("\t\t" + classStr + "\t" + Integer.toString(humanLabelCounts[i]) + "\t"
					+ Integer.toString(modelLabelCounts[i]) + "\t" + Integer.toString(humanModelCounts[i]) + "\t"
					+ fm.format(precision * 100) + "\t" + fm.format(recall * 100) + "\t" + fm.format(f1 * 100));
			if (fout != null && model.option.isLogging) {
				fout.println("\t\t" + classStr + "\t" + Integer.toString(humanLabelCounts[i]) + "\t"
						+ Integer.toString(modelLabelCounts[i]) + "\t" + Integer.toString(humanModelCounts[i]) + "\t"
						+ fm.format(precision * 100) + "\t" + fm.format(recall * 100) + "\t" + fm.format(f1 * 100));
			}
		}

		total1Pre /= count;
		total1Rec /= count;
		total1F1 = 2 * total1Pre * total1Rec / (total1Pre + total1Rec);

		total2Pre = (double) totalMatch / totalModel;
		total2Rec = (double) totalMatch / totalHuman;
		total2F1 = 2 * total2Pre * total2Rec / (total2Pre + total2Rec);

		System.out.println("\t\t-----\t-----\t-----\t-----\t-------\t-------\t--------");
		System.out.println("\t\tAvg.1\t\t\t\t" + fm.format(total1Pre * 100) + "\t" + fm.format(total1Rec * 100) + "\t"
				+ fm.format(total1F1 * 100));
		System.out.println("\t\tAvg.2\t" + Integer.toString(totalHuman) + "\t" + Integer.toString(totalModel) + "\t"
				+ Integer.toString(totalMatch) + "\t" + fm.format(total2Pre * 100) + "\t" + fm.format(total2Rec * 100)
				+ "\t" + fm.format(total2F1 * 100));
		System.out.println();
		if (fout != null && model.option.isLogging) {
			fout.println();
			fout.println("\t\t-----\t-----\t-----\t-----\t-------\t-------\t--------");
			fout.println("\t\tAvg.1\t\t\t\t" + fm.format(total1Pre * 100) + "\t" + fm.format(total1Rec * 100) + "\t"
					+ fm.format(total1F1 * 100));
			fout.println("\t\tAvg.2\t" + Integer.toString(totalHuman) + "\t" + Integer.toString(totalModel) + "\t"
					+ Integer.toString(totalMatch) + "\t" + fm.format(total2Pre * 100) + "\t"
					+ fm.format(total2Rec * 100) + "\t" + fm.format(total2F1 * 100));
		}

		return total2F1 * 100;
	}
}
