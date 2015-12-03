package it.unige.dibris.moodtc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import it.uniroma1.lcl.jlt.util.Language;

public class TextLoader {
	
	private String filename;
	private Language language;
	private String text = "";
	
	public TextLoader(){
		
	}
	
	public TextLoader(String text) {
		this.text = text;
		this.language = new LanguageDetection().detection(this.text);
	}
	
	public TextLoader(File fileToRead) throws IOException{
		this.load(fileToRead);
	}

	public void load(String text) {
		this.filename = null;
		this.text = text;
		this.language = new LanguageDetection().detection(this.text);
	}
	
	public void load(File fileToRead) throws IOException {
		try(FileReader fr = new FileReader(fileToRead)){
		    try(BufferedReader br = new BufferedReader(fr)){
			    String line;
			    while((line = br.readLine()) != null){
			        this.text += line + "\n";
			    }
			    this.filename = fileToRead.getAbsolutePath();
			    this.language = new LanguageDetection().detection(this.text);
		    }
		} catch(FileNotFoundException e){
			throw new IllegalArgumentException("file:" + filename + " not found", e);
		}
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
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
}
