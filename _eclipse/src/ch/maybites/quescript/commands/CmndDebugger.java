package ch.maybites.quescript.commands;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.quescript.expression.Expression;
import ch.maybites.quescript.expression.ExpressionVar;
import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndDebugger extends Cmnd {
	protected static String NODE_NAME = "debugger";

	private static String ATTR_SHOWVARDOMAIN = "vardomain";

	RunTimeEnvironment prt;
	
	boolean showLocal = false;
	boolean showQue = false;
	boolean showGlobal = false;

	public CmndDebugger(Cmnd _parentNode){
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
		prt = rt;

		if(getAttributeValue(ATTR_SHOWVARDOMAIN) != null){
			String domains = getAttributeValue(ATTR_SHOWVARDOMAIN);
			if(domains.contains("local"))
				showLocal = true;
			if(domains.contains("que"))
				showQue = true;
			if(domains.contains("global"))
				showGlobal = true;
		}

		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created "+cmdName+"-Comnd");			
	}

	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	public void bang(CMsgShuttle _msg) {
		if(!_msg.isWaitLocked()){
			lockLessBang(_msg);
		}
	}

	public void lockLessBang(CMsgShuttle _msg){
		if(getDebugMode()){
			HashMap<String, ExpressionVar> locals = (HashMap<String, ExpressionVar>) prt.getPrivateVars();
			HashMap<String, ExpressionVar> que = (HashMap<String, ExpressionVar>) prt.getProtectedVars();
			HashMap<String, ExpressionVar> global = (HashMap<String, ExpressionVar>) prt.getPublicVars();
			String var;
			ExpressionVar exVar;
			Iterator<String> it;
			if(showLocal || showQue || showGlobal){
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"DEBUGGER"}));
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"------------------"}));
			} else {
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"QueScript: <debugger> usage:"}));
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"<debugger " + ATTR_SHOWVARDOMAIN + "=\"local, que, global\" />"}));
			}
				
			if(showLocal){
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"Local Variables:"}));
				it = locals.keySet().iterator();
				while(it.hasNext()){
					var = it.next();
					exVar = locals.get(var);
					this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{var, " = ", exVar.getStringValue(), " (" + ((exVar.isNumber)?"float":"string") + ")"}));
				}
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"------------------"}));
			}
						
			if(showQue){
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"Que Variables:"}));
				it = que.keySet().iterator();
				while(it.hasNext()){
					var = it.next();
					exVar = que.get(var);
					this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{var, " = ", exVar.getStringValue(), " (" + ((exVar.isNumber)?"float":"string") + ")"}));
				}
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"------------------"}));
			}

			if(showGlobal){
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"Global Variables:"}));
				it = global.keySet().iterator();
				while(it.hasNext()){
					var = it.next();
					exVar = global.get(var);
					this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{var, " = ", exVar.getStringValue(), " (" + ((exVar.isNumber)?"float":"string") + ")"}));
				}
				this.getOutput().outputSendMsg("print", Atom.newAtom(new String[]{"------------------"}));
			}
		}		
	}

	@Override
	public void resume(long _timePassed) {
		// TODO Auto-generated method stub
		
	}

}
