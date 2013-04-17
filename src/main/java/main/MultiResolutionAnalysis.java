package main;

import game.core.Data;
import game.core.Dataset;
import game.core.Dataset.SampleIterator;
import game.core.DatasetBuilder;
import game.core.DatasetTemplate;
import game.core.Element;
import game.core.ElementTemplate;
import game.core.Instance;
import game.core.ResultList;
import game.core.Sample;
import game.core.blocks.Classifier;
import game.plugins.blocks.classifiers.KNNClassifier;
import game.plugins.datasetbuilders.CSVDatasetLoader;
import game.plugins.experiments.SimpleExperiment;
import game.plugins.metrics.StandardClassificationMetrics;
import game.plugins.trainingalgorithms.SimpleKNNTraining;
import game.plugins.valuetemplates.LabelTemplate;
import game.plugins.valuetemplates.VectorTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;

public class MultiResolutionAnalysis extends Application {
	
	private static final Map<String, Integer> featureMap = new HashMap<>();
	
	static {
		featureMap.put("breast-cancer-diagnostic", 30);
		featureMap.put("breast-cancer-original", 9);
		featureMap.put("ionosphere", 34);
		featureMap.put("mammographic-masses", 5);
		featureMap.put("parkinsons", 22);
		featureMap.put("sonar", 60);
	}
	
	private static final String directory = "data/";
	private static final String datasetName = "sonar";
	private static int featureNumber = featureMap.get(datasetName);
	private static final int profileSize = 10;
	private static final int clusterNumber = 3;
	private static final int maxKMeansIterations = 100;
	private static final double learningPercent = 0.5;
	private static final double sigmaMaxPercent = 0.3;
	private static double sigmaMax;
	
	private static List<Dataset> testingClusters;
	private static Dataset testingDataset, learningDataset;
	
	private static final DatasetTemplate template = new DatasetTemplate(
			new ElementTemplate(new VectorTemplate(featureNumber)),
			new ElementTemplate(new LabelTemplate("1", "-1")));
	
	public static void main(String... args) {
		Locale.setDefault(Locale.ENGLISH);
		
		CSVDatasetLoader loader = new CSVDatasetLoader();
		loader.setContent("datasetTemplate", template);
		loader.setContent("file", new File(directory+datasetName+".data.csv"));
		loader.setContent("startIndex", 1);
		
		List<Sample> complete = applyMahalanobisTransform(loader.buildDataset());
//		Collections.shuffle(complete, new Random(1));
		
		List<Sample> learning = splitKeepingProbability(complete, learningPercent);
		sigmaMax = evaluateSigmaMax(learning, (int) (learning.size() * sigmaMaxPercent));
		System.out.println("sigmaMax = " + sigmaMax);
		
		List<Sample> testing = new ArrayList<>(complete);
		testing.removeAll(learning);
		
		List<Sample> learningProfiles = applyMultiResolutionTransform(learning, learning);
		
		List<Sample> testingProfiles  = applyMultiResolutionTransform(learning, testing);
		
		List<RealVector> centroids = applyKMeans(learningProfiles);
		
		Collections.sort(centroids, new Comparator<RealVector>() {
			@Override public int compare(RealVector o1, RealVector o2) {
				return Double.compare(getMultiResolutionIndex(o1), getMultiResolutionIndex(o2));
			}
		});
		
		testingClusters = revertToDatasets(getClusters(testingProfiles, centroids));
		
		learningDataset = revertToDataset(learningProfiles);
		
		testingDataset = revertToDataset(testingProfiles);
		
		for(int i = 0; i < clusterNumber; i++) {
			double accuracy = getKNNAccuracy(learningDataset, testingClusters.get(i));
			
			System.out.println(testingClusters.get(i).size() + " " + centroids.get(i) + " "
							   + getMultiResolutionIndex(centroids.get(i)) + " " + accuracy);
		}
		System.out.println("Overall accuracy " + getKNNAccuracy(learningDataset, testingDataset));
		
		if (featureNumber == 2)
			launch(args);
	}

	private static double evaluateSigmaMax(List<Sample> samples, int k) {
		double sigma = 0;
		for(Sample sample: samples) {
			sigma += evaluateLocalSigmaMax(sample.getSource().get(RealVector.class), samples, k);
		}
		return sigma / samples.size();
	}

	private static double evaluateLocalSigmaMax(RealVector v, List<Sample> samples, int k) {
		double[] distances = getDistances(v, samples);
		return distances[k+1];
	}

	@Override
	public void start(Stage stage) throws Exception {
		int i = 0;
		showScatterPlot(testingDataset, 0);
		for(Dataset cluster: testingClusters) {
			showScatterPlot(cluster, ++i);
		}
		showScatterPlot(learningDataset, -1);
	}

