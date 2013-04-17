package main;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import main.MultiResolutionAnalysis3.Configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.ios.IObject;

public class MultiResolutionAnalysisPostProcess {

	public static void main(String[] args) throws Exception {
		Kryo kryo = IObject.getKryo();
		Input input = new Input(new FileInputStream("results_around5_2clusters.dat"));
		List<Configuration> confs = (List<Configuration>) kryo.readClassAndObject(input);
		input.close();
		for(Configuration c: confs) {
			System.out.println(c);
			for (String dataset: c.accuracies.keySet())
				System.out.println(dataset + ": " + Arrays.toString(c.accuracies.get(dataset)[2]) + " " + Arrays.toString(c.mries.get(dataset)[2]));
		}
		/*
		Kryo kryo = IObject.getKryo();
		Input input = new Input(new FileInputStream("results_j48.dat"));
		List<Configuration> confsj48 = (List<Configuration>) kryo.readClassAndObject(input);
		input.close();
		int bestconfj48index = getBestConfIndex(confsj48);
		input = new Input(new FileInputStream("results_knn.dat"));
		List<Configuration> confsknn = (List<Configuration>) kryo.readClassAndObject(input);
		input.close();
		int bestconfknnindex = getBestConfIndex(confsknn);
		
		System.out.println(confsj48.get(bestconfknnindex));
		
		System.out.println(confsknn.get(bestconfj48index));
		
		Configuration p10j48 = getConfiguration(confsj48, 10, 0.15, 0.0, 0.0, true, 3, WekaClassifier.class);
		Configuration p10knn = getConfiguration(confsknn, 10, 0.15, 0.0, 0.0, true, 3, KNNClassifier.class);

		System.out.println(p10j48);
		System.out.println(p10knn);
		*/
	}
	
	private static Configuration getConfiguration(List<Configuration> list, int profileSize, double sigmaMaxPercent, double defaultPsi, double multiplier, boolean doMahalanobis, int clusterNumber, Class<?> classifier) {
		for (Configuration conf: list) {
			if (profileSize == conf.profileSize &&
					sigmaMaxPercent == conf.sigmaMaxPercent &&
					defaultPsi == conf.defaultPsi &&
					multiplier == conf.multiplier &&
					doMahalanobis == conf.doMahalanobis &&
					clusterNumber == conf.clusterNumber &&
					classifier == conf.classifier.getClass())
				return conf;
		}
		return null;
	}
	
	private static int getBestConfIndex(List<Configuration> confs) throws Exception {
		int bestconfindex = 0;
		Configuration bestconf = confs.get(0);
		ListIterator<Configuration> it = confs.listIterator(1);
		while(it.hasNext()) {
			Configuration conf = it.next();
			if (isBetter(conf, bestconf)) {
				bestconfindex = it.nextIndex()-1;
				bestconf = conf;
			}
		}
		
		System.out.println(bestconf);
		return bestconfindex;
	}
	
	private static boolean isBetter(Configuration current, Configuration previous) {
		int sumCurrent = sumAll(current.rankCorrs);
		int sumPrevious = sumAll(previous.rankCorrs);
		return sumCurrent > sumPrevious;
	}

	private static int sumAll(HashMap<String, double[]> rankCorrs) {
		int ret = 0;
		for(double[] value: rankCorrs.values()) {
			for(double v: value)
				ret += (int)v;
		}
		return ret;
	}

}
