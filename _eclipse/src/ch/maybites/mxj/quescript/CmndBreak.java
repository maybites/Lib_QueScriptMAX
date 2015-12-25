package ch.maybites.mxj.quescript;

import org.w3c.dom.Node;

import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndBreak extends Cmnd {

	public CmndBreak(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName("break");
	}

	public void build(Node _xmlNode) throws ScriptMsgException{
		super.build(_xmlNode);
		Debugger.verbose("QueScript - NodeFactory", "created Break Comnd");			
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
