package it.unige.dibris.moodtc.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import it.unige.dibris.adm.TCModule;
import it.unige.dibris.moodtc.ModuleLoad;
import it.unige.dibris.moodtc.TCModuleNode;

public class ModuleLoader {
	private String filename;
	private TCModule module;

	public ModuleLoader(String filename) {
		this.filename = filename;
		load();
	}
	
	private void load(){
		File file = new File(filename);
		// String textFile = selectedFile.getPath();
		URL downloadURL;
		try {
			downloadURL = file.toURI().toURL();
			URL[] downloadURLs = new URL[] { downloadURL };
			URLClassLoader loader = URLClassLoader.newInstance(
					downloadURLs, getClass().getClassLoader());
			try {
				List<Class<?>> implementingClasses = ModuleLoad
						.findImplementingClassesInJarFile(file,
								TCModule.class, loader);
				boolean first = true;
				for (Class<?> clazz : implementingClasses) {
					// System.out.println(clazz.getName());
					// assume there is a public default constructor
					// available
					module = (TCModule) clazz.newInstance();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public TCModule getModule() {
		// TODO Auto-generated method stub
		return module;
	}
}
