package game.plugins.blocks.filters;

import game.core.Data;
import game.core.DatasetTemplate;
import game.core.Element;
import game.core.ElementTemplate;
import game.core.blocks.Filter;
import game.plugins.valuetemplates.LabelTemplate;
import game.plugins.valuetemplates.VectorTemplate;

import java.util.List;

import org.apache.commons.math3.linear.RealVector;

public class CentroidClusterer extends Filter {
	
	public int clusterNumber = 3;
	
	public List<RealVector> centroids;

	@Override
	public String compatibilityError(DatasetTemplate object) {
		if (object.sourceTemplate.isSingletonTemplate(VectorTemplate.class))
			return null;
		else
			return "can only handle a singleton VectorTemplate as input";
	}

	@Override
	public Data transform(Data input) {
		Data ret = new Data();
		
		for(Element e: input) {
			RealVector v = e.get();
			String label = String.valueOf(getNearestCentroid(v));
			ret.add(new Element(label));
		}
		
		return ret;
	}
	
	private int getNearestCentroid(RealVector v) {
		int nearest = 0;
		for(int i = 1; i < clusterNumber; i++) {
			if (v.getDistance(centroids.get(i)) < v.getDistance(centroids.get(nearest)))
				nearest = i;
		}
		return nearest;
	}

	@Override
	protected void updateOutputTemplate() {
		String[] labels = new String[clusterNumber];
		for(int i = 0; i < clusterNumber; i++)
			labels[i] = String.valueOf(i);
		setContent("outputTemplate", new ElementTemplate(new LabelTemplate(labels)));
		
	}

}
