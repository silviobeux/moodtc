package it.unige.dibris.moodtc.utils;

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
}
