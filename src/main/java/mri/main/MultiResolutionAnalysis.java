package mri.main;

import game.core.Metrics;
import game.core.ResultList;
import game.plugins.metrics.StandardClassificationMetrics;
import game.plugins.weka.algorithms.WekaJ48;
import game.plugins.weka.classifiers.WekaClassifier;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;

public class MultiResolutionAnalysis {

    private Writer writer;

    public MultiResolutionAnalysis(Writer writer) {
        this.writer = writer;
    }

    public void run(ResultList<MultiResolutionExperiment.MultiResolutionResult> results) throws Exception {
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

    private void printComparison(int row, StandardClassificationMetrics standard, MultiResolutionIndex mri, int[] sizes) throws Exception {
        double[] standards = getMetric(row, standard);
        double[] mris = getMetric(0, mri);
        double corr = new SpearmansCorrelation().correlation(standards, mris);
        write(writer, standard.statistics.getRowLabels().get(row) + ": " + corr + "\n");
        write(writer, Arrays.toString(sizes) + "\n");
        write(writer, Arrays.toString(standards) + "\n");
        write(writer, Arrays.toString(mris) + "\n");
    }

    private double[] getMetric(int row, Metrics metrics) {
        double[] ret = new double[metrics.data.size()];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = ((RealMatrix)metrics.data.get(i)).getEntry(row, 0);
        }
        return ret;
    }

    public static void main(String... args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        int[] ms = {/*40, */50};
        double[] sigmamaxs = {/*0.10, */0.15/*, 0.20, 0.25, 0.30*/};
        int[] clusternums = {3/*, 4, 5*/};

        for(int m: ms) {
            for(double sigmamax: sigmamaxs) {
                for(int clusters: clusternums) {
                    String name = String.format("MultiResolutionExperiment(%d, 10, MultiResolutionTransform(%d, %.2f))", clusters, m, sigmamax);
                    String fileName = "results_" + name + ".txt";
                    Writer writer = new FileWriter(fileName, false);

                    MultiResolutionTransform transform = new MultiResolutionTransform(m, sigmamax, 1);

                    WekaClassifier classifier = new WekaClassifier();
                    classifier.setContent("trainingAlgorithm", new WekaJ48());

                    MultiResolutionExperiment experiment = new MultiResolutionExperiment(true, 5, clusters, 10, transform);

                    write(writer, name + "\n");

                    String[] datasets = {
                            "appendicitis", "banana", "breast-cancer-original", "mammographic-masses", "phoneme",
                            "pima", "ring", "spambase", "twonorm", "wdbc" };
                    int[] sizes = {10, 530, 65, 96, 540, 70, 740, 455, 740, 56};

                    for(int i = 0; i < datasets.length; i++) {
                        String dataset = datasets[i];
                        int size = sizes[i];
                        write(writer, "\nAnalysis for " + dataset + " (" + (size * 10) + ")" + "\n");

                        String filename = "data/keel/pc-" + dataset + ".dat.csv";

                        ResultList<MultiResolutionExperiment.MultiResolutionResult> results = experiment.run(filename, classifier, size);

                        MultiResolutionAnalysis analysis = new MultiResolutionAnalysis(writer);
                        analysis.run(results);

                        writer.flush();
                    }
                    writer.close();
                }
            }
        }
    }

    private static void write(Writer writer, String s) throws Exception {
        System.out.print(s);
        writer.append(s);
    }

}
