package main;

import game.core.Dataset;
import game.core.DatasetBuilder;
import game.core.Instance;
import game.core.ResultList;
import game.core.blocks.Classifier;
import game.plugins.blocks.classifiers.KNNClassifier;
import game.plugins.blocks.filters.CentroidClusterer;
import game.plugins.blocks.filters.LinearTransform;
import game.plugins.blocks.filters.MultiResolutionTransform;
import game.plugins.datasetbuilders.CSVDatasetLoader;
import game.plugins.experiments.SimpleExperiment;
import game.plugins.metrics.StandardClassificationMetrics;
import game.plugins.trainingalgorithms.KMeansAlgorithm;
import game.plugins.trainingalgorithms.MahalanobisTransform;
import game.plugins.trainingalgorithms.SimpleKNNTraining;
import game.plugins.trainingalgorithms.SimpleMRTraining;
import game.plugins.weka.algorithms.WekaJ48;
import game.plugins.weka.classifiers.WekaClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.ios.IObject;

public class MultiResolutionAnalysis3 {
	
	public static class Configuration {
		
		public int profileSize;
		public double sigmaMaxPercent;
		public double defaultPsi;
		public double multiplier;
		public boolean doMahalanobis;
		public int clusterNumber;
		public Classifier classifier;
		
		public HashMap<String, double[][]> accuracies = new HashMap<>();
		public HashMap<String, double[][]> mries = new HashMap<>();
		public HashMap<String, double[]> rankCorrs = new HashMap<>();
		
		@Override
		public String toString() {
			StringBuilder ret = new StringBuilder();
			
			ret.append("profileSize: ").append(profileSize).append("\n");
			ret.append("sigmaMaxPercent: ").append(sigmaMaxPercent).append("\n");
			ret.append("defaultPsi: ").append(defaultPsi).append("\n");
			ret.append("multiplier: ").append(multiplier).append("\n");
			ret.append("doMahalanobis: ").append(doMahalanobis).append("\n");
			ret.append("clusterNumber: ").append(clusterNumber).append("\n");
			ret.append("classifier: ").append(classifier).append("\n");
			for(String key: rankCorrs.keySet())
				ret.append("\trankCorr: ").append(Arrays.toString(rankCorrs.get(key))).append("\t").append(key).append("\n");
			
			return ret.toString();
		}
		
	}
	
	private static final List<Configuration> configurations = prepareConfigurations();
	
	private static List<Configuration> prepareConfigurations() {
		
		int[] profileSizes = { 26, 28, 30, 32, 34 }; // 10, 15, 20, 25 };
		double[] sigmaMaxPercents = { 0.15 }; // 0.10, 0.15, 0.20, 0.25, 0.30 };
		double[] defaultPsis = { 0.0 }; //, 1.0 };
		double[] multipliers = { 0.0 }; //, 1.0, 2.0 };
		boolean[] doMahalanobises = { true }; //, false };
		int[] clusterNumbers = { 3 }; //, 5 };
		Classifier[] classifiers = { new KNNClassifier(), new WekaClassifier()/*, new WekaClassifier()*/ };
		classifiers[0].setContent("k", 7);
		classifiers[0].setContent("trainingAlgorithm", new SimpleKNNTraining());
//		classifiers[1].setContent("trainingAlgorithm", new WekaMultilayerPerceptron());
		classifiers[1].setContent("trainingAlgorithm", new WekaJ48());
		
		List<Configuration> ret = new ArrayList<>();
		
		for(int profileSize: profileSizes) {
			for(double sigmaMaxPercent: sigmaMaxPercents) {
				for(double defaultPsi: defaultPsis) {
					for(double multiplier: multipliers) {
						for(boolean doMahalanobis: doMahalanobises) {
							for(int clusterNumber: clusterNumbers) {
								for(Classifier classifier: classifiers) {
									Configuration conf = new Configuration();
									conf.profileSize = profileSize;
									conf.sigmaMaxPercent = sigmaMaxPercent;
									conf.defaultPsi = defaultPsi;
									conf.multiplier = multiplier;
									conf.doMahalanobis = doMahalanobis;
									conf.clusterNumber = clusterNumber;
									conf.classifier = classifier;
									ret.add(conf);
								}
							}
						}
					}
				}
			}
		}
		
		return ret;
		
	}
	
	private static String[] datasets = {
			"appendicitis", "banana", "breast-cancer-original",
			"mammographic-masses", "phoneme", "pima",
			"ring", "spambase", "twonorm", "wdbc" };

	private static int folds = 10;
	
	private static double learningPercent = 0.5;

