/*
 * A batch filter enabling features to be extracted from images.
 * Uses the LIRE 0.9.3 feature extraction libaries.
 * 
 * The first attribute in the data *must* be a string, specifically the filename 
 * of the image. Image feature attributes are added to the dataset by the filter, 
 * and the all string attributes are removed.
 */

package weka.filters.unsupervised.instance.imagefilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.imageanalysis.BasicFeatures;
import weka.core.*;
import weka.core.Capabilities.*;
import weka.filters.*;

public class BasicImageFeatures extends SimpleBatchFilter {

	private static final long serialVersionUID = 8658527406486139573L;

	public String globalInfo() {
		return "A batch filter for extracting eight basic features from images.";
	}

	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		return result;
	}

	protected Instances determineOutputFormat(Instances inputFormat) {
		Instances result = new Instances(inputFormat, 0);
		int numFeatures=getNumFeatures();
		for (int index = 0; index < numFeatures; index++) {
			result.insertAttributeAt(new Attribute("f" + index),
					result.numAttributes() - 1);
		}
		result.deleteAttributeAt(0);
		return result;
	}

	protected Instances process(Instances inst) throws Exception {
		// check that first attribute is a string
		if (!inst.attribute(0).isString()) {
			throw new Exception(
					"ImageFilter Exception: dataset's first attribute must be a string!");
		}
		// create the new dataset
		Instances result = new Instances(determineOutputFormat(inst), 0);
		// iterate over the instances
		for (int example_index = 0; example_index < inst.numInstances(); example_index++) {
			// load the file from disk into a buffered image
			String filename = inst.instance(example_index).stringValue(0);
			BufferedImage img = null;
			try {
				img = ImageIO.read(new File(filename));
			} catch (IOException e) {
				throw new Exception(
						"ImageFilter Exception: was unable to read file "
								+ filename + "!");
			}
			// extract features from the buffered image
			double[] features=getFeatures(img);
			// create the final feature vector and new instance
			double[] values = new double[result.numAttributes()];
			values[values.length - 1] = inst.instance(example_index).classValue();
			for (int att_index=1; att_index<inst.numAttributes();att_index++)
				values[att_index-1]=inst.instance(example_index).value(att_index);
			for (int att_index=0; att_index<features.length;att_index++)
				values[values.length - 1 - features.length + att_index]=features[att_index];
			result.add(new DenseInstance(1, values));
		}
		// done -- return new dataset
		return result;
	}

	// overriable methods for so subclasses can extract different features
	protected int getNumFeatures(){
		return 8;
	}
	protected double[] getFeatures(BufferedImage img){
		BasicFeatures features=new BasicFeatures();
		features.extract(img);
		return features.getDoubleHistogram();
	}
	
	public static void main(String[] args) {
		runFilter(new BasicImageFeatures(), args);
	}
}