package mri.main;

import game.core.*;
import org.apache.commons.math3.linear.RealVector;

public class MultiResolutionIndex extends Metrics<MultiResolutionExperiment.MultiResolutionResult> {
    @Override
    protected LabeledMatrix evaluateMetrics(MultiResolutionExperiment.MultiResolutionResult result) {
        LabeledMatrix ret = new LabeledMatrix(1, 1);
        ret.setRowLabels("mri");
        ret.setColumnLabels("mri");

        ret.setEntry(0, 0, evaluateMeanMRI(result.transformedDataset));

        return ret;
    }

    private double evaluateMeanMRI(Dataset transformedDataset) {
        double ret = 0;
        for(Instance i: transformedDataset) {
            ret += getMultiResolutionIndex(i.getSource().get().get(RealVector.class));
        }
        return ret / transformedDataset.size();
    }

    private double getMultiResolutionIndex(RealVector v) {
        double mri = 0;

        for(int i = 0; i < v.getDimension(); i++) {
            double wi = 1.0 - 1.0 * i / v.getDimension();
            mri += wi * (1.0 - v.getEntry(i));
        }

        return mri / (2 * v.getDimension());
    }

    @Override
    protected void prepareForEvaluation(MultiResolutionExperiment.MultiResolutionResult result) {
        // Nothing to do
    }

    @Override
    public String compatibilityError(Result object) {
        return null;
    }
}
