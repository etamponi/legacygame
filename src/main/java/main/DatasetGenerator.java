package main;

import game.core.Data;
import game.core.Dataset;
import game.core.Dataset.SampleIterator;
import game.core.DatasetTemplate;
import game.core.Element;
import game.core.ElementTemplate;
import game.core.Instance;
import game.core.Sample;
import game.plugins.valuetemplates.LabelTemplate;
import game.plugins.valuetemplates.VectorTemplate;

import java.io.FileWriter;
import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class DatasetGenerator {
	
	private static final Random random = new Random();
	
	private static final DatasetTemplate template = new DatasetTemplate(new ElementTemplate(new VectorTemplate(2)), new ElementTemplate(new LabelTemplate("1","-1")));
	
	private static final String datasetName = "data/noisy-xor";
	
	private static final double noiseProbability = 0.0;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Dataset dataset = new Dataset(template);
		
		for(int i = 0; i < 1000; i++) {
			RealVector v = generateRandom();
			String label = getLabel(v);
			dataset.add(new Instance(new Data(new Element(v)), new Data(new Element(label))));
		}
		
		FileWriter writer = new FileWriter(datasetName+".data.csv");
		writer.write("f01,f02,class\n");
		SampleIterator it = dataset.sampleIterator();
		while(it.hasNext()) {
			Sample sample = it.next();
			RealVector v = sample.getSource().get();
			String label = sample.getTarget().get();
			for(int i = 0; i < 2; i++)
				writer.write(v.getEntry(i) + ",");
			writer.write(label+"\n");
		}
		writer.close();
	}

	private static String getLabel(RealVector v) {
		boolean positive = v.getEntry(0)*v.getEntry(1) > 0;
		if (random.nextDouble() < noiseProbability)
			positive = !positive;
		return positive ? "1" : "-1";
	}

	private static RealVector generateRandom() {
		RealVector ret = new ArrayRealVector(2);
		ret.setEntry(0, 2*(random.nextDouble()-0.5));
		ret.setEntry(1, 2*(random.nextDouble()-0.5));
		return ret;
	}

}
