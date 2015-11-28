package it.unige.dibris.moodtc;

import java.util.List;

import javax.swing.AbstractListModel;

public class TCOutputModel<T> extends AbstractListModel<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3530551311319790618L;
	private List<T> objects;

	public TCOutputModel(List<T> objects) {
		this.objects = objects;
	}

	@Override
	public int getSize() {
		return objects.size();
	}

	@Override
	public T getElementAt(int index) {
		return objects.get(index);
	}
}
