package ch.maybites.mxj.utils;

import com.cycling74.max.Atom;

public class Atomizer {
	
	private Atom[] myArray;
	
	public Atomizer(){
		clear();
	}
	
	public void clear(){
		myArray = new Atom[0];
	}
	
	public Atom[] getArray(){
		return myArray;
	}
	
	public Atomizer append(Atom atom){
		Atom[] newArray = new Atom[myArray.length + 1];
		for(int i = 0; i < myArray.length; i++)
			newArray[i] = myArray[i];
		newArray[newArray.length -1] = atom;
		myArray = newArray;
		return this;
	}
	
	public Atomizer prepend(Atom atom){
		Atom[] newArray = new Atom[myArray.length + 1];
		for(int i = 0; i < myArray.length; i++)
			newArray[i + 1] = myArray[i];
		newArray[0] = atom;
		myArray = newArray;
		return this;
	}

}
