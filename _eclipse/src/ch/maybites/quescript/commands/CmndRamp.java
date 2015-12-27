package ch.maybites.quescript.commands;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Node;

import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.messages.CMsgAnim;
import ch.maybites.quescript.messages.CMsgFade;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.CMsgTime;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

/**
 * @Deprecated
 * @author maybites
 *
 */
public class CmndRamp extends Cmnd {
	private static String NODE_NAME = "ramp";
	
	private static String ATTR_LOOP = "loop";
	private static String ATTR_NAME = "name";
	private static String ATTR_DURATION = "duration";
	private static String ATTR_FADEOUT = "fadeout";

	private static String ATTR_LOOP_VAL_NO = "no";
	private static String ATTR_LOOP_VAL_NORMAL = "normal";
	private static String ATTR_LOOP_VAL_PALINDROME = "palindrome";

	private final static int LOOP_MODE_NONE			= 1;
	private final static int LOOP_MODE_NORMAL 		= 2;
	private final static int LOOP_MODE_PALINDROME 	= 3;
	
	private final static int EXECUTE_OFF 		= 0;
	private final static int EXECUTE 			= LOOP_MODE_NONE;  
	private final static int EXECUTE_LOOP 		= LOOP_MODE_NORMAL;
	private final static int EXECUTE_PALINDROME = LOOP_MODE_PALINDROME;
	private final static int EXECUTE_PAUSE 		= 4;
	private final static int EXECUTE_FADEOUT 	= 5;
	private final static int EXECUTE_SHUTDOWN 	= 6;
	
	private int loop;
	private String name;
	private CMsgTime durationTime;
	private CMsgTime fadeoutTime;
	private CMsgTime fadeoutTime_original;
	
	private CMsgTime executionTime;
	
	int runMode = EXECUTE_OFF;
	
	boolean palindromDirection = false;
			
	Hashtable<String, CmndDOUBLE> valueInterolators;
	ArrayList<CmndMessage> sendCommands;
	
	double[] relKeyTiming = null;

	RunTimeEnvironment privateExprEnvironment;

