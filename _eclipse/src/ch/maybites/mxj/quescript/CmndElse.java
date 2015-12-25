package ch.maybites.mxj.quescript;

import java.util.Enumeration;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.mxj.expression.Expression;
import ch.maybites.mxj.expression.ExpressionVar;
import ch.maybites.mxj.expression.RunTimeEnvironment;
import ch.maybites.mxj.expression.Expression.ExpressionException;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.CMsgTime;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndElse extends Cmnd {
	protected static String NODE_NAME = "else";
			
	public CmndElse(CmndInterface _parentNode){
		super(_parentNode);
		super.setCmndName(NODE_NAME);
	}

	public void build(Node _xmlNode) throws ScriptMsgException{
		super.build(_xmlNode);
		
		//if there is a nested <anim> node inside this if, it checks if there is another <anim> node
		// further down the tree towards the root
		for(Cmnd child: this.getChildren()){
			if(child instanceof CmndAnim){
				Cmnd parnt = getParent(this);
				while(!(parnt instanceof CmndQue)){
					if(parnt instanceof CmndQue)
						throw new ScriptMsgException("Command <else>: Multiple Nesting of <anim> and <if> nodes are prohibited");
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
	public void setup(RunTimeEnvironment rt)throws ScriptMsgException{
		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created "+cmdName+"-Comnd");
		// Make sure the que- and local- variables are created before the children are parsed
		for(Cmnd child: this.getChildren()){
			child.setup(rt);
		}
	}
	
	@Override
	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bang(CMsgShuttle _msg) {
		for(Cmnd child : getChildren()){
			child.bang(_msg);
		}
	}

	public void lockLessBang(CMsgShuttle _msg) {
		for(Cmnd child : getChildren()){
			child.lockLessBang(_msg);
		}
	}

	@Override
	public void resume(long _timePassed) {
	}

}
