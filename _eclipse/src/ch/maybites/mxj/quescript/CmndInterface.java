package ch.maybites.mxj.quescript;

import org.w3c.dom.*;

import ch.maybites.mxj.expression.RunTimeEnvironment;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;

public interface CmndInterface {
	
	public int getLevel();

	public boolean getDebugMode();
	
	public String getQueName();

	public int getLine();
	
	public void build(Node _xmlNode)throws ScriptMsgException;

	//public void messagePass(CMsgTrigger _msg);

	public void store(Node _parentElement);
	
	public void printStructure();

	OutputInterface getOutput();
	
	public Cmnd getThis();

}
