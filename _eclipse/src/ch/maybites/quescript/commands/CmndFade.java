package ch.maybites.quescript.commands;

import org.w3c.dom.Node;

import ch.maybites.quescript.expression.Expression;
import ch.maybites.quescript.expression.ExpressionVar;
import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.expression.Expression.ExpressionException;
import ch.maybites.quescript.messages.CMsgFade;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.CMsgTime;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndFade extends Cmnd {
	protected static String NODE_NAME = "fade";

	private static String ATTR_FADEOUT = "fadeout";
	private static String ATTR_NAME = "name";

	String name;
	CMsgTime fadetime;

	public CmndFade(Cmnd _parentNode){
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
		// use the attribute or the first value of the key
		if(this.hasAttributeValue(ATTR_FADEOUT))
			try {
				fadetime = getAttributeTime(getAttributeValue(ATTR_FADEOUT),rt);
			} catch (ExpressionException e) {
				throw new ScriptMsgException("Setting fadeout: Invalid time format: " + e.getMessage());
			}
		if(this.hasAttributeValue(ATTR_NAME))
			name = getAttributeValue(ATTR_NAME);

		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+"created fade Comnd");	
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bang(CMsgShuttle _msg) {
		if(!_msg.isWaitLocked()){
			lockLessBang(_msg);
		}
	}

	public void lockLessBang(CMsgShuttle _msg){
		if(_msg.isDebugging())
			Debugger.verbose("QueScript", "que("+parentNode.getQueName()+") sent fade message");

		_msg.addMessage(new CMsgFade(name, fadetime));
	}

	@Override
	public void resume(long _timePassed) {
		// dont care
	}

}
