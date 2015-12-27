package ch.maybites.quescript.commands;

import java.util.ArrayList;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.quescript.expression.ExpressionVar;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndDOUBLE extends Cmnd {
	private static String NODE_NAME = "float";

	private static String ATTR_FADETO = "fadeto";

	private double[] keyValues;
	private double[] relKeyTimes;
	private double fadeTo;
	private double fadeFrom;
	
	public String keyName;
	
	private ExpressionVar calculatedValue;
	
	boolean fadeToMode = false;
	
	String floatName;
		
	public CmndDOUBLE(Cmnd _parentNode, String _cmndName){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
		floatName = _cmndName;
		calculatedValue = new ExpressionVar(0);
	}
	
	public void build(Node _xmlNode) throws ScriptMsgException{
		super.build(_xmlNode);
				
		keyName = "$"+ floatName;
				
		parse(super.content);		

		// use the attribute or the first value of the key
		if(this.hasAttributeValue(ATTR_FADETO))
			fadeTo = Float.parseFloat(getAttributeValue(ATTR_FADETO));
		else
			fadeTo = keyValues[0];

		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created "+cmdName+" Comnd");	
	}
		
	/**
	 * sets the fade to mode.
	 * @param _mode false is normal mode, true is fade to mode
	 */
	public void fadeToMode(boolean _mode){
		fadeToMode = _mode;
		fadeFrom = calculatedValue.getNumberValue();
	}
	
	/**
	 * calculate the float value at the specified time
	 * @param _time normalized time 
	 */
	public void calculate(float _time){
		if(fadeToMode){
			calculatedValue.setValue((fadeTo - fadeFrom) * _time + fadeFrom);
//			Debugger.verbose("Script FLOAT - Command", "fade _time =" + _time);
			return;
		} else {
			for(int i = 0; i < relKeyTimes.length - 1; i++){
				if(relKeyTimes[i] <= _time && _time < relKeyTimes[i+1]){
					double diffTime = relKeyTimes[i+1] - relKeyTimes[i];
					double relTime = (_time - relKeyTimes[i]) / diffTime;
					calculatedValue.setValue((keyValues[i + 1] - keyValues[i]) * relTime + keyValues[i]);
					return;
				}
			}
			calculatedValue.setValue(keyValues[keyValues.length -1]);
		}
	}
	
	/**
	 * get the value of the result of the calculate method
	 * @return float value
	 */
	public double getValue(){
		return calculatedValue.getNumberValue();
	}
	
	/**
	 * get the object container for the calculated value;
	 * @return
	 */
	public ExpressionVar getValueObject(){
		return calculatedValue;
	}
	
	/**
	 * set the key times
	 * @param _keyTimes
	 */
	public void setKeyTimes(double[] _keyTimes){
		relKeyTimes = _keyTimes;
	}

	/**
	 * parse the values and stores them
	 * @param _content
	 */
	private void parse(String _content){
		String[] segmts = _content.split("\\s+");
		keyValues = new double[segmts.length];
		relKeyTimes = new double[segmts.length];
		for(int i = 0; i < segmts.length; i++){
			try{
				keyValues[i] = Double.parseDouble(segmts[i]);
				relKeyTimes[i] = i * 1. / (double)(segmts.length - 1);
			} catch (NumberFormatException e){;}
		}
		// make sure the last keyTime is a perfect 1.0:
		relKeyTimes[segmts.length - 1] = 1.0f;
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bang(CMsgShuttle _msg) {
		// TODO Auto-generated method stub
		
	}

	public void lockLessBang(CMsgShuttle _msg){;}

	@Override
	public void resume(long _timePassed) {
		// TODO Auto-generated method stub
		
	}

}
