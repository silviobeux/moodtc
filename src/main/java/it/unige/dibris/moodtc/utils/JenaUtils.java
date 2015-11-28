package it.unige.dibris.moodtc.utils;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class JenaUtils {
	
	public static OntModel ONTMODEL;
	
	public static void jenaConfig() {
		// JENA PART
		// Create an empty in-memory ontology model
		OntDocumentManager mgr = new OntDocumentManager();
		OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_DL_MEM);
		s.setDocumentManager(mgr);
		ONTMODEL = ModelFactory.createOntologyModel(s, null);
		ONTMODEL.setStrictMode(false);
	}
}
