package it.unige.dibris.moodtc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import arq.tokens;
import edu.mit.jwi.item.POS;
import it.unige.dibris.adm.ClassifierObject;
import it.unige.dibris.adm.TCOutput;
import it.unige.dibris.moodtc.utils.LanguageDetection;
import it.unige.dibris.moodtc.utils.ModuleLoader;
import it.unige.dibris.moodtc.utils.OntologyLoader;
import it.unige.dibris.moodtc.utils.TCUtils;
import it.unige.dibris.moodtc.utils.TextLoader;
import it.unige.dibris.moodtc.utils.TreeTaggerUtils;
import it.unige.dibris.moodtc.utils.Exceptions.ClassifierStateException;
import it.uniroma1.lcl.babelfy.commons.BabelfyConstraints;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.MCS;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.MatchingType;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.ScoredCandidates;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.SemanticAnnotationResource;
import it.uniroma1.lcl.babelfy.commons.BabelfyToken;
import it.uniroma1.lcl.babelfy.commons.PosTag;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.commons.annotation.TokenOffsetFragment;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetIDRelation;
import it.uniroma1.lcl.babelnet.data.BabelDomain;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;

public class TC {

	private OntologyLoader ontLoader;
	private TextLoader textLoader;
	private List<ClassifierObject> info;
	// should be loaded from a configuration file
	private Language supportedLanguages[] = new Language[] { Language.EN,
			Language.IT, Language.ES, Language.FR, Language.DE };
	private LanguageDetection detection = new LanguageDetection();
	
	public TC() {
		
	}
	
	public TC(String ontologyFileName, String textFileName) throws IOException{
		this.textLoader = new TextLoader(new File(textFileName));
		this.ontLoader = new OntologyLoader(ontologyFileName);
	}
	
