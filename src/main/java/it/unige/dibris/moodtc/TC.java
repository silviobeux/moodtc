package it.unige.dibris.moodtc;

import it.unige.dibris.adm.ClassifierObject;
import it.unige.dibris.adm.TCOutput;
import it.unige.dibris.moodtc.utils.JenaUtils;
import it.unige.dibris.moodtc.utils.TCUtils;
import it.unige.dibris.moodtc.utils.TreeTaggerUtils;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TC {

	private Language textLang = Language.EN;
	private Language ontLang = Language.EN;
	private String textFileName = null;
	private String ontFileName = null;

	private Language supportedLanguages[] = TCUtils.extractSupportedLanguages();

	public TC() {
		JenaUtils.jenaConfig();
	}

	public TC(Language textLang, Language ontLang, String textFileName,
			String ontFileName) {
		this.textLang = textLang;
		this.ontLang = ontLang;
		this.textFileName = textFileName;
		this.ontFileName = ontFileName;
		JenaUtils.jenaConfig();
	}

	public ArrayList<String> getOntologyTree(OntClass c) {
		ArrayList<String> tree = new ArrayList<String>();
		while (c.getSuperClass() != null) {
			tree.add(c.getLocalName());
			c = c.getSuperClass();
		}
		tree.add(c.getLocalName());
		Collections.reverse(tree);
		return tree;
	}

	public TCOutput classification() {
		TreeTaggerUtils.treeTagConfig(textLang);
		TCOutput output = new TCOutput(textLang, ontLang, null);
		ArrayList<ClassifierObject> info = new ArrayList<ClassifierObject>();
		// use the FileManager to open the ontology from the filesystem
		InputStream in = FileManager.get().open(this.ontFileName);
		if (in == null)
			throw new IllegalArgumentException("File: " + this.ontFileName
					+ " not found");
		// read the ontology file
		JenaUtils.ONTMODEL.read(in, "");
		// Extrapolate token from text
		ArrayList<String> words = TCUtils.extractToken(this.textFileName);
		ArrayList<TagObject> tagObj = TreeTaggerUtils.tagToken(textLang, words);
		BabelNet bn = BabelNet.getInstance();
		boolean found = false;
		for (TagObject obj : tagObj) {
			found = false;
			// HashSet<String> lemmas = new HashSet<String>();
			try {
				for (BabelSynset syn : bn.getSynsets(textLang, obj.getLemmaWord(),
						obj.getPOS())) {
					for (BabelSense sen : syn.getSenses(ontLang)) {
						String sense = sen.getLemma().toLowerCase();
						if (!sense.contains("_")) {
							ExtendedIterator<OntClass> it = JenaUtils.ONTMODEL.listClasses();
							while (it.hasNext()) {
								OntClass c = (OntClass) it.next();
								String name = c.getLocalName();
								ArrayList<String> names = null;
								if (name != null)
									names = new ArrayList<String>(
											Arrays.asList(name.split("_")));
								if (names != null && names.contains(sense)) {
									boolean contains = false;
									for (int i = 0; i < info.size() && !contains; i++) {
										ClassifierObject o = info.get(i);
										if (o.getLemmaWord().equals(
												obj.getLemmaWord())
												&& o.getPos().equals(obj.getPOS())) {
											o.addTextWord(obj.getTextWord());
											contains = true;
										}
									}
									if (!contains) {
										List<String> textWords = new ArrayList<String>();
										textWords.add(obj.getTextWord());
										info.add(new ClassifierObject(textWords,
												obj.getLemmaWord(), sense, obj
														.getPOS(),
												getOntologyTree(c)));
									}
									found = true;
									break;
								}
							}
						}
						if (found)
							break;
					}
					if (found)
						break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		output.setInfo(info);
		return output;
	}

	public Language getTextLang() {
		return textLang;
	}

	public void setTextLang(Language textLang) {
		this.textLang = textLang;
	}

	public Language getOntLang() {
		return ontLang;
	}

	public void setOntLang(Language ontLang) {
		this.ontLang = ontLang;
	}

	public String getTextFileName() {
		return textFileName;
	}

	public void setTextFileName(String textFileName) {
		this.textFileName = textFileName;
	}

	public String getOntFileName() {
		return ontFileName;
	}

	public void setOntFileName(String ontFileName) {
		this.ontFileName = ontFileName;
	}

	public Language[] getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setSupportedLanguages(Language[] supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}
}
