package it.unige.dibris.moodtc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

import it.unige.dibris.adm.ClassifierObject;
import it.unige.dibris.adm.TCOutput;
import it.unige.dibris.moodtc.utils.LanguageDetection;
import it.unige.dibris.moodtc.utils.OntologyLoader;
import it.unige.dibris.moodtc.utils.TCUtils;
import it.unige.dibris.moodtc.utils.TextLoader;
import it.unige.dibris.moodtc.utils.TreeTaggerUtils;
import it.unige.dibris.moodtc.utils.Exceptions.ClassifierStateException;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.jlt.util.Language;

public class TC {

	private OntologyLoader ontLoader;
	private TextLoader textLoader;
	private List<ClassifierObject> info;
	// should be loaded from a configuration file
	private Language supportedLanguages[] = new Language[] { Language.EN,
			Language.IT, Language.ES, Language.FR, Language.DE };

	public TC() {
		
	}
	
	public TC(String ontologyFileName, String textFileName) throws IOException{
		this.textLoader = new TextLoader(new File(textFileName));
		this.ontLoader = new OntologyLoader(ontologyFileName);
	}
	
	public void loadOntology(String ontologyFileName){
		this.ontLoader = new OntologyLoader(ontologyFileName);
	}
	
	public void loadTextFromFile(String filename) throws IOException{
		this.textLoader = new TextLoader(new File(filename));
	}
	
