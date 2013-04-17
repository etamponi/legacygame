package game.plugins.trainingalgorithms;

import game.core.Dataset;
import game.core.Dataset.SampleIterator;
import game.core.DatasetTemplate;
import game.core.Sample;
import game.core.TrainingAlgorithm;
import game.plugins.blocks.filters.LinearTransform;
import game.plugins.valuetemplates.VectorTemplate;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;

import com.ios.Property;
import com.ios.triggers.BoundProperties;

public class MahalanobisTransform extends TrainingAlgorithm<LinearTransform> {
	
	public boolean adjustStats = true;
	
	public MahalanobisTransform() {
		addTrigger(new BoundProperties<MahalanobisTransform>("block.outputDimension") {
			private boolean listening = true;
			@Override
			public void action(Property changedPath) {
				if (listening) {
					listening = false;
					Integer dim = getRoot().getContent("block.datasetTemplate.sourceTemplate.0.dimension");
					if (dim != null)
						getRoot().setContent("block.outputDimension", dim);
					listening = true;
				}
			}
		});
	}

	@Override
	protected void train(Dataset dataset) {
		int featureNumber = dataset.getTemplate().sourceTemplate.getSingleton(VectorTemplate.class).dimension;
		
		// Obtain statistics
		MultivariateSummaryStatistics stats = new MultivariateSummaryStatistics(featureNumber, adjustStats);
		SampleIterator it = dataset.sampleIterator();
		while(it.hasNext()) {
			Sample sample = it.next();
			stats.addValue(sample.getSource().get(RealVector.class).toArray());
		}
		
		RealMatrix inverseSquareRoot = getInverseSquareRoot(stats.getCovariance());
		RealVector mean = new ArrayRealVector(stats.getMean());
		
		block.setContent("matrix", inverseSquareRoot);
		block.setContent("offset", mean);
	}

	@Override
	protected String getTrainingPropertyNames() {
		return "matrix offset";
	}

	@Override
	protected String compatibilityError(DatasetTemplate datasetTemplate) {
		return null;
	}
	
	private static RealMatrix getInverseSquareRoot(RealMatrix matrix) {
		EigenDecomposition dec = new EigenDecomposition(matrix);
		dec = new EigenDecomposition(dec.getSquareRoot());
		return dec.getSolver().getInverse();
	}

}
