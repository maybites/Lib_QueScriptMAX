package ch.maybites.mxj.quescript;

import org.w3c.dom.Node;

import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndBreak extends Cmnd {

	public CmndBreak(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName("break");
		super.setAttrNames(new String[]{"watch", "hourglass", "trigger", "countdown", "ramp", "timer"});
		super.setChildNames(new String[]{});
	}

	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);
		Debugger.verbose("QueScript - NodeFactory", "created Break Comnd");			
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepper(CMsgShuttle _msg) {
		// TODO Auto-generated method stub
		
	}

	public void lockLessStepper(CMsgShuttle _msg){;}

	@Override
	public void resume(long _timePassed) {
		// TODO Auto-generated method stub
		
	}

}
