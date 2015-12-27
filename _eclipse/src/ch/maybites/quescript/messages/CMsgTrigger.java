package ch.maybites.quescript.messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import com.cycling74.max.Atom;

public class CMsgTrigger implements CMsgInterface{
	String trigger;
	Atom[] triggerVals;
	String[] triggerVariables;
	
	boolean isArmed = false;
	
	public CMsgTrigger(String _trigger, Atom[] _values){
		trigger = _trigger;
		triggerVals = _values;
	}
	
	public CMsgTrigger(String _trigger){
		trigger = _trigger;
		triggerVals = null;
	}
	
	public void setArmed(){
		isArmed = true;
	}
	
	public boolean isArmed(){
		return isArmed;
	}
	
	public void setTriggerVariables(String[] _triggerVariables){
		triggerVariables = _triggerVariables;
	}

	/**
	 * test if the passed string matches the trigger 
	 */
	public boolean isTrigger(String _name) {
		// first split the string into segments
		String[] segmts = _name.split("\\s+");
		if(segmts.length == 1){
			return (trigger.equals(segmts[0])?true:false);
		} else if(segmts.length > 1 && triggerVals != null){
			int isMatch = (trigger.equals(segmts[0]))? 1: 0;
			for(int i = 1; i < segmts.length; i++){
				if(triggerVals.length >= i){
					isMatch *= (triggerVals[i - 1].toString().equals(segmts[i]))? 1: 0;
				}
			}
			return (isMatch == 1)? true: false;
		}
		return false;
	}
	
	public boolean isFade(String _name) {
		return false;
	}

	public boolean isAnim(String _name) {
		return false;
	}

	public boolean isStop() {
		return false;
	}

	public boolean isFade() {
		return false;
	}
}
