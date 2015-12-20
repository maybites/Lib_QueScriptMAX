package ch.maybites.mxj.quescript;

import org.w3c.dom.Node;

import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.expression.RunTimeEnvironment;

public class CmndTimer extends Cmnd {
	protected static String NODE_NAME = "timer";

	public CmndTimer(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
		super.setAttrNames(new String[]{});
		super.setChildNames(new String[]{});
	}

	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);
	}

	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created "+cmdName+"-Comnd");			
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	public void stepper(CMsgShuttle _msg) {
		if(!_msg.isWaitLocked()){
			_msg.setTimer();
			if(_msg.isDebugging())
				Debugger.verbose("QueScript - Command Timer", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created timer = '"+_msg.getTimerTime().print()+"'");			
		}
	}

	public void lockLessStepper(CMsgShuttle _msg){;}

	@Override
	public void resume(long _timePassed) {
		// TODO Auto-generated method stub
		
	}

}
