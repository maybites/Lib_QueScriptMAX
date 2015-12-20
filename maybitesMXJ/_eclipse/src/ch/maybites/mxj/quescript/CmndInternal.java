package ch.maybites.mxj.quescript;

import org.w3c.dom.Node;

import ch.maybites.mxj.quescript.messages.CMsgFade;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.expression.RunTimeEnvironment;

public class CmndInternal extends Cmnd {
	public static String NODE_NAME_STOP = "stop";
	public static String NODE_NAME_RESUME = "resume";
	public static String NODE_NAME_PAUSE = "pause";
	public static String NODE_NAME_PLAY = "play";
	public static String NODE_NAME_SHUTDOWN = "shutdown";
	
	private static String ATTR_NAME = "name";

	String name;
	
	public CmndInternal(CmndInterface _parentNode, String _cmdName){
		super(_parentNode);
		super.setCmndName(_cmdName);
		super.setAttrNames(new String[]{ATTR_NAME});
		super.setChildNames(new String[]{});
	}
	
	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);
		
		// use the attribute or the first value of the key
		if(this.hasAttributeValue(ATTR_NAME))
			name = getAttributeValue(ATTR_NAME);
		
	}

	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created internal "+ cmdName +"-Comnd for: " + name);	
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepper(CMsgShuttle _msg) {
		if(!_msg.isWaitLocked()){
			lockLessStepper(_msg);
		}
	}

	public void lockLessStepper(CMsgShuttle _msg){
		if(!_msg.isInStopMode()){
			this.getOutput().outputSelfCommand(new String[]{cmdName, parentNode.getQueName(), name});
			if(getDebugMode())
				Debugger.verbose("QueScript "+cmdName+"-Command que:(" + parentNode.getQueName() + ")", "sent "+cmdName+"-message to "+cmdName+": " + name);
		}
	}

	@Override
	public void resume(long _timePassed) {
		// TODO Auto-generated method stub
		
	}

}
