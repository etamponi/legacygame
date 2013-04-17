package game.plugins.blocks.filters;

import game.core.Data;
import game.core.DatasetTemplate;
import game.core.Element;
import game.core.ElementTemplate;
import game.core.Sample;
import game.core.blocks.Filter;
import game.plugins.valuetemplates.LabelTemplate;
import game.plugins.valuetemplates.VectorTemplate;

import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class MultiResolutionTransform extends Filter {
	
	public int profileSize = 10;
	
	public List<Sample> reference;
	
	public double posProb = 0.5;
	
	public double sigmaMax = 1;
	
	public double defaultPsi = 0;
	
	public double multiplier = 1;

	@Override
	public String compatibilityError(DatasetTemplate object) {
		if (object.sourceTemplate.isSingletonTemplate(VectorTemplate.class)) {
			if (object.targetTemplate.isSingletonTemplate(LabelTemplate.class)) {
				if (object.targetTemplate.getSingleton(LabelTemplate.class).labels.size() != 2)
					return "can only handle singleton LabelTemplates with 2 labels";
				else
					return null;
			} else {
				return "can only handle singleton LabelTemplates with 2 labels";
			}
		} else
			return "can only handle singleton VectorTemplate as sourceTemplate";
	}

	@Override
	public Data transform(Data input) {
		Data ret = new Data();
		
		for(Element e: input) {
			RealVector v = e.get();
			ret.add(new Element(getProfile(v)));
		}
		
		return ret;
	}
	
	private RealVector getProfile(RealVector input) {
		double[] distances = getDistances(input);
		
		RealVector profile = new ArrayRealVector(profileSize);
		
		for(int i = 0; i < profileSize; i++) {
			double radius = (multiplier*profileSize + (i+1))/((1+multiplier)*profileSize) * sigmaMax;
			
			profile.setEntry(i, getPsi(radius, distances));
		}
		
		return profile;
	}
	
	private double getPsi(double radius, double[] distances) {
		String posLabel = datasetTemplate.targetTemplate.getSingleton(LabelTemplate.class).labels.get(0);
		String negLabel = datasetTemplate.targetTemplate.getSingleton(LabelTemplate.class).labels.get(1);
		double posProbe = getProbe(radius, distances, posLabel);
		double negProbe = getProbe(radius, distances, negLabel);
		
		double psi = Math.abs(posProb * posProbe - (1-posProb) * negProbe) / (posProb * posProbe + (1-posProb) * negProbe);
		
		if (Double.isNaN(psi)) { // nothing found around this element
			psi = defaultPsi;
		}
		
		return psi;
	}

	private double getProbe(double radius, double[] distances, String label) {
		double probe = 0;
		
		for(int i = 0; i < reference.size(); i++) {
			if (reference.get(i).getTarget().get().equals(label)) {
				if (distances[i] > 0 && distances[i] <= radius)
					probe++;
			}
		}
		
		return probe;
	}

	private double[] getDistances(RealVector v) {
		double[] ret = new double[reference.size()];
		for(int i = 0; i < reference.size(); i++) {
			RealVector other = reference.get(i).getSource().get(RealVector.class);
			ret[i] = v.getDistance(other);
		}
		return ret;
	}

	@Override
	protected void updateOutputTemplate() {
		if (profileSize > 0) {
			setContent("outputTemplate", new ElementTemplate(new VectorTemplate(profileSize)));
		} else {
			setContent("outputTemplate", null);
		}
	}

}
