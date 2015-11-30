package it.unige.dibris.moodtc.utils;

import it.uniroma1.lcl.jlt.util.Language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TCUtils {

	public static ArrayList<String> extractToken(String pathFile) {
		ArrayList<String> words = new ArrayList<String>();
		Scanner input;
		try {
			input = new Scanner(new File(pathFile));
			while (input.hasNext()) {
				for (String s : input.next().split("[.!,(')?;:]"))
					if (s.length() > 0)
						words.add(s.toLowerCase());
			}
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return words;
	}
	
	public static Language[] extractSupportedLanguages(){
		String currentJarPath = "";
		//File jarPath=new File(TreeTaggerUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		//currentJarPath=jarPath.getParentFile().getAbsolutePath() + "/";
		File conversionFolder = new File(currentJarPath + "conversions/");
		File[] listOfFiles = conversionFolder.listFiles();	
		Language[] supportedLanguages = new Language[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
		    if (listOfFiles[i].isFile()) {
		    	supportedLanguages[i] = Language.valueOf(listOfFiles[i].getName().substring(0, 2).toUpperCase());
		    }
		}
		return supportedLanguages;
	}
}
