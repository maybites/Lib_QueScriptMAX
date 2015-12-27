package ch.maybites.quescript.commands;

import org.w3c.dom.Node;

import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.CMsgTime;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndKeys extends Cmnd {
	public static String NODE_NAME = "keys";
	
	private static String ATTR_TIMING = "timing";

	private double[] relKeyTimes;
	
	private boolean timingIsAbsolute;

	public CmndKeys(Cmnd _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
	}

	public void build(Node _xmlNode) throws ScriptMsgException{
		super.build(_xmlNode);
	}

	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void setup(RunTimeEnvironment rt)throws ScriptMsgException{
		if(this.hasAttributeValue(ATTR_TIMING))
			timingIsAbsolute = (getAttributeValue(ATTR_TIMING).equals("abs"))? true: false;
		else
			timingIsAbsolute = true;
		
		parse(super.content);		

		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created keys Comnd");			
	}

	/**
	 * get the relative key times. if the key times are set in absolute values, they are
	 * converted into relative key times.
	 * 
	 * @param _duration the duration of the ramp in milliseconds
	 * @return the relative key times
	 */
	public double[] getKeyTimes(long _duration){
		if(timingIsAbsolute){
			double[] ret = new double[relKeyTimes.length];
			for(int i = 1; i < (ret.length - 1); i++){
				ret[i] = relKeyTimes[i] / _duration;
			}
			ret[0] = relKeyTimes[0];
			ret[ret.length - 1] = relKeyTimes[ret.length - 1];
			return ret;
		}
		return relKeyTimes;
	}
	
	/**
	 * parse the values and stores them
	 * @param _content
	 */
	private void parse(String _content){
		String[] segmts = _content.split("\\s+");
		relKeyTimes = new double[segmts.length + 2];
		for(int i = 0; i < segmts.length; i++){
			try{
				if(timingIsAbsolute){
					relKeyTimes[i + 1] = (float)(new CMsgTime(segmts[i])).getTotalMillis();
				} else {
					relKeyTimes[i + 1] = Float.parseFloat(segmts[i]);
				}
			} catch (NumberFormatException e){;
			} catch (ScriptMsgException e){;}
			
		}
		// make sure the last keyTime is a perfect 1.0:
		relKeyTimes[0] = 0.0f;
		relKeyTimes[relKeyTimes.length - 1] = 1.0f;
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
