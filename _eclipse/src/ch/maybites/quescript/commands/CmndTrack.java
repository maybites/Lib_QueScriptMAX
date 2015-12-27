package ch.maybites.quescript.commands;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.quescript.expression.Expression;
import ch.maybites.quescript.expression.ExpressionVar;
import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.expression.Expression.ExpressionException;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndTrack extends Cmnd {
	public static String NODE_NAME = "track";

	private static String ATTR_FADETO = "fadeto";
	private static String ATTR_NAME = "name";

	private ExpressionVar[] keyValues;
	private double[] relKeyTimes;
	private ExpressionVar fadeTo;
	private double fadeFrom;
	
	public String trackName;
	
	private ExpressionVar calculatedValue;
	
	boolean fadeToMode = false;
			
	public CmndTrack(Cmnd _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
		calculatedValue = new ExpressionVar(0);
	}
	
	public void build(Node _xmlNode) throws ScriptMsgException{
		super.build(_xmlNode);
		trackName = getAttributeValue(ATTR_NAME);
	}

	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void setup(RunTimeEnvironment rt)throws ScriptMsgException{
				
		List<String> segmts = new ArrayList<String>();
		Matcher m = Pattern.compile("([^{]\\S*|.+?[{}])\\s*").matcher(super.content);
		while (m.find()){
			String kv = m.group(1).trim();
			if(kv.startsWith("{") && kv.endsWith("}")){
				kv = kv.substring(1, kv.length() - 1);
			}
			segmts.add(kv); // Add .replace("\"", "") to remove surrounding quotes.
		}
		
		keyValues = new ExpressionVar[segmts.size()];
		boolean setupRelKeyTimes = false;
		if(relKeyTimes == null){
			relKeyTimes = new double[segmts.size()];
			setupRelKeyTimes = true;
		}

		for(int i = 0; i < segmts.size(); i++){
			try{
				keyValues[i] = new Expression(segmts.get(i)).parse(rt);
				if(setupRelKeyTimes)
					relKeyTimes[i] = i * 1. / (double)(segmts.size() - 1);
			} catch (ExpressionException e) {
				throw new ScriptMsgException("<track>: "+e.getMessage());
			}			
		}
		// make sure the last keyTime is a perfect 1.0:
		if(setupRelKeyTimes)
			relKeyTimes[segmts.size() - 1] = 1.0f;

		// use the attribute or the first value of the key
		if(this.hasAttributeValue(ATTR_FADETO)){
			try {
				fadeTo = new Expression(getAttributeValue(ATTR_FADETO), "{", "}").parse(rt);
			} catch (ExpressionException e) {
				throw new ScriptMsgException("<que name=\""+parentNode.getQueName()+"\"> <track name=\""+trackName+"\" ("+getAttributeValue(ATTR_FADETO)+")>: "+e.getMessage());
			}
		}
		else
			fadeTo = keyValues[0];

		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created track-Comnd: name='" + trackName + "' | " + super.content);	

		// and then do it for all the children
		for(Cmnd child: this.getChildren()){
			child.setup(rt);
		}
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
		try {
			fadeTo.eval();
			if(fadeToMode){
				calculatedValue.setValue((fadeTo.getNumberValue() - fadeFrom) * _time + fadeFrom);
				//			Debugger.verbose("Script FLOAT - Command", "fade _time =" + _time);
				return;
			} else {
				for(int i = 0; i < keyValues.length; i++){
					keyValues[i].eval();
				}
				for(int i = 0; i < relKeyTimes.length - 1; i++){
					if(relKeyTimes[i] <= _time && _time < relKeyTimes[i+1]){
						double diffTime = relKeyTimes[i+1] - relKeyTimes[i];
						double relTime = (_time - relKeyTimes[i]) / diffTime;
						calculatedValue.setValue((keyValues[i + 1].getNumberValue() - keyValues[i].getNumberValue()) * relTime + keyValues[i].getNumberValue());
						return;
					}
				}
				calculatedValue.setValue(keyValues[keyValues.length -1].getNumberValue());
			}
		} catch (ExpressionException e) {
			Debugger.error("QueScript que("+parentNode.getQueName()+") "+cmdName+" Comnd", e.getMessage());	
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
