package it.unige.dibris.moodtc;

import com.hp.hpl.jena.ontology.OntClass;

public class TCOntologyTreeNode {
	private OntClass c;

	public TCOntologyTreeNode(OntClass c) {
		this.c = c;
	}

	public OntClass getC() {
		return c;
	}

	public void setC(OntClass c) {
		this.c = c;
	}

	@Override
	public String toString() {
		return c.getLocalName();
	}

}
