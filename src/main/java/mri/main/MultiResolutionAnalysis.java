package mri.main;

import game.core.Metrics;
import game.core.ResultList;
import game.plugins.metrics.StandardClassificationMetrics;
import game.plugins.weka.algorithms.WekaJ48;
import game.plugins.weka.classifiers.WekaClassifier;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.util.Arrays;

public class MultiResolutionAnalysis {

    public void run(ResultList<MultiResolutionExperiment.MultiResolutionResult> results) {
        StandardClassificationMetrics standard = new StandardClassificationMetrics();
        standard.prepare(results);

        MultiResolutionIndex mri = new MultiResolutionIndex();
        mri.prepare(results);

        int[] sizes = new int[results.results.size()];
        for(int i = 0; i< results.results.size(); i++) {
            sizes[i] = results.results.get(i).transformedDataset.size();
        }

        for(int i = 0; i < standard.statistics.getMatrix().length; i++) {
            printComparison(i, standard, mri, sizes);
        }
    }

    private void printComparison(int row, StandardClassificationMetrics standard, MultiResolutionIndex mri, int[] sizes) {
        double[] standards = getMetric(row, standard);
        double[] mris = getMetric(0, mri);
        System.out.println(standard.statistics.getRowLabels().get(row));
        System.out.println(Arrays.toString(sizes));
        System.out.println(Arrays.toString(standards));
        System.out.println(Arrays.toString(mris));
        System.out.println(new SpearmansCorrelation().correlation(standards, mris));
    }

    private double[] getMetric(int row, Metrics metrics) {
        double[] ret = new double[metrics.data.size()];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = ((RealMatrix)metrics.data.get(i)).getEntry(row, 0);
        }
        return ret;
    }

    public static void main(String... args) {
        MultiResolutionTransform transform = new MultiResolutionTransform(50, 0.15, 1);

        WekaClassifier classifier = new WekaClassifier();
        classifier.setContent("trainingAlgorithm", new WekaJ48());

        MultiResolutionExperiment experiment = new MultiResolutionExperiment(true, 5, 3, 10, transform);

        String[] datasets = {
                "appendicitis", "banana", "breast-cancer-original", "mammographic-masses", "phoneme",
                "pima", "ring", "spambase", "twonorm", "wdbc" };
        int[] sizes = {10, 530, 65, 96, 540, 70, 740, 455, 740, 56};

        for(int i = 0; i < datasets.length; i++) {
            String dataset = datasets[i];
            int size = sizes[i];
            System.out.println("Analysis for " + dataset + " (" + (size*10) + ")");

            String filename = "data/keel/pc-" + dataset + ".dat.csv";

            ResultList<MultiResolutionExperiment.MultiResolutionResult> results = experiment.run(filename, classifier, size);

            MultiResolutionAnalysis analysis = new MultiResolutionAnalysis();
            analysis.run(results);
        }
    }

}
