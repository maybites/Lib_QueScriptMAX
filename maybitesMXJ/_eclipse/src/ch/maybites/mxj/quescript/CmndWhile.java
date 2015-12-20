package ch.maybites.mxj.quescript;

import java.util.Enumeration;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.mxj.quescript.messages.CMsgAnim;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.CMsgTime;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.expression.Expression;
import ch.maybites.tools.expression.Expression.ExpressionException;
import ch.maybites.tools.expression.ExpressionVar;
import ch.maybites.tools.expression.RunTimeEnvironment;

public class CmndWhile extends Cmnd {
	protected static String NODE_NAME = "while";
		
	private static String ATTR_REPEAT = "repeat";
	private static String ATTR_START = "start";
	private static String ATTR_STEP = "step";
	private static String ATTR_NAME = "name";
		
	private ExpressionVar ifCondition = null;
	private ExpressionVar startCondition = null;
	private ExpressionVar stepCondition = null;
	
	private boolean running = false;
	
	private String name = null;

	public CmndWhile(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
		super.setAttrNames(new String[]{ATTR_REPEAT, ATTR_START, ATTR_NAME, ATTR_STEP});
		super.setChildNames(new String[]{
				CmndIf.NODE_NAME,
				CmndExpr.NODE_NAME,
				CmndMessage.NODE_NAME_OSC, 
				CmndMessage.NODE_NAME_OUT, 
				CmndMessage.NODE_NAME_PRINT, 
				CmndMessage.NODE_NAME_SEND, 
				CmndMessage.NODE_NAME_TRIGGER,
				CmndDebugger.NODE_NAME,
				CmndInternal.NODE_NAME_PAUSE, 
				CmndInternal.NODE_NAME_PLAY, 
				CmndInternal.NODE_NAME_RESUME,
				CmndInternal.NODE_NAME_SHUTDOWN,
				CmndInternal.NODE_NAME_STOP,
				CmndFade.NODE_NAME});
	}

	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);
		
		//if there is a nested <anim> node inside this if, it checks if there is another <anim> node
		// further down the tree towards the root
		for(Cmnd child: this.getChildren()){
			if(child instanceof CmndAnim ||child instanceof CmndWhile){
				Cmnd parnt = getParent(this);
				while(!(parnt instanceof CmndQue)){
					if(parnt instanceof CmndAnim ||parnt instanceof CmndWhile)
						throw new ScriptMsgException("Command <while>: Nesting of <anim> and <while> nodes are prohibited");
					parnt = getParent(parnt);
				}
			}
		}
	}

	private Cmnd getParent(Cmnd child){
		return child.parentNode.getThis();
	}


	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
		RunTimeEnvironment prt = new RunTimeEnvironment();

		prt.setPublicVars(rt.getPublicVars());
		prt.setProtectedVars(rt.getProtectedVars());

		try {
			if(getAttributeValue(ATTR_START) != null){
				startCondition = new Expression(getAttributeValue(ATTR_START), "{", "}").parse(prt);
			}
			ifCondition = new Expression(getAttributeValue(ATTR_REPEAT), "{", "}").parse(prt);
			if(getAttributeValue(ATTR_STEP) != null){
				stepCondition = new Expression(getAttributeValue(ATTR_STEP), "{", "}").parse(prt);
			}
			if(getAttributeValue(ATTR_NAME) != null){
				name = getAttributeValue(ATTR_NAME);
			}
		} catch (ExpressionException e) {
			throw new ScriptMsgException("Command <while>: Attribute Expression: " + e.getMessage());
		}

		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created while Cmnd: " + getAttributeValue(ATTR_REPEAT));

		// Make sure the que- and local- variables are created before the children are parsed
		for(Cmnd child: this.getChildren()){
			child.parseExpr(prt);
		}
	}
	
	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepper(CMsgShuttle _msg) {
		if(_msg.isInStopMode())
			running = false;
		if(_msg.hasFadeMessage(name))
			running = false;
		if(!_msg.isWaitLocked() || running){
			try {
				if(!running && startCondition != null){
					// the <while> loop starts here. should be only called once
					startCondition.eval();
				}
				if(ifCondition.eval().getNumberValue() >= 1){
					for(Cmnd child : getChildren()){
						child.lockLessStepper(_msg);
					}
					// if there is a step expression, 
					// it will be executed after all the <while> children
					if(stepCondition != null)
						stepCondition.eval();
					running = true;
				} else {
					running = false;
					// if a name is set, <while> will send an anim message once its looping has finished
					if(name != null)
						_msg.addMessage(new CMsgAnim(name));
				}
			} catch (ExpressionException e) {
				Debugger.error("Script - Command <while>", "while expression: " + e.getMessage());			
			}
		}
	}

	public void lockLessStepper(CMsgShuttle _msg){;}

	@Override
	public void resume(long _timePassed) {
	}

}
