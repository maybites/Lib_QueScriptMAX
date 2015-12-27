package ch.maybites.quescript.commands;

import ch.maybites.quescript.OutputInterface;
import ch.maybites.quescript.messages.CMsgShuttle;

public class CmndScript extends Cmnd{
	public static String NODE_NAME = "script";
	
	OutputInterface output;

	public CmndScript() {
		super(null);
		super.setCmndName(NODE_NAME);
	}
	
	public void setOutput(OutputInterface output){
		this.output = output;
	}

	public OutputInterface getOutput(){
		return output;
	}

	@Override
	public void bang(CMsgShuttle _msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lockLessBang(CMsgShuttle _msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume(long _timePassed) {
		// TODO Auto-generated method stub
		
	}

}
