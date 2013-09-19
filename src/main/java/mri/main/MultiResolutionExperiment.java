package mri.main;

import game.core.Dataset;
import game.core.ResultList;
import game.core.blocks.Classifier;
import game.core.experiments.ClassificationResult;
import game.plugins.blocks.filters.CentroidClusterer;
import game.plugins.blocks.filters.LinearTransform;
import game.plugins.datasetbuilders.CSVDatasetLoader;
import game.plugins.experiments.KFoldCrossValidation;
import game.plugins.metrics.StandardClassificationMetrics;
import game.plugins.trainingalgorithms.KMeansAlgorithm;
import game.plugins.trainingalgorithms.MahalanobisTransform;
import game.plugins.trainingalgorithms.PrincipalComponents;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.util.*;

public class MultiResolutionExperiment {

    private boolean doMahalanobis;
    private int components;
    private int clusters;
    private int folds;
    private MultiResolutionTransform transform;

    public MultiResolutionExperiment(boolean doMahalanobis, int components, int clusters, int folds, MultiResolutionTransform transform) {
        this.doMahalanobis = doMahalanobis;
        this.components = components;
        this.clusters = clusters;
        this.folds = folds;
        this.transform = transform;
    }

    public static class MultiResolutionResult extends ClassificationResult {
        public Dataset transformedDataset;
    }

    public ResultList<MultiResolutionResult> run(String datasetName, Classifier classifier, int foldSize) {
        CSVDatasetLoader loader = new CSVDatasetLoader();
        loader.setContent("file", new File(datasetName));
        loader.setContent("instanceNumber", folds*foldSize);
        loader.setContent("hasHeader", true);
        loader.prepare();

        KFoldCrossValidation kfold = new KFoldCrossValidation();
        kfold.setContent("folds", folds);
        kfold.setContent("shuffle", false);
        kfold.setContent("datasetBuilder", loader);
        kfold.setContent("classifier", classifier);
        ResultList<ClassificationResult> results = kfold.execute(null);

        Dataset classified = getClassifiedDataset(results);

        Dataset dataset = loader.buildDataset();

        if (dataset.size() != (folds*foldSize)) {
            throw new RuntimeException("Dataset size is invalid: " + dataset.size() + " (should be " + (folds*foldSize) + ")");
        }
        /*
        LinearTransform pca = new LinearTransform();
        pca.setContent("datasetTemplate", dataset.getTemplate());
        pca.setContent("trainingAlgorithm", new PrincipalComponents());
        pca.setContent("trainingAlgorithm.components", components);
        pca.trainingAlgorithm.execute(dataset);
        dataset = dataset.apply(pca);
        */

        if (doMahalanobis) {
            LinearTransform mahalanobis = new LinearTransform();
            mahalanobis.setContent("datasetTemplate", dataset.getTemplate());
            mahalanobis.setContent("trainingAlgorithm", new MahalanobisTransform());
            mahalanobis.trainingAlgorithm.execute(dataset);

            dataset = dataset.apply(mahalanobis);
        }

        Dataset transformed = transform.apply(dataset);

        CentroidClusterer clusterer = new CentroidClusterer();
        clusterer.setContent("clusterNumber", clusters);
        clusterer.setContent("trainingAlgorithm", new KMeansAlgorithm());
        clusterer.setContent("datasetTemplate", transformed.getTemplate());
        clusterer.trainingAlgorithm.execute(transformed);

        for (RealVector centroid: clusterer.centroids) {
            System.out.print("\\addplot coordinates { ");
            for (int i = 0; i < centroid.getDimension(); i++) {
                System.out.print("(" + (i+1) + ", " + centroid.getEntry(i) + ") ");
            }
            System.out.println(" };");
        }

        Dataset clusterLabels = transformed.apply(clusterer);

        List<Dataset> clusters = splitIntoClusters(classified, clusterLabels);
        List<Dataset> transformedClusters = splitIntoClusters(transformed, clusterLabels);

        ResultList<MultiResolutionResult> perClusterResults = convertToResults(clusters, transformedClusters, results);

        return perClusterResults;
    }

    private ResultList<MultiResolutionResult> convertToResults(List<Dataset> clusters, List<Dataset> transformedClusters, ResultList<ClassificationResult> results) {
        Classifier classifier = results.results.get(0).trainedClassifier;
        ResultList<MultiResolutionResult> ret = new ResultList<>();
        for(int i = 0; i < clusters.size(); i++) {
            MultiResolutionResult result = new MultiResolutionResult();
            result.classifiedDataset = clusters.get(i);
            result.transformedDataset = transformedClusters.get(i);
            result.trainedClassifier = classifier;
            ret.results.add(result);
        }
        return ret;
    }

    private List<Dataset> splitIntoClusters(Dataset dataset, Dataset clusterLabels) {
        List<Dataset> ret = new ArrayList<>(clusters);
        while(ret.size() < clusters)
            ret.add(new Dataset(dataset.getTemplate()));
        for(int i = 0; i < dataset.size(); i++) {
            int cluster = Integer.valueOf(clusterLabels.get(i).getSource().get().get(String.class));
            ret.get(cluster).add(dataset.get(i));
        }
        return ret;
    }

    private Dataset getClassifiedDataset(ResultList<ClassificationResult> results) {
        Dataset ret = new Dataset(results.results.get(0).classifiedDataset.getTemplate());
        for(ClassificationResult result: results.results) {
            ret.addAll(result.classifiedDataset);
        }
        return ret;
    }

}
