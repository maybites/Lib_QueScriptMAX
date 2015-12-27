package ch.maybites.quescript.commands;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.quescript.expression.Expression;
import ch.maybites.quescript.expression.ExpressionVar;
import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.expression.Expression.ExpressionException;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.CMsgTime;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndWait extends Cmnd {
	protected static String NODE_NAME = "wait";
	
	private static String ATTR_WATCH = "watch";
	private static String ATTR_HOURGLASS = "hourglass";
	private static String ATTR_TRIGGER = "trigger";
	private static String ATTR_COUNTDOWN = "countdown";
	private static String ATTR_ANIM = "anim";
	private static String ATTR_TIMER = "timer";
	private static String ATTR_COMPLEX = "complex";
	private static String ATTR_WHILE = "while";
	private static String ATTR_UNTIL = "until";
	private static String ATTR_RAMP = "ramp";
	
	private final int MODE_COMPLEX 		= 0;
	private final int MODE_WATCH 		= 1;
	private final int MODE_HOURGLASS 	= 2;
	private final int MODE_TRIGGER 		= 3;
	private final int MODE_COUNTDOWN 	= 4;
	private final int MODE_ANIM 		= 5;
	private final int MODE_TIMER 		= 6;
	private final int MODE_WHILE 		= 7;
	private final int MODE_UNTIL 		= 8;
	private final int MODE_RAMP 		= 9;
	
	private int mode = -1;
	
	private CMsgTime myTime;
	private CMsgTime myCountdown;
	
	private ExpressionVar untilWhileCondition = null;

	public CmndWait(Cmnd _parentNode){
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
		if(getAttributes().size() == 1){
			String smode = getAttributes().iterator().next();
			try {
				if(smode.equals(ATTR_COUNTDOWN)){
					mode = MODE_COUNTDOWN;
					myTime = getAttributeTime(getAttributeValue(ATTR_COUNTDOWN),rt);
				} else if(smode.equals(ATTR_WATCH)){
					mode = MODE_WATCH;
					myTime = getAttributeTime(getAttributeValue(ATTR_WATCH),rt);
				} else if(smode.equals(ATTR_HOURGLASS)){
					mode = MODE_HOURGLASS;
					myTime = getAttributeTime(getAttributeValue(ATTR_HOURGLASS),rt);
				} else if(smode.equals(ATTR_TIMER)){
					mode = MODE_TIMER;
					myTime = getAttributeTime(getAttributeValue(ATTR_TIMER),rt);
				} else if(smode.equals(ATTR_COMPLEX)){
					mode = MODE_COMPLEX;
				} else if(smode.equals(ATTR_TRIGGER)){
					mode = MODE_TRIGGER;
				} else if(smode.equals(ATTR_RAMP)){
					mode = MODE_RAMP;
				} else if(smode.equals(ATTR_ANIM)){
					mode = MODE_ANIM;
				} else if(smode.equals(ATTR_UNTIL)){
					mode = MODE_UNTIL;
					untilWhileCondition = new Expression(getAttributeValue(ATTR_UNTIL), "{", "}").parse(rt);
				} else if(smode.equals(ATTR_WHILE)){
					mode = MODE_WHILE;
					untilWhileCondition = new Expression(getAttributeValue(ATTR_WHILE), "{", "}").parse(rt);
				}
				
				if(getDebugMode())
					Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created Wait Cmnd with mode = " + smode);
			} catch (ScriptMsgException e) {
					throw new ScriptMsgException("<wait>: Attribute Expression: " + e.getMessage());
			} catch (ExpressionException e) {
				throw new ScriptMsgException("Command <wait>: Attribute Expression: " + e.getMessage());
			}
		} else {
			Debugger.error("Script - Command <wait>", "only one of these attibutes are allowed: " + this.getAttributeList());			
			throw new ScriptMsgException("<wait>: illegal attribute");
		}	
	}
	
	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bang(CMsgShuttle _msg) {
		if(mode != -1){
			if(_msg.isInStopMode()){
				myCountdown = null;
			}
			if(!_msg.isWaitLocked()){
				_msg.lockWaitLock(this);
				getOutput().outputInfoMsg("script", new Atom[]{Atom.newAtom(this.line)});
				getOutput().outputInfoMsg("info", new Atom[]{Atom.newAtom(parentNode.getQueName() + " : " + getMainAttribute())});
			}
			if(_msg.isWaitLockedBy(this)){
				if(logic(_msg)){
					for(Cmnd child : getChildren()){
						if(!child.isCmndName(NODE_NAME))
							child.bang(_msg);
					}
					_msg.freeWaitLock(this);
					getOutput().outputInfoMsg("info", new Atom[]{Atom.newAtom("-")});
				}
			}
		}
	}

	public void lockLessBang(CMsgShuttle _msg){;}

	protected boolean logic(CMsgShuttle _msg){
		switch(mode){
		case MODE_COMPLEX:
			if(ifCondition(_msg))
				return true;
			break;
		case MODE_TRIGGER:
			if(_msg.hasTriggerMessage(getAttributeValue(ATTR_TRIGGER))){
				_msg.removeTriggerMessage(getAttributeValue(ATTR_TRIGGER));
				return true;
			}
			break;
		case MODE_ANIM:
			if(_msg.hasAnimMessage(getAttributeValue(ATTR_ANIM)))
				return true;
			break;
		case MODE_RAMP:
			if(_msg.hasAnimMessage(getAttributeValue(ATTR_RAMP)))
				return true;
			break;
		case MODE_TIMER:
			if(_msg.hasFrameTimePassedTimer(myTime))
				return true;
			break;
		case MODE_WATCH:
			if(_msg.hasFrameTimePassedWatch(myTime))
				return true;
			break;
		case MODE_WHILE:
			try {
				if(untilWhileCondition.eval().getNumberValue() == 0)
					return true;
			} catch (ExpressionException e) {
				Debugger.error("Script - Command <wait>", "while condition: " + e.getLocalizedMessage());			
			}
			break;
		case MODE_UNTIL:
			try {
				if(untilWhileCondition.eval().getNumberValue() == 1)
					return true;
			} catch (ExpressionException e) {
				Debugger.error("Script - Command <wait>", "until condition: " + e.getLocalizedMessage());			
			}
			break;
		case MODE_HOURGLASS:
			if(_msg.hasFrameTimePassedHourglass(myTime))
				return true;
			break;
		case MODE_COUNTDOWN:
			if(myCountdown == null){
				myCountdown = _msg.getCountDownTime(myTime);
			} else if(_msg.hasFrameTimePassedCountdown(myCountdown)){
				myCountdown = null;
				return true;
			} 
			break;
		}
		return false;
	}
	
	private String getMainAttribute(){
		String ret = "";
		switch(mode){
		case MODE_COMPLEX:
			ret = ATTR_COMPLEX + "=" + getAttributeValue(ATTR_COMPLEX);
			break;
		case MODE_TRIGGER:
			ret = ATTR_TRIGGER + "=" + getAttributeValue(ATTR_TRIGGER);
			break;
		case MODE_ANIM:
			ret = ATTR_ANIM + "=" + getAttributeValue(ATTR_ANIM);
			break;
		case MODE_TIMER:
			ret = ATTR_TIMER + "=" + getAttributeValue(ATTR_TIMER);
			break;
		case MODE_WATCH:
			ret = ATTR_WATCH + "=" + getAttributeValue(ATTR_WATCH);
			break;
		case MODE_HOURGLASS:
			ret = ATTR_HOURGLASS + "=" + getAttributeValue(ATTR_HOURGLASS);
			break;
		case MODE_COUNTDOWN:
			ret = ATTR_COUNTDOWN + "=" + getAttributeValue(ATTR_COUNTDOWN);
			break;
		}
		return ret;
	}
	
	/**
	 * This message is called by 
	 * @param _msg
	 */
	public boolean ifCondition(CMsgShuttle _msg){
		int conditionAdder = 0;
		int conditionCounter = 0;
		for(Cmnd child : getChildren()){
			if(child.isCmndName(NODE_NAME)){
				conditionAdder += ((((CmndWait)child).logic(_msg))? 1: 0);
				conditionCounter++;
			}
		}
		if(getAttributeValue(ATTR_COMPLEX).equals("and")){
			if(conditionAdder == conditionCounter)
				return true;
		}else if(getAttributeValue(ATTR_COMPLEX).equals("or")){
			if(conditionAdder > 0)
				return true;
		}else if(getAttributeValue(ATTR_COMPLEX).equals("not")){
			if(conditionAdder == 0)
				return true;
		}
		return false;
	}

	@Override
	public void resume(long _timePassed) {
		if(myCountdown != null){
			myCountdown.add(_timePassed);
		}
		// TODO Auto-generated method stub
		
	}

}