	public TC(String ontologyFileName, String textFileName, String moduleFileName) throws IOException{
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
	
	public static interface I{
		public boolean equals(Object o);
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
		/*
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

		String queryString = //"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
		        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
				//"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
		        //"PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
				//"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
		        //"PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#> \n" +
		        "select ?uri \n"+
		        //"where { \n ?uri rdfs:label ?name . \n";
		        "where { \n ?uri rdf:type ?x . \n";
		for(String s : senses){
			Matcher m = p.matcher(s);
			if(!m.find())
				queryString += "filter( regex(str(?uri), \"" + s + "\" ))\n";
		}
		queryString += "} \n ";
		//System.out.println(queryString);
		
		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL); 
				
		QueryExecution qe = QueryExecutionFactory.create(query, OntologyLoader.getOntModel());
		ResultSet results = qe.execSelect();
		//qe.close();
		
		while(results.hasNext()){
			QuerySolution sol = results.nextSolution();
			//String name = sol.getLiteral("name").getString();
			String uri = sol.get("uri").toString();
			int l_index = uri.length() - 1;
			while(l_index >= 0 && 
					(Character.isLetter(uri.charAt(l_index)) || 
							uri.charAt(l_index) == '_' || 
							uri.charAt(l_index) == ' ')) l_index--;
			String name = uri.substring(l_index+1, uri.length());
			
			ArrayList<String> names = null;
			if (name != null){
		        name = name.replaceAll("([a-z])([A-Z])", "$1_$2").replace(" ", "_").toLowerCase(); // replace camelCase with underscore
				names = new ArrayList<String>(
						Arrays.asList(name.split("_")));
			//}
				if (names != null && names.containsAll(senses)) { // if the ontology class contains all the words contained in the sense
					OntClass c = OntologyLoader.getOntModel().getOntClass(uri);
					if(names.size() == senses.size()){ // and only that
						return c;
					}
					else{ // otherwise is a spurious match
						incomplete.add(c);
					}
				}
			}
		}
		*/
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
			List<ClassifierObject> completeClasses, List<OntClass> incomplete, BabelNet bn) throws IOException{
		// unroll all senses of this tagged word
		Collection<Language> filter = new ArrayList<>();
		filter.add(ontLoader.getLanguage());
		List<String> compoundOntologyWords = new ArrayList<>();
		for(String lemmaWord : cls.getLemmaWord().split(" ")){
			List<BabelSynset> synsets = bn.getSynsets(lemmaWord, textLoader.getLanguage(), filter);//, cls.getPos());
			if(synsets == null || synsets.isEmpty()){
				synsets = bn.getSynsets(lemmaWord, textLoader.getLanguage(), filter);
				//senses.add(obj.getLemmaWord());
			}
			if(synsets == null || synsets.isEmpty()){
				synchronized (detection) {
					Language lemmaLanguage = detection.detection(lemmaWord);
					if(lemmaLanguage != null) 
						synsets = bn.getSynsets(lemmaWord, lemmaLanguage, filter);
				}
			}
			
			boolean interesting = false;
			for(BabelSynset syn : synsets){
				if(obj.getPOS() == POS.ADJECTIVE || obj.getPOS() == POS.ADVERB || obj.getPOS() == POS.VERB || syn.getDomains().containsKey(BabelDomain.HEALTH_AND_MEDICINE) 
						|| syn.getDomains().containsKey(BabelDomain.BIOLOGY)){
					interesting = true;
					break;
				}
			}
			
			if(interesting){
				for (BabelSynset syn : synsets) {
					for (BabelSense sen : syn.getSenses(ontLoader.getLanguage())) {
						if(!sen.getLemma().contains("_"))
							//senses.add(sen.getLemma().toLowerCase());
							compoundOntologyWords.add(sen.getLemma().toLowerCase() + "_" + sense);
					}
				}
			}
		}
		
		//String compoundOntologyWord = cls.getOntologyWord() + "_" + sense; // the new ontology word created by the composition
		String compoundLemmaWord = cls.getLemmaWord() + " " + obj.getLemmaWord(); // the new text word created by the composition
		for(String compoundOntologyWord : compoundOntologyWords){
			//System.out.println(compoundOntologyWord);
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
		}
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
	
	public TCOutput classificationWithBabelfy(){
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
		
		ArrayList<ArrayList<BabelfyToken>> tagBabObjPeriods = new ArrayList<>();
		tagBabObjPeriods.add(new ArrayList<>());
		int index = 0;
		for(TagObject tagObj : tagObjs){
			if(tagObj.getTextWord().contains(".")) {
				index++;
				tagBabObjPeriods.add(new ArrayList<>());
			}
			else{
				PosTag posTag;
				switch(tagObj.getPOS()){
					case NOUN: posTag = PosTag.NOUN; break;
					case ADJECTIVE: posTag = PosTag.ADJECTIVE; break;
					case ADVERB: posTag = PosTag.ADVERB; break;
					default: posTag = PosTag.VERB;
				}
				tagBabObjPeriods.get(index).add(new BabelfyToken(tagObj.getTextWord(), tagObj.getLemmaWord(), posTag, textLoader.getLanguage()));
			}
		}
		
		//BabelfyConstraints constraints = new BabelfyConstraints();
		BabelfyParameters bp = new BabelfyParameters();
		//bp.setAnnotationResource(SemanticAnnotationResource.BN);
		bp.setMCS(MCS.ON_WITH_STOPWORDS);
		bp.setScoredCandidates(ScoredCandidates.ALL);
		bp.setMatchingType(MatchingType.PARTIAL_MATCHING);
		Babelfy bfy = new Babelfy(bp);
		
		for(ArrayList<BabelfyToken> tagObj : tagBabObjPeriods){ 
			for(ClassifierObject clssObj : processingAPeriodWithBabelfy(tagObj, bfy.babelfy(tagObj, textLoader.getLanguage()))){
				updateInfo(clssObj, info, true);
			}
		}
		
		output.setInfo(info);
		return output;
	}
	
	public TCOutput classificationWithBabelfyConcurrent(){
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
		
		ArrayList<ArrayList<BabelfyToken>> tagBabObjPeriods = new ArrayList<>();
		tagBabObjPeriods.add(new ArrayList<>());
		int index = 0;
		for(TagObject tagObj : tagObjs){
			if(tagObj.getTextWord().contains(".")) {
				index++;
				tagBabObjPeriods.add(new ArrayList<>());
			}
			else{
				PosTag posTag;
				switch(tagObj.getPOS()){
					case NOUN: posTag = PosTag.NOUN; break;
					case ADJECTIVE: posTag = PosTag.ADJECTIVE; break;
					case ADVERB: posTag = PosTag.ADVERB; break;
					default: posTag = PosTag.VERB;
				}
				tagBabObjPeriods.get(index).add(new BabelfyToken(tagObj.getTextWord(), tagObj.getLemmaWord(), posTag, textLoader.getLanguage()));
			}
		}
		
		//BabelfyConstraints constraints = new BabelfyConstraints();
		BabelfyParameters bp = new BabelfyParameters();
		//bp.setAnnotationResource(SemanticAnnotationResource.BN);
		bp.setMCS(MCS.ON_WITH_STOPWORDS);
		bp.setScoredCandidates(ScoredCandidates.TOP);
		bp.setMatchingType(MatchingType.PARTIAL_MATCHING);
		Babelfy bfy = new Babelfy(bp);
		
		for(ArrayList<BabelfyToken> tagObj : tagBabObjPeriods){ 
			for(ClassifierObject clssObj : processingAPeriodWithBabelfy(tagObj, bfy.babelfy(tagObj, textLoader.getLanguage()))){
				updateInfo(clssObj, info, true);
			}
		}
		
		ExecutorService threadpool = Executors.newCachedThreadPool();
		ArrayList<Future<List<ClassifierObject>>> results = new ArrayList<>();;
		for(ArrayList<BabelfyToken> tagObj : tagBabObjPeriods){
			results.add(threadpool.submit(
					() -> 
					{
						return processingAPeriodWithBabelfy(tagObj, bfy.babelfy(tagObj, textLoader.getLanguage()));	
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
	
	private List<ClassifierObject> processingAPeriodWithBabelfy(List<BabelfyToken> tokens, List<SemanticAnnotation> bfyAnnotations){
		BabelNet bn = BabelNet.getInstance();
		boolean found = false;
		// list containing all classes which have been completed in this period
		List<ClassifierObject> completeClasses = new ArrayList<>();
		// list containing all classes which haven't been completed in this period (these could become complete)
		List<ClassifierObject> incompleteClasses = new ArrayList<>();
		Collection<Language> filter = new ArrayList<>();
		filter.add(ontLoader.getLanguage());
		
		//List<List<String>> senses = new ArrayList<>();
		HashMap<TokenOffsetFragment, List<String>> map = new HashMap<>();
		
		for(SemanticAnnotation annotation : bfyAnnotations){
			BabelSynset synset = bn.getSynset(filter, new BabelSynsetID(annotation.getBabelSynsetID()));
			List<String> aux = new ArrayList<>();
			if(synset.getPOS() == BabelPOS.ADJECTIVE || synset.getPOS() == BabelPOS.VERB || synset.getPOS() == BabelPOS.ADVERB || synset.getDomains().containsKey(BabelDomain.HEALTH_AND_MEDICINE) 
					|| synset.getDomains().containsKey(BabelDomain.BIOLOGY)){
				for (BabelSense sen : synset.getSenses(ontLoader.getLanguage())) {
					aux.add(sen.getLemma().toLowerCase());
				}
				/*for(BabelSynsetIDRelation synRel : synset.getEdges(BabelPointer.SEMANTICALLY_RELATED)){
					for (BabelSense sen : bn.getSynset(synRel.getBabelSynsetIDTarget()).getSenses(ontLoader.getLanguage())){
						aux.add(sen.getLemma().toLowerCase());
					}
				}*/
			}
			if(!aux.isEmpty()){
				if(map.containsKey(annotation.getTokenOffsetFragment())){
					map.get(annotation.getTokenOffsetFragment()).addAll(aux);
				}
				else{
					map.put(annotation.getTokenOffsetFragment(), aux);
				}
			}
		}
		
		
		for(TokenOffsetFragment tokenKey : map.keySet()){
			try {
				//String frag = textLoader.getText().substring(annotation.getCharOffsetFragment().getStart(),
					//	annotation.getCharOffsetFragment().getEnd() + 1);
				
				BabelfyToken token = tokens.get(tokenKey.getStart());
				System.out.println(token);
				found = false;
				// unroll all senses of this tagged word
				
				TagObject obj = new TagObject(token.getWord(), token.getLemma(), token.getPosTag());
				// auxiliary list used to heap all incomplete classes which will found during the analysis
				List<ClassifierObject> incompleteClassesAux = new ArrayList<>();
				// auxiliary list necessary to remove the obsolete incomplete classes (an incomplete class 
				// becomes obsolete when a more complex incomplete class (with a superset lemma) has been found) 	
				List<Integer> incompleteClassesToRemove = new ArrayList<>();
				
				// INCOMPLETE ---> (IN)COMPLETE
				// block dedicated to complete the incomplete classes
				for(String sense : map.get(tokenKey)){
					if (!sense.contains("_")) {
						// for each incomplete class which has been found in this period until now
						for(int i = 0; i < incompleteClasses.size(); i++){
							ClassifierObject incCls = incompleteClasses.get(i);
							// try to complete an incomplete class 
							ArrayList<OntClass> incomplete = new ArrayList<>();
							if(addToComplete(incCls, obj, sense, completeClasses, incomplete, bn)){
								incompleteClasses.remove(i); // the class now is complete, so we have to remove it from the incomplete classes list
								found = true; break;
							}
							// even if the incomplete class does not become complete using the current sense of the word,
							// we can update the list of incomplete classes with these new classes which are more complex
							// and refer to longer entries (lemma word)
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
				
				// COMPLETE ---> COMPLETE
				// block dedicated to complete a complete (make it more complex)
				for(String sense : map.get(tokenKey)){
					if (!sense.contains("_")) {
						for(int i = 0; i < completeClasses.size(); i++){
							ClassifierObject cCls = completeClasses.get(i);
							ArrayList<OntClass> incomplete = new ArrayList<>();
							// try to complete a complete class 
							if(addToComplete(cCls, obj, sense, completeClasses, incomplete, bn)){
								completeClasses.remove(i);
								found = true; break;
							}
						}
						if(found) break;
					}
				}
				if(found) continue;
				
				// block dedicated to find a direct correspondence with a single term in the ontology
				for(String sense : map.get(tokenKey)){
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
		
	private List<ClassifierObject> processingAPeriod(List<TagObject> tagObj){
		BabelNet bn = BabelNet.getInstance();
		boolean found = false;
		// list containing all classes which have been completed in this period
		List<ClassifierObject> completeClasses = new ArrayList<>();
		// list containing all classes which haven't been completed in this period (these could become complete)
		List<ClassifierObject> incompleteClasses = new ArrayList<>();
		
		// for each word of the text which has been tagged correctly
		for (TagObject obj : tagObj) {
			System.out.println("I'm processing " + obj.getLemmaWord());
			found = false;
			try {
				// unroll all senses of this tagged word
				List<String> senses = new ArrayList<>();
				Collection<Language> filter = new ArrayList<>();
				filter.add(ontLoader.getLanguage());
				List<BabelSynset> synsets = bn.getSynsets(obj.getLemmaWord(), textLoader.getLanguage(), obj.getBabelPOS(), filter);
				
				if(synsets == null || synsets.isEmpty()){
					synsets = bn.getSynsets(obj.getLemmaWord(), textLoader.getLanguage(), filter);
					//senses.add(obj.getLemmaWord());
				}
				if(synsets == null || synsets.isEmpty()){
					synchronized (detection) {
						Language lemmaLanguage = detection.detection(obj.getLemmaWord());
						if(lemmaLanguage != null) 
							synsets = bn.getSynsets(lemmaLanguage, obj.getLemmaWord());
					}
				}
				
				boolean interesting = false;
				for(BabelSynset syn : synsets){
					if(obj.getPOS() == POS.ADJECTIVE || syn.getDomains().containsKey(BabelDomain.HEALTH_AND_MEDICINE) 
							|| syn.getDomains().containsKey(BabelDomain.BIOLOGY)){
						interesting = true;
						break;
					}
				}
				
				if(interesting){
					for (BabelSynset syn : synsets) {
						for (BabelSense sen : syn.getSenses(ontLoader.getLanguage())) {
							senses.add(sen.getLemma().toLowerCase());
						}
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
							if(addToComplete(incCls, obj, sense, completeClasses, incomplete, bn)){
								incompleteClasses.remove(i); // the class now is complete, so we have to remove it from the incomplete classes list
								found = true; break;
							}
							// even if the incomplete class does not become complete using the current sense of the word,
							// we can update the list of incomplete classes with these new classes which are more complex
							// and refer to longer entries (lemma word)
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
				
				// COMPLETE ---> COMPLETE
				// block dedicated to complete a complete (make it more complex)
				for(String sense : senses){
					if (!sense.contains("_")) {
						for(int i = 0; i < completeClasses.size(); i++){
							ClassifierObject cCls = completeClasses.get(i);
							ArrayList<OntClass> incomplete = new ArrayList<>();
							// try to complete a complete class 
							if(addToComplete(cCls, obj, sense, completeClasses, incomplete, bn)){
								completeClasses.remove(i);
								found = true; break;
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
			throw new ClassifierStateException("Load an ontology file in order to do the classification");
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
