package it.unige.dibris.moodtc.utils;

import it.unige.dibris.moodtc.TagObject;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

		// Please uncomment the follow lines for the jar release
		//File jarPath = new File(TreeTaggerUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		//currentJarPath=jarPath.getParentFile().getAbsolutePath() + "/";

		String convFilename = currentJarPath + "conversions/";
		convFilename += textLanguage.toString().toLowerCase() + ".txt";
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
		System.setProperty("treetagger.home", currentJarPath + "treetagger/");
	}

	public static ArrayList<TagObject> tagToken(Language textLanguage,
			ArrayList<String> words) {
		final ArrayList<TagObject> tagObj = new ArrayList<TagObject>();
		TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
		try {
			tt.setModel(textLanguage.toString().toLowerCase() + "-utf8.par");
			tt.setHandler(new TokenHandler<String>() {
				public void token(String token, String pos, String lemma) {
					if(token.equals(".") || token.equals(";") || 
							token.equals(":") || token.equals("?") ||
							token.equals("!")){
						tagObj.add(new TagObject(token, null, (POS)null));
						return;
					}
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