	public CmndRamp(Cmnd _parentNode){
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
		privateExprEnvironment.setPublicVars(rt.getPublicVars());
		privateExprEnvironment.setProtectedVars(rt.getProtectedVars());

		// and then do it for all the children
		for(Cmnd child: this.getChildren()){
			child.setup(privateExprEnvironment);
		}
		
		if(this.getAttributeValue(ATTR_LOOP).equals(ATTR_LOOP_VAL_NO))
			loop = LOOP_MODE_NONE;
		else if(this.getAttributeValue(ATTR_LOOP).equals(ATTR_LOOP_VAL_NORMAL))
			loop = LOOP_MODE_NORMAL;
		else if(this.getAttributeValue(ATTR_LOOP).equals(ATTR_LOOP_VAL_PALINDROME))
			loop = LOOP_MODE_PALINDROME;
		
		try {
			durationTime = new CMsgTime(getAttributeValue(ATTR_DURATION));
			if(hasAttributeValue(ATTR_FADEOUT)){
				fadeoutTime = new CMsgTime(getAttributeValue(ATTR_FADEOUT));
				// a fadeout time of zero is causing troubles with the algorithm:
				// better set it to very short.
				if(fadeoutTime.getTotalMillis()==0){
					fadeoutTime = new CMsgTime("1ms");
				}
				fadeoutTime_original = fadeoutTime.clone();
			} 
		} catch (ScriptMsgException e) {
			Debugger.error("Script - Command Ramp", e.getMessage());
			throw new ScriptMsgException("<ramp>: "+e.getMessage());
		}

		// if there is a keys command, get the keyTimes.
		for(Cmnd child: this.getChildren()){
			if(child.isCmndName(CmndKeys.NODE_NAME)){
				CmndKeys snd = (CmndKeys)child;
				relKeyTiming = snd.getKeyTimes(durationTime.getTotalMillis());
			}
		}

		name = getAttributeValue(ATTR_NAME);
		
		valueInterolators = new Hashtable<String, CmndDOUBLE>();
		
		for(Cmnd child: this.getChildren()){
			if(child.isCmndName("float")){
				CmndDOUBLE flt = (CmndDOUBLE)child;
				valueInterolators.put(flt.keyName, flt);
				if(relKeyTiming != null)
					flt.setKeyTimes(relKeyTiming);
			}
		}

		sendCommands = new ArrayList<CmndMessage>();
		
		for(Cmnd child: this.getChildren()){
			if(child.isCmndName(CmndMessage.NODE_NAME_OUT) ||
					child.isCmndName(CmndMessage.NODE_NAME_PRINT) ||
					child.isCmndName(CmndMessage.NODE_NAME_SEND) ||
					child.isCmndName(CmndMessage.NODE_NAME_TRIGGER)){
				CmndMessage snd = (CmndMessage)child;
				snd.registerDouble(valueInterolators);
				sendCommands.add(snd);
			}
		}
		
		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created Ramp Comnd");	
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bang(CMsgShuttle _msg) {
		if(_msg.isInStopMode() && 
				(runMode != EXECUTE_OFF)){
			// if there is a stop message (called by the stop command or the interruption
			// of the que),
			
			// if a fadeout is set:
			if(fadeoutTime != null){
				// and there is a fadeout time set, then it attempts to reach the fadeout target
				
				// it is a fullstop message, then it will try to reach
				// the fadeout target immediately
				if(_msg.isInFullStopMode()){
					// make sure the execution is running, but only once so it is set to the
					// defined fadeout values
					runMode = EXECUTE_FADEOUT;
					setFade2Mode(runMode);
					palindromDirection = false;

					executionTime = _msg.getFrameTime().subtract(fadeoutTime);
//					Debugger.info("Script - Command <ramp> fullstop", "set execution time("+executionTime.print()+")");			
				} else if(_msg.isInShutDownMode() && runMode != EXECUTE_FADEOUT){
					// or its a shutdown message and it allows to reach with the default fadeout time
					runMode = EXECUTE_FADEOUT;
					setFade2Mode(runMode);
					palindromDirection = false;

					executionTime = _msg.getFrameTime();
//					Debugger.info("Script - Command <ramp> shutdown", "set execution time("+executionTime.print()+")");			
				}


			} else {
				// otherwise it stops the ramp if it is in LOOP and still executing
				if(runMode == EXECUTE_LOOP || runMode == EXECUTE_PALINDROME || _msg.isInFullStopMode()){
					palindromDirection = false;
					runMode = EXECUTE_OFF;
				}
				// this also means that it will allow to finish the ramp
			}
			
		} else if(_msg.hasFadeMessage(name) && 
				(runMode != EXECUTE_OFF && runMode != EXECUTE_FADEOUT)){
			// if there is a fade message use the fade message value
//			Debugger.verbose("Script Ramp - Command", "received fade message");
			runMode = EXECUTE_FADEOUT;
			setFade2Mode(runMode);
			palindromDirection = false;
			
			executionTime = _msg.getFrameTime();
//			Debugger.info("Script - Command <ramp> fade", "set execution time("+executionTime.print()+")");			
						
			CMsgFade fade = _msg.getFadeMessage(name);
			fade.tagForDeletion();
			if(fade.hasFadeTime())
				fadeoutTime = fade.getFadeTime();
		} else {
			// here the ramp is actually started. 
			if(!_msg.isWaitLocked() && 
					(runMode == EXECUTE_OFF || runMode == EXECUTE_PAUSE)){
				runMode = loop;
				setFade2Mode(runMode);
				palindromDirection = false;
				
				executionTime = _msg.getFrameTime();
//				Debugger.info("Script - Command <ramp> start", "set execution time("+executionTime.print()+")");			
				fadeoutTime = fadeoutTime_original;
			}
		}
		// and in here the calculations are done
		if(runMode != EXECUTE_OFF && runMode != EXECUTE_PAUSE){
			long passedTime = _msg.getFrameTime().subtract(executionTime).getTotalMillis();
			float normalizedTime;
			if(runMode == EXECUTE_FADEOUT){
				normalizedTime = (float)passedTime / (float)fadeoutTime.getTotalMillis();
//				Debugger.info("Script - Command <ramp> execute", "passedTime = "+passedTime+" | normalizedTime = " + normalizedTime);			
			}else{
				normalizedTime = (float)passedTime / (float)durationTime.getTotalMillis();
			}
			// now make sure it will be exactly 1 if time passes the duration
			normalizedTime = (normalizedTime > 1.0)? 1.0f: normalizedTime;
			// and then turn it depending of the palindrome direction

			execute(normalizedTime, _msg);

			// in here the decision is made what to do when the target time has been reached
			if(normalizedTime == 1.0f){
				switch(runMode){
				case EXECUTE_FADEOUT:
					runMode = EXECUTE_OFF;
					break;
				case EXECUTE:
					runMode = (fadeoutTime != null)? EXECUTE_PAUSE: EXECUTE_OFF;
					_msg.addMessage(new CMsgAnim(name));
					break;
				case EXECUTE_LOOP:
					executionTime = _msg.getFrameTime();
					break;
				case EXECUTE_PALINDROME:
					executionTime = _msg.getFrameTime();
					palindromDirection = !palindromDirection;
					break;
				}
			}
			// and tell the que command that it is still running
			_msg.addNodesStillRunning();
		}
	}

	public void lockLessBang(CMsgShuttle _msg){;}

	private void execute(float _normalizedTime, CMsgShuttle _msg){
		_normalizedTime = (palindromDirection)? 1.0f - _normalizedTime: _normalizedTime;

//		Debugger.verbose("Script - Command Ramp", "normalized time: " + _normalizedTime);
		
		Enumeration<CmndDOUBLE> e = valueInterolators.elements();
		while(e.hasMoreElements())
			e.nextElement().calculate(_normalizedTime);
		
		for(CmndMessage snd: sendCommands)
			snd.lockLessBang(_msg);		
	}
	
	private void setFade2Mode(int _mode){
		Enumeration<CmndDOUBLE> e = valueInterolators.elements();
		while(e.hasMoreElements())
			e.nextElement().fadeToMode((_mode == EXECUTE_FADEOUT)? true: false);		
	}

	public void resume(long _timePassed) {
		if(executionTime != null){
			executionTime.add(_timePassed);
		}
	}
}
