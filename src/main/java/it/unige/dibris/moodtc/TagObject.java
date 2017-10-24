package it.unige.dibris.moodtc;

import edu.mit.jwi.item.POS;
import it.uniroma1.lcl.babelfy.commons.PosTag;
import it.uniroma1.lcl.babelnet.data.BabelPOS;

public class TagObject {
	private String textWord;
	private String lemmaWord;
	private POS pos = null;
	
	public TagObject(String word, String lemma, POS s) {
		this.textWord = word;
		this.lemmaWord = lemma;
		this.pos = s;
	}
	
	public TagObject(String word, String lemma, PosTag s) {
		this.textWord = word;
		this.lemmaWord = lemma;
		switch(s){
			case NOUN: pos = POS.NOUN; break;
			case ADJECTIVE: pos = POS.ADJECTIVE; break;
			case ADVERB: pos = POS.ADVERB; break;
			default: pos = POS.VERB;
		}
	}

	public String getLemmaWord() {
		return lemmaWord;
	}

	public void setLemmaWord(String lemma) {
		this.lemmaWord = lemma;
	}

	public String getTextWord() {
		return textWord;
	}

	public void setTextWord(String word) {
		this.textWord = word;
	}

	public POS getPOS() {
		return pos;
	}
	
	public BabelPOS getBabelPOS(){
		if(pos == POS.NOUN) return BabelPOS.NOUN;
		else if(pos == POS.ADJECTIVE) return BabelPOS.ADJECTIVE;
		else if(pos == POS.ADVERB) return BabelPOS.ADVERB;
		else return BabelPOS.VERB;
	}

	public void setPOS(POS pos) {
		this.pos = pos;
	}	
}
