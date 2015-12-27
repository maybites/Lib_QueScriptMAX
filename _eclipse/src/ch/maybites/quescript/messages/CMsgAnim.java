package ch.maybites.quescript.messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import com.cycling74.max.Atom;

public class CMsgAnim implements CMsgInterface{
	String ramp;
	
	public CMsgAnim(String _ramp){
		ramp = _ramp;
	}

	public boolean isTrigger(String _name) {
		return false;
	}

	public boolean isFade(String _name) {
		return false;
	}

	public boolean isAnim(String _name) {
		return (ramp.equals(_name)?true:false);
	}

	public boolean isStop() {
		return false;
	}

	public boolean isFade() {
		return false;
	}
}
