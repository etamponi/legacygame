package game.plugins.blocks.filters;

import game.core.Data;
import game.core.DatasetTemplate;
import game.core.Element;
import game.core.ElementTemplate;
import game.core.blocks.Filter;
import game.plugins.valuetemplates.VectorTemplate;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class LinearTransform extends Filter {
	
	public int outputDimension;
	
	public RealMatrix matrix;
	
	public RealVector offset;

	@Override
	public String compatibilityError(DatasetTemplate template) {
		return template.sourceTemplate != null &&
				template.sourceTemplate.isSingletonTemplate(VectorTemplate.class)
				? null : "sourceTemplate must be a singleton VectorTemplate";
	}

	@Override
	public Data transform(Data input) {
		Data ret = new Data();
		
		for(Element e: input) {
			RealVector v = e.get();
			RealVector vret = matrix.operate(v.subtract(offset));
			ret.add(new Element(vret));
		}
		
		return ret;
	}

	@Override
	protected void updateOutputTemplate() {
		setContent("outputTemplate", new ElementTemplate(new VectorTemplate(outputDimension)));
	}

}
