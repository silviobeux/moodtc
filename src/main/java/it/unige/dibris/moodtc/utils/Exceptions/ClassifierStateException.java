package it.unige.dibris.moodtc.utils.Exceptions;

public class ClassifierStateException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClassifierStateException(){
		
	}
	
	public ClassifierStateException(String message){
		super(message);
	}
	
	public ClassifierStateException(String message, Exception e){
		super(message, e);
	}
}