	private void showScatterPlot(Dataset cluster, int index) {
		Stage stage = new Stage();
		stage.setTitle("Scatter for cluster " + index);
        final NumberAxis x1Axis = new NumberAxis(-30, 30, 1);
        final NumberAxis x2Axis = new NumberAxis(-60, 60, 1);        
        final ScatterChart<Number,Number> sc = new
            ScatterChart<Number,Number>(x1Axis,x2Axis);
        x1Axis.setLabel("X1");
        x2Axis.setLabel("X2");
        sc.setTitle("Cluster " + index);
        
        Map<String, XYChart.Series> map = new HashMap<>();
        map.put( "1", new XYChart.Series("Positive", FXCollections.observableArrayList()));
        map.put("-1", new XYChart.Series("Negative", FXCollections.observableArrayList()));
        SampleIterator it = cluster.sampleIterator();
        while(it.hasNext()) {
        	Sample sample = it.next();
        	RealVector xy = sample.getSource().get(RealVector.class);
        	XYChart.Data data = new XYChart.Data(10*xy.getEntry(0), 10*xy.getEntry(1));
        	map.get(sample.getTarget().get()).getData().add(data);
        }
        
        for (XYChart.Series series: map.values()) {
	        if (!series.getData().isEmpty())
	        	sc.getData().add(series);
        }
        Scene scene = new Scene(sc, 1000, 800);
        stage.setScene(scene);
        stage.show();
	}

	private static List<Sample> splitKeepingProbability(List<Sample> samples, double percent) {
		List<Sample> ret = new ArrayList<>();
 		List<Sample> positiveSamples = getPositiveSamples(samples);
 		
 		double posProb = ((double)positiveSamples.size()) / samples.size();
 		
 		int posSize = (int) (posProb * percent * samples.size());
 		int negSize = (int) ((1-posProb) * percent * samples.size());
 		
 		ret.addAll(positiveSamples.subList(0, posSize));
 		
 		List<Sample> negativeSamples = new ArrayList<>(samples);
 		negativeSamples.removeAll(positiveSamples);
 		
 		ret.addAll(negativeSamples.subList(0, negSize));
 		
 		return ret;
	}

	private static List<Sample> getPositiveSamples(List<Sample> samples) {
		List<Sample> ret = new ArrayList<>();
		
		for (Sample sample: samples) {
			if (sample.getTarget().get().equals("1"))
				ret.add(sample);
		}
		
		return ret;
	}

	private static Dataset revertToDataset(List<Sample> profiledSamples) {
		Dataset ret = new Dataset(template);
		for(Sample sample: profiledSamples) {
			ret.add(new Instance(new Data(new Element(sample.getSource().get(1))), new Data(sample.getTarget())));
		}
		return ret;
	}

	private static double getKNNAccuracy(final Dataset trainSet, final Dataset testSet) {
		SimpleExperiment exp = new SimpleExperiment();
		exp.setContent("datasetBuilder", new DatasetBuilder() {
			@Override public void prepare() { }
			@Override public Dataset buildDataset() {return trainSet;}
		});
		exp.setContent("datasetBuilder.datasetTemplate", template);
		exp.setContent("testingDataset", new DatasetBuilder() {
			@Override public void prepare() { }
			@Override public Dataset buildDataset() {return testSet;}
		});
		exp.setContent("testingDataset.datasetTemplate", template);
		exp.setContent("classifier", prepareClassifier());
		
		for(String error: exp.getErrors())
			System.out.println(error);
		if (!exp.getErrors().isEmpty())
			return -1;
		
		ResultList results = exp.execute(null);
		StandardClassificationMetrics metrics = new StandardClassificationMetrics();
		metrics.prepare(results);
		return metrics.getAccuracy();
	}

	private static Classifier prepareClassifier() {
		KNNClassifier classifier = new KNNClassifier();
		
		classifier.setContent("k", 7);
		classifier.setContent("trainingAlgorithm", new SimpleKNNTraining());
		
		return classifier;
	}

	private static List<Dataset> revertToDatasets(List<List<Sample>> clusters) {
		List<Dataset> ret = new ArrayList<>();
		
		for(List<Sample> cluster: clusters)
			ret.add(revertToDataset(cluster));
		
		return ret;
	}

	private static double getMultiResolutionIndex(RealVector p) {
		double mri = 0;
		double normalizer = 0;
		
		for(int i = 0; i < profileSize; i++) {
			double wi = 1.0 - 1.0 * i / profileSize;
			mri += wi * (1 - p.getEntry(i));
			normalizer += 2*wi;
		}
		
		return mri / normalizer;
	}

	private static List<RealVector> applyKMeans(List<Sample> profiles) {
		List<RealVector> centroids = new ArrayList<>();
		
		int initialSize = profiles.size()/clusterNumber;
		for(int i = 0; i < clusterNumber; i++) {
			centroids.add(getCentroid(profiles.subList(i*initialSize, (i+1)*initialSize)));
		}
		
		int iteration = 0;
		while(iteration++ < maxKMeansIterations) {
			List< List<Sample> > clusters = getClusters(profiles, centroids);
			
			List<RealVector> nextCentroids = new ArrayList<>();
			for(List<Sample> cluster: clusters)
				nextCentroids.add(getCentroid(cluster));
			
			if (nextCentroids.equals(centroids)) {
				System.out.println("Found centroids after " + iteration + " iterations.");
				break;
			}
			
			centroids = nextCentroids;
		}
		
		return centroids;
	}

