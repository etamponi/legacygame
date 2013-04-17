package game.plugins.trainingalgorithms;

import game.core.Dataset;
import game.core.DatasetTemplate;
import game.core.Sample;
import game.core.TrainingAlgorithm;
import game.plugins.blocks.filters.CentroidClusterer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class KMeansAlgorithm extends TrainingAlgorithm<CentroidClusterer> {
	
	public int maxIterations = 100;

	@Override
	protected void train(Dataset dataset) {
		List<Sample> samples = dataset.toSampleList();
		block.setContent("centroids", findCentroids(samples));
	}
	
	private List<RealVector> findCentroids(List<Sample> training) {
		List<RealVector> centroids = new ArrayList<>();
		
		int initialSize = training.size()/block.clusterNumber;
		for(int i = 0; i < block.clusterNumber; i++) {
			centroids.add(getCentroid(training.subList(i*initialSize, (i+1)*initialSize)));
		}
		
		int iteration = 0;
		while(iteration++ < maxIterations) {
			List< List<Sample> > clusters = getClusters(training, centroids);
			
			List<RealVector> nextCentroids = new ArrayList<>();
			for(List<Sample> cluster: clusters)
				nextCentroids.add(getCentroid(cluster));
			
			if (nextCentroids.equals(centroids)) {
				break;
			}
			
			centroids = nextCentroids;
		}
		
		return centroids;
	}

	private List<List<Sample>> getClusters(List<Sample> training, List<RealVector> centroids) {
		List< List<Sample> > clusters = new ArrayList<>();
		for(int i = 0; i < block.clusterNumber; i++)
			clusters.add(new ArrayList<Sample>());
		
		for(Sample sample: training) {
			int i = getNearestCentroid(sample.getSource().get(0, RealVector.class), centroids);
			clusters.get(i).add(sample);
		}
		
		return clusters;
	}

	private int getNearestCentroid(RealVector v, List<RealVector> centroids) {
		int nearest = 0;
		for(int i = 1; i < block.clusterNumber; i++) {
			if (v.getDistance(centroids.get(i)) < v.getDistance(centroids.get(nearest)))
				nearest = i;
		}
		return nearest;
	}

	private RealVector getCentroid(List<Sample> cluster) {
		RealVector ret = new ArrayRealVector(block.datasetTemplate.sourceTemplate.getContent("0.dimension", int.class));
		
		for(Sample sample: cluster)
			ret = ret.add(sample.getSource().get(0, RealVector.class));
		if (cluster.size() != 0)
			ret.mapDivideToSelf(cluster.size());
		
		return ret;
	}

	@Override
	protected String getTrainingPropertyNames() {
		return "centroids";
	}

	@Override
	protected String compatibilityError(DatasetTemplate datasetTemplate) {
		return null;
	}

}
