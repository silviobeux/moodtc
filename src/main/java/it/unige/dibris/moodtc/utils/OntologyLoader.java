package it.unige.dibris.moodtc.utils;

import java.io.InputStream;
import java.util.LinkedList;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import it.uniroma1.lcl.jlt.util.Language;

public class OntologyLoader {
	
	private String filename;
	private Language language;
	private static OntModel ontModel;
	
	static{
		OntDocumentManager mgr = new OntDocumentManager();
		OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_DL_MEM);
		s.setDocumentManager(mgr);
		ontModel = ModelFactory.createOntologyModel(s, null);
		ontModel.setStrictMode(false);
	}
	
	public OntologyLoader(String filename){
		this.load(filename);
	}
	
	public void load(String filename){
		if(!filename.endsWith(".owl")){
			throw new IllegalArgumentException("The file passed as argument must be an owl file");
		}
		String ont = "";
		InputStream in = FileManager.get().open(filename);
		if (in == null)
			throw new IllegalArgumentException("File: " + filename
					+ " not found");
		// read the ontology file
		ontModel.removeAll();
		ontModel.read(in, "");
		ExtendedIterator<OntClass> it = ontModel.listHierarchyRootClasses();
		while (it.hasNext()) {
			OntClass c = it.next();
			LinkedList<OntClass> fifo = new LinkedList<>();
			fifo.add(c);
			while (!fifo.isEmpty()) {
				OntClass cl = fifo.remove();
				ont += (cl.getLocalName() + " ");
				ExtendedIterator<OntClass> it1 = cl.listSubClasses();
				while (it1.hasNext()){
					fifo.add(it1.next());
				}
			}
		}
		this.filename = filename;
		String ont1 = ont.replaceAll("([a-z])([A-Z])", "$1_$2").replace("_", " ");
		this.language = new LanguageDetection().detection(ont1);
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the language
	 */
	public Language getLanguage() {
		return language;
	}

	/**
	 * @return the ontModel
	 */
	public static OntModel getOntModel() {
		return ontModel;
	}
	
}
