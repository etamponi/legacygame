package mri.main;

import game.core.*;
import game.plugins.valuetemplates.LabelTemplate;
import game.plugins.valuetemplates.VectorTemplate;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiResolutionTransform {

    private int profileSize;
    private double sigmaMaxPercent;
    private double defaultPsi;

    public MultiResolutionTransform(int profileSize, double sigmaMaxPercent, double defaultPsi) {
        this.profileSize = profileSize;
        this.sigmaMaxPercent = sigmaMaxPercent;
        this.defaultPsi = defaultPsi;
    }

    public Dataset apply(Dataset dataset) {
        Dataset ret = new Dataset(new DatasetTemplate(new ElementTemplate(new VectorTemplate(profileSize)), dataset.getTemplate().targetTemplate));

        double sigmaMax = getSigmaMax(dataset);
        double percent0 = 0.5; // getFirstLabelPercent(dataset);

        for(Instance instance: dataset) {
            RealVector profile = getProfile(instance, dataset, sigmaMax, percent0);

            ret.add(new Instance(new Data(new Element(profile)), instance.getTarget()));
        }

        return ret;
    }

    private double getSigmaMax(Dataset dataset) {
        double ret = 0;
        int threshold = (int)(dataset.size() * sigmaMaxPercent);
        for(Instance instance: dataset) {
            List<Double> distances = getDistances(instance, dataset);
            Collections.sort(distances);
            ret += distances.get(threshold);
        }
        return ret / dataset.size();
    }

    private double getFirstLabelPercent(Dataset dataset) {
        String label0 = dataset.getTemplate().getContent("targetTemplate.0.labels.0");
        double ret = 0;
        for(Instance i: dataset) {
            String label = i.getTarget().get().get();
            if (label.equals(label0)) {
                ret++;
            }
        }
        return ret / dataset.size();
    }

    private RealVector getProfile(Instance instance, Dataset dataset, double sigmaMax, double percent0) {
        List<Double> distances = getDistances(instance, dataset);
        double y = getSign(instance.getTarget().get().get(String.class),
                dataset.getTemplate().getContent("targetTemplate.0.labels.0", String.class));

        RealVector ret = new ArrayRealVector(profileSize);

        for(int i = 0; i < profileSize; i++) {
            double sigma = getSigma(i, sigmaMax);
            ret.setEntry(i, getPsi(y, distances, dataset, sigma, percent0));
        }

        return ret;
    }

    private double getPsi(double y, List<Double> distances, Dataset dataset, double sigma, double percent0) {
        double percent1 = 1 - percent0;
        double probe0 = getProbe(distances, dataset, sigma,  1);
        double probe1 = getProbe(distances, dataset, sigma, -1);

        double psi = y * (percent0 * probe0 - percent1 * probe1) / (percent0 * probe0 + percent1 * probe1);
        if (Double.isNaN(psi))
            return defaultPsi;
        else
            return psi;
    }

    private double getProbe(List<Double> distances, Dataset dataset, double sigma, int y) {
        String label0 = dataset.getTemplate().getContent("targetTemplate.0.labels.0");
        double count = 0;
        for(int i = 0; i < dataset.size(); i++) {
            double dist = distances.get(i);
            String label = dataset.get(i).getTarget().get().get();
            int currY = getSign(label, label0);
            if (currY == y && dist <= sigma) {
                count++;
            }
        }
        return count;
    }

    private double getSigma(int i, double sigmaMax) {
        return sigmaMax * (i+1) / profileSize;
    }

    private List<Double> getDistances(Instance current, Dataset dataset) {
        RealVector input = current.getSource().get().get();

        List<Double> ret = new ArrayList<>(dataset.size());

        for(Instance other: dataset) {
            ret.add(getDistance(input, other));
        }

        return ret;
    }

    private double getDistance(RealVector input, Instance other) {
        RealVector otherInput = other.getSource().get().get();

        return input.getDistance(otherInput);
    }

    private int getSign(String label, String label0) {
        return label.equals(label0) ? 1 : -1;
    }

}