	public void loadText(String text){
		this.textLoader = new TextLoader(text);
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
	
	// update the info list with the new classifier object clssObj
	// if toAdd is true when we update an incomplete class, false otherwise
	private void updateInfo(ClassifierObject clssObj, List<ClassifierObject> info, boolean toAdd){
		boolean contains = false;
		for (int i = 0; i < info.size() && !contains; i++) {
			ClassifierObject o = info.get(i);
			if (o.getLemmaWord().split(" ").length == clssObj.getLemmaWord().split(" ").length &&
					Arrays.asList(o.getLemmaWord().split(" ")).containsAll(Arrays.asList(clssObj.getLemmaWord().split(" ")))
					&& (o.getPos() == null || (o.getPos().equals(clssObj.getPos())))) {
				if(toAdd) o.getTextWords().addAll(clssObj.getTextWords());
				else o.setTextWords(clssObj.getTextWords());
				contains = true;
			}
		}
		if (!contains) {
			info.add(clssObj);
		}
	}
	
	private void updateInfo(TagObject obj, String sense, OntClass c, List<ClassifierObject> info){
		boolean contains = false;
		for (int i = 0; i < info.size() && !contains; i++) {
			ClassifierObject o = info.get(i);
			if (o.getLemmaWord().split(" ").length == obj.getLemmaWord().split(" ").length &&
					Arrays.asList(o.getLemmaWord().split(" ")).containsAll(Arrays.asList(obj.getLemmaWord().split(" ")))
					&& (o.getPos() == null || (o.getPos().equals(obj.getPOS())))) {
				o.addTextWord(obj.getTextWord());
				contains = true;
			}
		}
		if (!contains) {
			ArrayList<ArrayList<String>> textWords = new ArrayList<>();
			ArrayList<String> word = new ArrayList<String>();
			word.add(obj.getTextWord());
			textWords.add(word);
			info.add(new ClassifierObject(textWords,
					obj.getLemmaWord(), sense, obj.getPOS(),
					getOntologyTree(c)));
		}
	}
	
	private OntClass findClasses(String sense, List<OntClass> incomplete){
		sense = sense.replaceAll("([a-z])([A-Z])", "$1_$2");
		List<String> senses = new ArrayList<String>(
				Arrays.asList(sense.split("_"))); // decompose sense in all its subterms
		
		ExtendedIterator<OntClass> it = OntologyLoader.getOntModel().listClasses();
		while (it.hasNext()) { // for each class in the ontology
			OntClass c = (OntClass) it.next();
			String name = c.getLocalName();
			ArrayList<String> names = null;
			if (name != null){
		        name = name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(); // replace camelCase with underscore
				names = new ArrayList<String>(
						Arrays.asList(name.split("_")));
			}
			if (names != null && names.containsAll(senses)) { // if the ontology class contains all the words contained in the sense
				if(names.size() == senses.size()){ // and only that
					return c;
				}
				else{ // otherwise is a spurious match
					incomplete.add(c);
				}
			}
		}
		return null;
	}
	
	private boolean addToComplete(ClassifierObject cls, TagObject obj, String sense, 
			List<ClassifierObject> completeClasses, List<OntClass> incomplete){
		String compoundOntologyWord = cls.getOntologyWord() + "_" + sense; // the new ontology word created by the composition
		String compoundLemmaWord = cls.getLemmaWord() + " " + obj.getLemmaWord(); // the new text word created by the composition
		OntClass complete = findClasses(compoundOntologyWord, incomplete);
		if(complete != null){ // if the compound word matches with an ontology class
			for( ArrayList<String> ws : cls.getTextWords()){
				ws.add(obj.getTextWord());
			}
			updateInfo(new ClassifierObject(cls.getTextWords(),
					compoundLemmaWord, compoundOntologyWord, null,
					getOntologyTree(complete)), completeClasses, true);
			return true;
		} // otherwise (however, we are interested in all partial results, like the incomplete classes)
		return false;
	}
	
	/*public TCOutput classificationOld() {
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
		String regex = "([a-z])([A-Z])"; //Used for passing from camelCase to underscore_
        String replacement = "$1_$2";
		boolean found = false;
	
		for (TagObject obj : tagObj) {
			found = false;
			// HashSet<String> lemmas = new HashSet<String>();
			try {
				List<String> senses = new ArrayList<>();
				for (BabelSynset syn : bn.getSynsets(textLang, obj.getLemmaWord(), obj.getPOS())) {
					for (BabelSense sen : syn.getSenses(ontLang)) {
						senses.add(sen.getLemma().toLowerCase());
					}
				}
				for(String sense : senses){
					if (!sense.contains("_")) {
						ExtendedIterator<OntClass> it = JenaUtils.ONTMODEL.listClasses();
						while (it.hasNext()) {
							OntClass c = (OntClass) it.next();
							String name = c.getLocalName();
							ArrayList<String> names = null;
							if (name != null){
						        name = name.replaceAll(regex, replacement);
								names = new ArrayList<String>(
										Arrays.asList(name.split("_")));
							}
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
					if(found) break;
				}				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		output.setInfo(info);
		return output;
	}*/
	
	private List<ClassifierObject> processingAPeriod(List<TagObject> tagObj){
		BabelNet bn = BabelNet.getInstance();
		boolean found = false;
		// list containing all classes which have been completed in this period
		List<ClassifierObject> completeClasses = new ArrayList<>();
		// list containing all classes which haven't been completed in this period (these could become complete)
		List<ClassifierObject> incompleteClasses = new ArrayList<>();
		
		// for each word of the text which has been tagged correctly
		for (TagObject obj : tagObj) {
			found = false;
			try {
				// unroll all senses of this tagged word
				List<String> senses = new ArrayList<>();
				
				List<BabelSynset> synsets = bn.getSynsets(textLoader.getLanguage(), obj.getLemmaWord(), obj.getPOS());
				if(synsets == null || synsets.isEmpty()){
					synsets = bn.getSynsets(textLoader.getLanguage(), obj.getLemmaWord());
					//senses.add(obj.getLemmaWord());
				}
				if(synsets == null || synsets.isEmpty()){
					Language lemmaLanguage = new LanguageDetection().detection(obj.getLemmaWord());
					if(lemmaLanguage != null) synsets = bn.getSynsets(lemmaLanguage, obj.getLemmaWord());
				}
				for (BabelSynset syn : synsets) {
					for (BabelSense sen : syn.getSenses(ontLoader.getLanguage())) {
						senses.add(sen.getLemma().toLowerCase());
					}
				}
				//if(textLoader.getLanguage() == ontLoader.getLanguage()){
					//senses.add(obj.getLemmaWord());
				//}
				
				// auxiliary list used to heap all incomplete classes which will found during the analysis
				List<ClassifierObject> incompleteClassesAux = new ArrayList<>();
				// auxiliary list necessary to remove the obsolete incomplete classes (an incomplete class 
				// becomes obsolete when a more complex incomplete class (with a superset lemma) has been found) 	
				List<Integer> incompleteClassesToRemove = new ArrayList<>();
				
				// INCOMPLETE ---> (IN)COMPLETE
				// block dedicated to complete the incomplete classes
				for(String sense : senses){
					if (!sense.contains("_")) {
						// for each incomplete class which has been found in this period until now
						for(int i = 0; i < incompleteClasses.size(); i++){
							ClassifierObject incCls = incompleteClasses.get(i);
							// try to complete an incomplete class 
							ArrayList<OntClass> incomplete = new ArrayList<>();
							if(addToComplete(incCls, obj, sense, completeClasses, incomplete)){
								incompleteClasses.remove(i); // the class now is complete, so we have to remove it from the incomplete classes list
								found = true; break;
							}
							// even if the incomplete class does not become complete using the current sense of the word,
							// we can update the list of incomplete classes with these new classes which are more complex
							// and refer to entries longer (lemma word)
							if(incomplete.size() != 0){
								ArrayList<ArrayList<String>> clone = new ArrayList<>();
								for(ArrayList<String> e : incCls.getTextWords()){ // add the new text word at the end of each word
									ArrayList<String> cloneDeep = new ArrayList<>();
									for(String s : e){
										cloneDeep.add(s);
									}
									cloneDeep.add(obj.getTextWord());
									clone.add(cloneDeep);
								}
								// add the new incomplete class to the auxiliary list of the new incomplete classes
								updateInfo(new ClassifierObject(clone,
										incCls.getLemmaWord() + " " + obj.getLemmaWord(), 
										incCls.getOntologyWord() + "_" + sense, null,
										null), incompleteClassesAux, false);
								if(!incompleteClassesToRemove.contains(i)) // update the classes which will be removed (since obsolete)
									incompleteClassesToRemove.add(i);
							}							
						}
						if(found) break;
					}
				}
				if(found) continue;
				
				// block dedicated to find a direct correspondence with a single term in the ontology
				for(String sense : senses){
					if (!sense.contains("_")) {
						List<OntClass> incomplete = new ArrayList<>();
						OntClass complete = findClasses(sense, incomplete);
						if(complete != null){ //if sense has a complete "correspondence" with an ontology class 
							updateInfo(obj, sense, complete, completeClasses);
							found = true; break;
						}
						// the sense hasn't a direct term in the ontology but it can have an incomplete term in the ontology 
						if(incomplete.size() != 0){
							ArrayList<ArrayList<String>> newTextWords = new ArrayList<>();
							ArrayList<String> word = new ArrayList<>();
							word.add(obj.getTextWord());
							newTextWords.add(word); // save the current text word
							updateInfo(new ClassifierObject(newTextWords,
									obj.getLemmaWord(), sense, obj.getPOS(),
									getOntologyTree(incomplete.get(0))), incompleteClassesAux, false);
						}
					}
				}
				if(found) continue;
				
				// COMPLETE ---> COMPLETE
				// block dedicated to complete a complete (make it more complex)
				for(String sense : senses){
					if (!sense.contains("_")) {
						for(int i = 0; i < completeClasses.size(); i++){
							ClassifierObject cCls = completeClasses.get(i);
							ArrayList<OntClass> incomplete = new ArrayList<>();
							// try to complete a complete class 
							if(addToComplete(cCls, obj, sense, completeClasses, incomplete)){
								completeClasses.remove(i);
								found = true; break;
							}
						}
						if(found) break;
					}
				}
				if(found) continue;
				
				Collections.sort(incompleteClassesToRemove, Comparator.reverseOrder());
				
				for(int i : incompleteClassesToRemove){
					incompleteClasses.remove(i);
				}
				for(ClassifierObject clssObj : incompleteClassesAux){
					updateInfo(clssObj, incompleteClasses, false);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return completeClasses;
	}
	
	public TCOutput classificationConcurrent() {
		if(textLoader == null){
			throw new ClassifierStateException("Load a text file in order to do the classification");
		}
		if(ontLoader == null){
			throw new ClassifierStateException("Load an ontology file in order to the classification");
		}
		
		TreeTaggerUtils.treeTagConfig(textLoader.getLanguage());
		TCOutput output = new TCOutput(textLoader.getLanguage(), ontLoader.getLanguage(), null);
		
		info = new ArrayList<ClassifierObject>();
		
		// Extrapolate token from text
		ArrayList<String> words = TCUtils.extractToken(textLoader.getFilename());
		ArrayList<TagObject> tagObjs = TreeTaggerUtils.tagToken(textLoader.getLanguage(), words);
		
		ArrayList<ArrayList<TagObject>> tagObjPeriods = new ArrayList<>();
		tagObjPeriods.add(new ArrayList<>());
		int index = 0;
		for(TagObject tagObj : tagObjs){
			if(tagObj.getTextWord().contains(".")) {
				index++;
				tagObjPeriods.add(new ArrayList<>());
			}
			else{
				tagObjPeriods.get(index).add(tagObj);
			}
		}
		
		ExecutorService threadpool = Executors.newCachedThreadPool();
		ArrayList<Future<List<ClassifierObject>>> results = new ArrayList<>();;
		for(ArrayList<TagObject> tagObj : tagObjPeriods){
			results.add(threadpool.submit(
					() -> 
					{
						return processingAPeriod(tagObj);	
					}));
		}
		
		try {
			for(Future<List<ClassifierObject>> res : results){
				List<ClassifierObject> completeClasses = res.get();
				for(ClassifierObject clssObj : completeClasses){
					updateInfo(clssObj, info, true);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		output.setInfo(info);
		return output;
	}
	
	public TCOutput classification() {
		if(textLoader == null){
			throw new ClassifierStateException("Load a text file in order to do the classification");
		}
		if(ontLoader == null){
			throw new ClassifierStateException("Load an ontology file in order to the classification");
		}
		
		TreeTaggerUtils.treeTagConfig(textLoader.getLanguage());
		TCOutput output = new TCOutput(textLoader.getLanguage(), ontLoader.getLanguage(), null);
		
		ArrayList<ClassifierObject> info = new ArrayList<ClassifierObject>();
		
		// Extrapolate token from text
		ArrayList<String> words = TCUtils.extractToken(textLoader.getFilename());
		ArrayList<TagObject> tagObjs = TreeTaggerUtils.tagToken(textLoader.getLanguage(), words);
		
		ArrayList<ArrayList<TagObject>> tagObjPeriods = new ArrayList<>();
		tagObjPeriods.add(new ArrayList<>());
		int index = 0;
		for(TagObject tagObj : tagObjs){
			if(tagObj.getTextWord().contains(".")) {
				index++;
				tagObjPeriods.add(new ArrayList<>());
			}
			else{
				tagObjPeriods.get(index).add(tagObj);
			}
		}
		
		for(ArrayList<TagObject> tagObj : tagObjPeriods){
			for(ClassifierObject clssObj : processingAPeriod(tagObj)){
				updateInfo(clssObj, info, true);
			}
		}
		
		output.setInfo(info);
		return output;
	}

	public Language[] getSupportedLanguages() {
		return supportedLanguages;
	}

	/**
	 * @return the ontLoader
	 */
	public OntologyLoader getOntLoader() {
		return ontLoader;
	}

	/**
	 * @return the textLoader
	 */
	public TextLoader getTextLoader() {
		return textLoader;
	}

	/**
	 * @param ontLoader the ontLoader to set
	 */
	public void setOntLoader(OntologyLoader ontLoader) {
		this.ontLoader = ontLoader;
	}

	/**
	 * @param textLoader the textLoader to set
	 */
	public void setTextLoader(TextLoader textLoader) {
		this.textLoader = textLoader;
	}

	public void setSupportedLanguages(Language[] supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}
}
