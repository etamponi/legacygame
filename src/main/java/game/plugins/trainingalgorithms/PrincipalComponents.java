package game.plugins.trainingalgorithms;

import com.ios.triggers.MasterSlaveTrigger;
import game.core.Dataset;
import game.core.DatasetTemplate;
import game.core.Sample;
import game.core.TrainingAlgorithm;
import game.plugins.blocks.filters.LinearTransform;
import game.plugins.valuetemplates.VectorTemplate;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;

public class PrincipalComponents extends TrainingAlgorithm<LinearTransform> {

    public int components = 5;

    public PrincipalComponents() {
        addTrigger(new MasterSlaveTrigger(this, "components", "block.outputDimension"));
    }

    @Override
    protected void train(Dataset dataset) {
        int featureNumber = dataset.getTemplate().sourceTemplate.getSingleton(VectorTemplate.class).dimension;

		// Obtain statistics
		MultivariateSummaryStatistics stats = new MultivariateSummaryStatistics(featureNumber, false);
		Dataset.SampleIterator it = dataset.sampleIterator();
		while(it.hasNext()) {
			Sample sample = it.next();
			stats.addValue(sample.getSource().get(RealVector.class).toArray());
		}

        PearsonsCorrelation corr = new PearsonsCorrelation(stats.getCovariance(), (int)stats.getN());
        RealMatrix eigenMatrix = getEigenVectorMatrix(corr.getCorrelationMatrix());

        RealMatrix transform = eigenMatrix.getSubMatrix(0, components-1, 0, eigenMatrix.getColumnDimension()-1);
        for(int i = 0; i < components; i++) {
            for(int j = 0; j < transform.getColumnDimension(); j++) {
                transform.setEntry(i, j, transform.getEntry(i, j) / Math.sqrt(stats.getCovariance().getEntry(j, j)));
            }
        }

		block.setContent("matrix", transform);
		block.setContent("offset", new ArrayRealVector(stats.getMean()));
    }

    private RealMatrix getEigenVectorMatrix(RealMatrix covariance) {
        EigenDecomposition dec = new EigenDecomposition(covariance);
        RealMatrix eig = dec.getVT();
        RealVector lam = new ArrayRealVector(dec.getRealEigenvalues());

        RealMatrix ret = new Array2DRowRealMatrix(eig.getRowDimension(), eig.getColumnDimension());
        for (int i = 0; i < eig.getRowDimension(); i++) {
            int maxIndex = lam.getMaxIndex();

            ret.setRowVector(i, eig.getRowVector(maxIndex).unitVector());

            lam.setEntry(maxIndex, -1);
        }

        return ret;
    }

    @Override
    protected String getTrainingPropertyNames() {
        return "matrix offset";
    }

    @Override
    protected String compatibilityError(DatasetTemplate datasetTemplate) {
        return null;
    }
}
