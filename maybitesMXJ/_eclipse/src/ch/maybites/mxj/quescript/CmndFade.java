package ch.maybites.mxj.quescript;

import org.w3c.dom.Node;

import ch.maybites.mxj.quescript.messages.CMsgFade;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.CMsgTime;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.expression.Expression;
import ch.maybites.tools.expression.Expression.ExpressionException;
import ch.maybites.tools.expression.ExpressionVar;
import ch.maybites.tools.expression.RunTimeEnvironment;

public class CmndFade extends Cmnd {
	protected static String NODE_NAME = "fade";

	private static String ATTR_FADEOUT = "fadeout";
	private static String ATTR_NAME = "name";

	String name;
	CMsgTime fadetime;

	public CmndFade(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
		super.setAttrNames(new String[]{ATTR_NAME, ATTR_FADEOUT});
		super.setChildNames(new String[]{});
	}

	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);
	}

	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
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
	public void stepper(CMsgShuttle _msg) {
		if(!_msg.isWaitLocked()){
			lockLessStepper(_msg);
		}
	}

	public void lockLessStepper(CMsgShuttle _msg){
		if(_msg.isDebugging())
			Debugger.verbose("QueScript", "que("+parentNode.getQueName()+") sent fade message");

		_msg.addMessage(new CMsgFade(name, fadetime));
	}

	@Override
	public void resume(long _timePassed) {
		// dont care
	}

}
