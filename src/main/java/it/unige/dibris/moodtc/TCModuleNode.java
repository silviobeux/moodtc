package it.unige.dibris.moodtc;

import adm.TCModule;


public class TCModuleNode {
	private TCModule module;
	
	public TCModuleNode(TCModule module){
		this.module = module;
	}

	@Override
	public String toString() {
		final String nomodule = "No module";
		if (module == null)
			return nomodule;
		return module.getClass().getSimpleName();
	}

	public TCModule getModule() {
		return module;
	}
}
