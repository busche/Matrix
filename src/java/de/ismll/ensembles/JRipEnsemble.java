package de.ismll.ensembles;

import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.core.Instances;

public class JRipEnsemble extends WekaClassifierbasedEnsemble {

	@Override
	protected Classifier buildClassifier(Instances wtrain)
			throws EnsembleException {
		JRip rip = new JRip();

		try {
			rip.buildClassifier(wtrain);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		return rip;
	}

}
