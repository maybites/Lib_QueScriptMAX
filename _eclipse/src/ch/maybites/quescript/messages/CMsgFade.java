package ch.maybites.quescript.messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import com.cycling74.max.Atom;

import ch.maybites.quescript.expression.ExpressionVar;
import ch.maybites.tools.Debugger;

public class CMsgFade implements CMsgInterface{
	String rampName = null;
	CMsgTime time = null;
	
	boolean tagForDeletion = false;
	
	public CMsgFade(String _rampName, CMsgTime _timeString){
		rampName = _rampName;
		if(_timeString != null){
			try {
				time = _timeString;
				// a fade message of 0 time is causing troubles:
				// better set it to very short:
				if(time.getTotalMillis() == 0){
					time = new CMsgTime("1ms");
				}
			} catch (ScriptMsgException e) {
				Debugger.error("Script Parsing: Command Fade", e.getMessage());			
			}			
		}
	}
	
	public void tagForDeletion(){
		tagForDeletion = true;
	}
	
	public boolean isToBeDeleted(){
		return tagForDeletion;
	}

	public boolean isTrigger(String _name) {
		return false;
	}

	public boolean isFade(String _name) {
		return (rampName == null || rampName.equals(_name)?true:false);
	}

	public boolean isFade() {
		return true;
	}

	public boolean isAnim(String _name) {
		return false;
	}

	public boolean hasFadeTime(){
		return (time != null)?true: false;
	}

	public CMsgTime getFadeTime(){
		return time;
	}
	
	public boolean isStop() {
		return false;
	}
}
