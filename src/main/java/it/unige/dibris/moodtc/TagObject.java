package it.unige.dibris.moodtc;

import edu.mit.jwi.item.POS;

public class TagObject {
	private String textWord;
	private String lemmaWord;
	private POS pos = null;
	
	public TagObject(String word, String lemma, POS s) {
		this.textWord = word;
		this.lemmaWord = lemma;
		this.pos = s;
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

	public void setPOS(POS pos) {
		this.pos = pos;
	}	
}
