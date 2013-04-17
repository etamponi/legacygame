package game.plugins.trainingalgorithms;

import game.core.Dataset;
import game.core.DatasetTemplate;
import game.core.Sample;
import game.core.TrainingAlgorithm;
import game.core.Dataset.SampleIterator;
import game.plugins.blocks.filters.MultiResolutionTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;

public class SimpleMRTraining extends
		TrainingAlgorithm<MultiResolutionTransform> {
	
	public double sigmaMaxPercent = 0.3;

	@Override
	protected void train(Dataset dataset) {
		List<Sample> reference = new ArrayList<>();
		
		SampleIterator it = dataset.sampleIterator();
		while(it.hasNext()) {
			reference.add(it.next());
		}
		
		block.setContent("reference", reference);
		block.setContent("posProb", getPositiveProbability(reference, dataset.getTemplate().getContent("targetTemplate.0.labels.0", String.class)));
		block.setContent("sigmaMax", evaluateSigmaMax());
//		block.setContent("sigmas", evaluateSigmas());
	}
	
	private static double getPositiveProbability(List<Sample> samples, String label) {
		double count = 0;
		for(Sample sample: samples) {
			if (sample.getTarget().get().equals(label))
				count++;
		}
		return count/samples.size();
	}

	@Override
	protected String getTrainingPropertyNames() {
		return "reference posProb sigmaMax";
	}

	@Override
	protected String compatibilityError(DatasetTemplate datasetTemplate) {
		return null;
	}
	
	private double evaluateSigmaMax() {
		double sigma = 0;
		for(Sample sample: block.reference) {
			sigma += evaluateLocalSigmaMax(sample.getSource().get(RealVector.class));
		}
		sigma = sigma/block.reference.size();
		return sigma;
	}
	
	private double evaluateLocalSigmaMax(RealVector v) {
		double[] distances = getDistances(v);
		Arrays.sort(distances);
		return distances[(int)(sigmaMaxPercent*block.reference.size())-1];
	}
	
	private double[] getDistances(RealVector v) {
		double[] ret = new double[block.reference.size()];
		for(int i = 0; i < block.reference.size(); i++) {
			RealVector other = block.reference.get(i).getSource().get(RealVector.class);
			ret[i] = v.getDistance(other);
		}
		return ret;
	}

}
