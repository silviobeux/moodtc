package it.unige.dibris.moodtc.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma1.lcl.jlt.util.Language;

public class LanguageDetection {

	private Detector detector;
	private final String dirname = "profiles.sm/";

	public LanguageDetection() {
		DetectorFactory.clear();
		try {
			Enumeration<URL> en = Detector.class.getClassLoader().getResources(
					dirname);
			List<String> profiles = new ArrayList<>();
			if (en.hasMoreElements()) {
				URL url = en.nextElement();
				System.err.println(url.toString());
				JarURLConnection urlcon = (JarURLConnection) url
						.openConnection();
				JarFile jar = urlcon.getJarFile();
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					String entry = entries.nextElement().getName();
					if (entry.startsWith(dirname)) {
						try (InputStream in = Detector.class.getClassLoader()
								.getResourceAsStream(entry);) {
							profiles.add(IOUtils.toString(in));
						}
					}
				}
			}
			DetectorFactory.loadProfile(profiles);
			detector = DetectorFactory.create();
		} catch (LangDetectException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Language detection(String text) {
		detector.append(text);
		try {
			return Language.valueOf(detector.detect().toUpperCase());
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}

	public Language detection(File filename) throws FileNotFoundException {
		Scanner sc = new Scanner(filename);
		final String text = sc.useDelimiter("\\Z").next();
		sc.close();
		return detection(text);
	}
}