	private static List<List<Sample>> getClusters(List<Sample> profiles, List<RealVector> centroids) {
		List< List<Sample> > clusters = new ArrayList<>();
		for(int i = 0; i < clusterNumber; i++)
			clusters.add(new ArrayList<Sample>());
		
		for(Sample sample: profiles) {
			int i = getNearestCentroid(sample.getSource().get(0, RealVector.class), centroids);
			clusters.get(i).add(sample);
		}
		
		return clusters;
	}

	private static int getNearestCentroid(RealVector profile, List<RealVector> centroids) {
		int nearest = 0;
		for(int i = 1; i < clusterNumber; i++) {
			if (profile.getDistance(centroids.get(i)) < profile.getDistance(centroids.get(nearest)))
				nearest = i;
		}
		return nearest;
	}

	private static RealVector getCentroid(List<Sample> cluster) {
		RealVector ret = new ArrayRealVector(profileSize);
		
		for(Sample sample: cluster)
			ret = ret.add(sample.getSource().get(0, RealVector.class));
		ret.mapDivideToSelf(cluster.size());
		
		return ret;
	}

	private static List<Sample> applyMultiResolutionTransform(List<Sample> learningSamples, List<Sample> samples) {
		List<Sample> ret = new ArrayList<>();
		
		double posProb = getPositiveProbability(learningSamples);
		double negProb = 1 - posProb;
		
		for(Sample sample: samples) {
			RealVector profile = getProfile(sample.getSource().get(RealVector.class), learningSamples, posProb, negProb);
			
			ret.add(new Sample(new Element(profile, sample.getSource().get()), sample.getTarget()));
		}
		
		return ret;
	}

	private static double getPositiveProbability(List<Sample> samples) {
		double count = 0;
		for(Sample sample: samples) {
			if (sample.getTarget().get().equals("1"))
				count++;
		}
		return count/samples.size();
	}

	private static RealVector getProfile(RealVector v, List<Sample> learningSamples, double posProb, double negProb) {
		double[] distances = getDistances(v, learningSamples);
		
		RealVector profile = new ArrayRealVector(profileSize);
		
		for(int i = 0; i < profileSize; i++) {
//			double sigmaMax = evaluateLocalSigmaMax(v, learningSamples, (int) (learningSamples.size()*sigmaMaxPercent));
			double radius = sigmaMax * (i+1)/profileSize;
			profile.setEntry(i, getPsi(radius, distances, learningSamples, posProb, negProb));
		}
		
		return profile;
	}

	private static double getPsi(double radius, double[] distances, List<Sample> samples, double posProb, double negProb) {
		double posProbe = getProbe(radius, distances, samples,  "1");
		double negProbe = getProbe(radius, distances, samples, "-1");
		
		double psi = Math.abs(posProb * posProbe - negProb * negProbe) / (posProb * posProbe + negProb * negProbe);
		
		if (Double.isNaN(psi))
			return 0;
		
		return psi;
	}

	private static double getProbe(double radius, double[] distances, List<Sample> samples, String label) {
		double probe = 0;
		
		for(int i = 0; i < samples.size(); i++) {
			if (samples.get(i).getTarget().get().equals(label)) {
				if (distances[i] > 0 && distances[i] <= radius)
					probe++;
			}
		}
		
		return probe;
	}

	private static double[] getDistances(RealVector v, List<Sample> learning) {
		double[] ret = new double[learning.size()];
		for(int i = 0; i < learning.size(); i++) {
			RealVector other = learning.get(i).getSource().get(RealVector.class);
			ret[i] = v.getDistance(other);
		}
		return ret;
	}

	private static List<Sample> applyMahalanobisTransform(Dataset dataset) {
		List<Sample> ret = new ArrayList<>();
		
		// Obtain statistics
		MultivariateSummaryStatistics stats = new MultivariateSummaryStatistics(featureNumber, true);
		SampleIterator it = dataset.sampleIterator();
		while(it.hasNext()) {
			Sample sample = it.next();
			stats.addValue(sample.getSource().get(RealVector.class).toArray());
		}
		
		RealMatrix inverseSquareRoot = getInverseSquareRoot(stats.getCovariance());
		RealVector mean = new ArrayRealVector(stats.getMean());
		
		it.reset();
		while(it.hasNext()) {
			Sample sample = it.next();
			RealVector v = inverseSquareRoot.operate(sample.getSource().get(RealVector.class).subtract(mean));
			ret.add(new Sample(new Element(v), sample.getTarget()));
//			ret.add(new Sample(sample.getSource(), sample.getTarget()));
		}
		
		return ret;
	}

	private static RealMatrix getInverseSquareRoot(RealMatrix matrix) {
		EigenDecomposition dec = new EigenDecomposition(matrix);
		dec = new EigenDecomposition(dec.getSquareRoot());
		return dec.getSolver().getInverse();
	}

}
