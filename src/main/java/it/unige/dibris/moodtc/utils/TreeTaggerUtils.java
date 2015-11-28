package it.unige.dibris.moodtc.utils;

import it.unige.dibris.moodtc.TagObject;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import edu.mit.jwi.item.POS;

public class TreeTaggerUtils {

	private static HashMap<String, String> correspondences = new HashMap<>();

	public static void treeTagConfig(Language textLanguage) {
		String currentJarPath = "";
		try {
			currentJarPath = TreeTaggerUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			currentJarPath = currentJarPath.substring(0, currentJarPath.lastIndexOf("/"));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String convFilename = currentJarPath + "/conversions/";
		switch (textLanguage) {
		case EN:
			convFilename += "en.txt";
			break;
		case ES:
			convFilename += "es.txt";
			break;
		case DE:
			convFilename += "de.txt";
			break;
		case FR:
			convFilename += "fr.txt";
			break;
		case IT:
			convFilename += "it.txt";
			break;
		default:
			break;
		}
		Scanner input;
		try {
			input = new Scanner(new File(convFilename));
			while (input.hasNext()) {
				correspondences.put(input.next(), input.next());
			}
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		// TreeTagger Setting
		System.setProperty("treetagger.home", currentJarPath + "/../treetagger/");
	}

	public static ArrayList<TagObject> tagToken(Language textLanguage,
			ArrayList<String> words) {
		final ArrayList<TagObject> tagObj = new ArrayList<TagObject>();
		TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
		try {
			switch (textLanguage) {
			case EN:
				tt.setModel("/english-utf8.par");
				break;
			case ES:
				tt.setModel("/spanish-utf8.par");
				break;
			case DE:
				tt.setModel("/german-utf8.par");
				break;
			case FR:
				tt.setModel("/french-utf8.par");
				break;
			case IT:
				tt.setModel("/italian-utf8.par");
				break;
			default:
				break;
			}

			tt.setHandler(new TokenHandler<String>() {
				public void token(String token, String pos, String lemma) {
					String val = correspondences.get(pos.toLowerCase());
					if (val != null) {
						switch (val) {
						case "n":
							tagObj.add(new TagObject(token, lemma, POS.NOUN));
							break;
						case "a":
							tagObj.add(new TagObject(token, lemma,
									POS.ADJECTIVE));
							break;
						case "r":
							tagObj.add(new TagObject(token, lemma, POS.ADVERB));
							break;
						case "v":
							tagObj.add(new TagObject(token, lemma, POS.VERB));
							break;
						default:
							break;
						}
					}
				}
			});
			tt.process(words);
		} catch (IOException | TreeTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tt.destroy();
		}
		return tagObj;
	}
}
