package ch.maybites.mxj.quescript;

import java.util.ArrayList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.cycling74.max.Atom;

import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.CMsgStop;
import ch.maybites.mxj.quescript.messages.CMsgTime;
import ch.maybites.mxj.quescript.messages.CMsgTrigger;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.mxj.utils.JitterObjectArray;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.expression.Expression;
import ch.maybites.tools.expression.RunTimeEnvironment;

public class CmndQue extends Cmnd{
	public static String NODE_NAME = "que";
	
	private static String ATTR_NAME = "name";
	private static String ATTR_LOOP = "loop";

	private static String ATTR_LOOP_VAL_NO = "no";
	private static String ATTR_LOOP_VAL_YES = "yes";
	
	CMsgShuttle executionShuttle;
		
	boolean isLooping = false;
	
	boolean isPlaying = false;

	RunTimeEnvironment localExprEnvironment;

	CMsgTime pauseTime;
	
	protected CmndQue(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
		super.setAttrNames(new String[]{ATTR_NAME, ATTR_LOOP});
		super.setChildNames(new String[]{
				CmndMessage.NODE_NAME_OSC,
				CmndMessage.NODE_NAME_OUT,
				CmndMessage.NODE_NAME_PRINT,
				CmndMessage.NODE_NAME_SEND,
				CmndMessage.NODE_NAME_TRIGGER,
				CmndInternal.NODE_NAME_PAUSE, 
				CmndInternal.NODE_NAME_PLAY, 
				CmndInternal.NODE_NAME_RESUME,
				CmndInternal.NODE_NAME_SHUTDOWN,
				CmndInternal.NODE_NAME_STOP,
				CmndAnim.NODE_NAME,
				CmndExpr.NODE_NAME,
				CmndWait.NODE_NAME,
				CmndFade.NODE_NAME,
				CmndTimer.NODE_NAME,
				CmndIf.NODE_NAME,
				CmndWhile.NODE_NAME,
				CmndDebugger.NODE_NAME,
				"ramp", "or", "and"});
		
    	localExprEnvironment = new RunTimeEnvironment();
	}
	
	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);

		isLooping = (this.getAttributeValue(ATTR_LOOP).equals(ATTR_LOOP_VAL_NO))?false: true;
		
		queName = getAttributeValue(ATTR_NAME);
		
		executionShuttle = new CMsgShuttle(localExprEnvironment);
	}
	
	/**
	 * takes the global Expression-runtime-environment and modifies its own 
	 * local Expression-runtime-environment
	 */
	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "... created Que:" + queName);	

		localExprEnvironment.setPublicVars(rt.getPublicVars());
		localExprEnvironment.setProtectedVariable("$TIMER", 0);
		for(Cmnd child: this.getChildren()){
			child.parseExpr(localExprEnvironment);
		}
	}
	
	/**
	 * is called by the scripter to start the que
	 * @param debugMode 
	 */
	public void play(boolean debugMode){
//		Debugger.verbose("Script-Command Que", "beeing executed: " + this.queName);	
		// if the que is still running, it first has to be stopped
		if(executionShuttle.isInExecution() || executionShuttle.isInShutDownMode()){
//			Debugger.verbose("Script-Command Que", "... stopped: " + this.queName);	
			executionShuttle.stop();
			stepper(executionShuttle);
		}
		// and now it can be restarted
		executionShuttle.execute();
//		Debugger.verbose("Script-Command Que", "...executed: " + this.queName);	
		
		// and set the debug mode
		if(debugMode)
			executionShuttle.setDebugInfo();

		// pass the info of how many lines are in this que
		getOutput().outputInfoMsg("quename", new Atom[]{Atom.newAtom(queName)});

		// pass the info of how many lines are in this que
		getOutput().outputInfoMsg("script", new Atom[]{Atom.newAtom("size"), Atom.newAtom(getLine())});
		
		isPlaying = true;
	}
	
	public void resume(){
		if(!isPlaying && !executionShuttle.isOff()){
			executionShuttle.frameBang(localExprEnvironment);

			// calculate the time passed during the pause 
			long resumeTime = executionShuttle.getFrameTime().getTotalMillis() - pauseTime.getTotalMillis();
			// tell all child nodes about this
			resume(resumeTime);
			// set the timer accordingly
			executionShuttle.addToTimer(resumeTime);
			
			// keep on playing
			isPlaying = true;
		}
	}
	
	public void pause(){
		if(isPlaying && !executionShuttle.isOff()){
			isPlaying = false;
			pauseTime = executionShuttle.getFrameTime();
		}
	}
	
	/**
	 * is called by the scripter when a new que was selected
	 */
	public void shutDown(){
		if(executionShuttle.isInExecution()){
			if(executionShuttle.isDebugging())
				Debugger.verbose("QueScript", "...shuting down: " + this.queName);	
			executionShuttle.shutDown();
			
			//TODO on execution stop the waitlock needs to be removed and should not
			// be able to be set again until it is executed again..
		}
	}

	/** 
	 * Fullstop of the execution
	 */
	public void stop(){
		executionShuttle.stop();			
		stepper(executionShuttle);
		executionShuttle.off();
		isPlaying = false;
		if(executionShuttle.isDebugging())
			Debugger.verbose("QueScript", "stop: " + this.queName);	
	}

	/**
	 * is called by the scripter on every frame
	 * @param _triggerQueue
	 */
	public void bang(ArrayList<CMsgTrigger> _triggerQueue){
		if(isPlaying){
			if(executionShuttle.isInExecution()){
				// add first all trigger messages into the container
				for(CMsgTrigger trgger: _triggerQueue)
					executionShuttle.addMessage(trgger);

				executionShuttle.frameBang(localExprEnvironment);
				stepper(executionShuttle);
				// if the que is over and no looping is set, shutdown this que
				if(!executionShuttle.isWaitLocked() && !isLooping){
					executionShuttle.shutDown();
					if(executionShuttle.isDebugging())
						Debugger.verbose("QueScript", "... shuting down: " + this.queName);	
				}
			}  else if(executionShuttle.isInShutDownMode()){
				// the create the frame Timer
				executionShuttle.frameBang(localExprEnvironment);
				stepper(executionShuttle);
				if(!executionShuttle.hasNodesInShutDown()){
					if(executionShuttle.isDebugging())
						Debugger.verbose("QueScript", "stop: " + this.queName);	
					executionShuttle.off();
					isPlaying = false;
				}
			}
		}
	}
	
	public void update(){
	}
	
	public void clear(){
	}

	public void stepper(CMsgShuttle _msg) {
		for(Cmnd child: super.getChildren()){
			child.stepper(_msg);
		}
		_msg.clearMessages();
	}

	public void lockLessStepper(CMsgShuttle _msg){;}

	public String getQueName(){
		return this.getAttributeValue(ATTR_NAME);
	}

	@Override
	public void resume(long _timePassed) {
		for(Cmnd child: super.getChildren()){
			child.resume(_timePassed);
		}
	}
	
}