	public static void main(String[] args) throws Exception {
		
		for(Configuration conf: configurations) {
			
			for(String dataset: datasets) {
				double[][] accuracy = new double[folds][conf.clusterNumber];
				double[][] mri = new double[folds][conf.clusterNumber];
				double[] rankCorr = new double[folds];
				
				try {
					System.out.print(String.format("%25s: ", dataset));
					CSVDatasetLoader loader = new CSVDatasetLoader();
					loader.setContent("hasHeader", false);
					loader.setContent("instanceNumber", 2000);
					loader.setContent("file", new File("data/keel/"+dataset+".dat"));
					loader.prepare();
					
					Dataset ds = loader.buildDataset();
					
					for(int fold = 0; fold < folds; fold++) {
						Dataset ls = ds.getRandomSubset(learningPercent);
						
						Dataset ts = new Dataset(ds); ts.removeAll(ls);
						
						if (conf.doMahalanobis) {
							LinearTransform mahalanobis = new LinearTransform();
							mahalanobis.setContent("datasetTemplate", ls.getTemplate());
							mahalanobis.setContent("trainingAlgorithm", new MahalanobisTransform());
							mahalanobis.trainingAlgorithm.execute(ls);
							
							ls = ls.apply(mahalanobis);
							ts = ts.apply(mahalanobis);
						}
						
						MultiResolutionTransform mrt = new MultiResolutionTransform();
						mrt.setContent("datasetTemplate", ls.getTemplate());
						mrt.setContent("profileSize", conf.profileSize);
						mrt.setContent("defaultPsi", conf.defaultPsi);
						mrt.setContent("multiplier", conf.multiplier);
						mrt.setContent("trainingAlgorithm", new SimpleMRTraining());
						mrt.setContent("trainingAlgorithm.sigmaMaxPercent", conf.sigmaMaxPercent);
						mrt.trainingAlgorithm.execute(ls);
						
						Dataset mrls = ls.apply(mrt);
						Dataset mrts = ts.apply(mrt);
						
						CentroidClusterer kmeans = new CentroidClusterer();
						kmeans.setContent("clusterNumber", conf.clusterNumber);
						kmeans.setContent("datasetTemplate", mrls.getTemplate());
						kmeans.setContent("trainingAlgorithm", new KMeansAlgorithm());
						kmeans.trainingAlgorithm.execute(mrls);
						
						/*
						Collections.sort(kmeans.centroids, new Comparator<RealVector>() {
							@Override public int compare(RealVector o1, RealVector o2) {
								return Double.compare(getMultiResolutionIndex(o1), getMultiResolutionIndex(o2));
							}
						});
						*/
						Dataset clusterLabels = mrts.apply(kmeans);
						List<Dataset> clusters = splitByCluster(ts, clusterLabels, kmeans.clusterNumber);
						List<Dataset> mrclusters = splitByCluster(mrts, clusterLabels, kmeans.clusterNumber);
						
						for(int cluster = 0; cluster < clusters.size(); cluster++) {
							Dataset split = clusters.get(cluster);
							accuracy[fold][cluster] = getAccuracy(ls, split, conf.classifier);
//							mri[fold][cluster] = getMultiResolutionIndex(kmeans.centroids.get(cluster));
							mri[fold][cluster] = getMeanMultiResolutionIndex(mrclusters.get(cluster));
						}
						
						rankCorr[fold] = evaluateRankCorrelation(accuracy[fold], mri[fold]);
						
						System.out.print(". ");
					}
					conf.accuracies.put(dataset, accuracy);
					conf.mries.put(dataset, mri);
					conf.rankCorrs.put(dataset, rankCorr);
				} catch (Exception e) {
					System.out.print("error");
				}
				System.out.println();
			}
			System.out.println(conf);
		}
		
		Kryo kryo = IObject.getKryo();
		Output output = new Output(new FileOutputStream("results_around30_3clusters.dat"));
		kryo.writeClassAndObject(output, configurations);
		output.close();
	}

	private static double getMeanMultiResolutionIndex(Dataset dataset) {
		double ret = 0;
		for(Instance i: dataset) {
			ret += getMultiResolutionIndex(i.getSource().get().get(RealVector.class));
		}
		return ret / dataset.size();
	}

	private static double evaluateRankCorrelation(double[] x, double[] y) {
		SpearmansCorrelation corr = new SpearmansCorrelation();
		return corr.correlation(x, y);
	}

	private static double getAccuracy(final Dataset trainSet, final Dataset testSet, final Classifier classifier) {
		SimpleExperiment exp = new SimpleExperiment();
		exp.setContent("datasetBuilder", new DatasetBuilder() {
			@Override public void prepare() { }
			@Override public Dataset buildDataset() {return trainSet;}
		});
		exp.setContent("datasetBuilder.datasetTemplate", trainSet.getTemplate());
		exp.setContent("testingDataset", new DatasetBuilder() {
			@Override public void prepare() { }
			@Override public Dataset buildDataset() {return testSet;}
		});
		exp.setContent("testingDataset.datasetTemplate", trainSet.getTemplate());
		exp.setContent("classifier", classifier.copy());
		
		for(String error: exp.getErrors())
			System.out.println(error);
		if (!exp.getErrors().isEmpty())
			return -1;
		
		ResultList results = exp.execute(null);
		StandardClassificationMetrics metrics = new StandardClassificationMetrics();
		metrics.prepare(results);
		return metrics.getAccuracy();
	}
	
	private static double getMultiResolutionIndex(RealVector p) {
		double mri = 0;
		double normalizer = 0;
		
		for(int i = 0; i < p.getDimension(); i++) {
			double wi = 1.0 - 1.0 * i / p.getDimension();
			mri += wi * p.getEntry(i);
			normalizer += 2*wi;
		}
		
		return mri / normalizer;
	}

	private static List<Dataset> splitByCluster(Dataset ds, Dataset clusters, int n) {
		List<Dataset> ret = new ArrayList<>();
		for(int i = 0; i < n; i++)
			ret.add(new Dataset(ds.getTemplate()));
		
		for(int i = 0; i < ds.size(); i++) {
			ret.get(Integer.valueOf(clusters.get(i).getSource().get().get(String.class))).add(ds.get(i));
		}
		
		return ret;
	}
	
}
