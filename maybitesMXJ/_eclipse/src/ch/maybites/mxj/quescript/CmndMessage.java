package ch.maybites.mxj.quescript;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.cycling74.max.Atom;

import ch.maybites.mxj.expression.Expression;
import ch.maybites.mxj.expression.ExpressionVar;
import ch.maybites.mxj.expression.RunTimeEnvironment;
import ch.maybites.mxj.expression.Expression.ExpressionException;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public class CmndMessage extends Cmnd {
	public static String NODE_NAME_SEND 	= "send";
	public static String NODE_NAME_PRINT 	= "print";
	public static String NODE_NAME_OUT 		= "out";
	public static String NODE_NAME_TRIGGER 	= "trigger";
	public static String NODE_NAME_OSC 		= "osc";

	Atom[] myMsg; 
	
	ArrayList<String> myVars;
	
	// TODO REMOVE ONCE Ramp is obsolete
	// stores the message index in which an interpolator value needs to be set
	int[] trackValueMsgArrayIndices;	
	// the reference to the interpolator value
	ExpressionVar[] trackValueInterpolators;
	
	// stores the message index in which an expression result needs to be set
	int[] exprValueMsgArrayIndices;	
	// the reference to the expression
	Expression[] expressions;
	ExpressionVar[] expressionResults;
	
	public CmndMessage(CmndInterface _parentNode, String _cmdName){
		super(_parentNode);
		super.setCmndName(_cmdName);
		super.setAttrNames(new String[]{"remote", "local"});
		super.setChildNames(new String[]{});
	}

	public void parse(Node _xmlNode) throws ScriptMsgException{
		super.parseRaw(_xmlNode);
		
		myVars = new ArrayList<String>();
		
		parseContentString(super.content);
	}
	
	private void parseContentString(String _content) throws ScriptMsgException{
		List<String> segmts = new ArrayList<String>();
		Matcher m = Pattern.compile("([^{]\\S*|.+?[{}])\\s*").matcher(_content);
		while (m.find())
			segmts.add(m.group(1).trim()); // Add .replace("\"", "") to remove surrounding quotes.

		ArrayList<Atom> atoms = new ArrayList<Atom>();
		ArrayList<Integer> trackValueIndices = new ArrayList<Integer>();
		ArrayList<Integer> exprValueIndices = new ArrayList<Integer>();
		boolean isNumber = false;
		for(int i = 0; i < segmts.size(); i++){
			Atom seg = null;
			// first try int
			if(seg == null){
				try{
					seg = Atom.newAtom(Integer.parseInt(segmts.get(i)));
					isNumber = true;
				} catch (NumberFormatException e){;}
			} 
			if(seg == null){
				try{
					seg = Atom.newAtom(Float.parseFloat(segmts.get(i)));
					isNumber = true;
				} catch (NumberFormatException e){;}
			}
			if(seg == null){
				seg = Atom.newAtom(segmts.get(i));
				//----> DEPRECATED
				// TODO REMOVE ONCE Ramp is obsolete
				if(segmts.get(i).startsWith("$")){
					//stores the index inside the message array in which a track interpolation
					// value waits for evaluation
					trackValueIndices.add(i);
					// <---- DEPRECATED
				} else if(segmts.get(i).startsWith("{") && segmts.get(i).endsWith("}")){
					//stores the index inside the message array in which an expression
					// waits for evaluation
					exprValueIndices.add(i);
					// removes the { and } at the beginning and the end
					seg = Atom.newAtom(segmts.get(i).substring(1, segmts.get(i).length() - 1));
				}
			}
			atoms.add(seg);
			
			isNumber = false;
		}
		Atom[] atomlist = new Atom[atoms.size()];
		myMsg =  atoms.toArray(atomlist);

		//----> DEPRECATED
		// TODO REMOVE ONCE Ramp is obsolete
		
		// stores the track interpolation references
		Integer[] integerIndexs = new Integer[trackValueIndices.size()];
		trackValueMsgArrayIndices = new int[trackValueIndices.size()];
		trackValueInterpolators = new ExpressionVar[trackValueIndices.size()];
		
		trackValueIndices.toArray(integerIndexs);
		for(int i = 0; i < integerIndexs.length; i++){
			trackValueMsgArrayIndices[i] = integerIndexs[i].intValue();
		}

		// <---- DEPRECATED
		
		// stores the expression references
		integerIndexs = new Integer[exprValueIndices.size()];
		exprValueMsgArrayIndices = new int[exprValueIndices.size()];
		expressions = new Expression[exprValueIndices.size()];
		expressionResults = new ExpressionVar[exprValueIndices.size()];
		
		exprValueIndices.toArray(integerIndexs);
		for(int i = 0; i < integerIndexs.length; i++){
			exprValueMsgArrayIndices[i] = integerIndexs[i].intValue();
			try {
				expressions[i] = new Expression(myMsg[exprValueMsgArrayIndices[i]].getString());
			} catch (ExpressionException e) {
				throw new ScriptMsgException("<"+cmdName+">: "+e.getMessage());
			}
		}
	}
	
	/**
	 * Parse the Expressions with the RuntimeEnvironement
	 */
	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
		try {
			// go through all the found expressions
			for(int i = 0; i < expressions.length; i++){
				expressionResults[i] = expressions[i].parse(rt);
			}
		} catch (ExpressionException e) {
			throw new ScriptMsgException("<que name=\""+parentNode.getQueName()+"\"> <send>: "+e.getMessage());
		}
		
		if(getDebugMode())
			Debugger.verbose("QueScript - NodeFactory", "que("+parentNode.getQueName()+") "+new String(new char[getLevel()]).replace('\0', '_')+" created "+cmdName+"-Comnd = '"+super.content+"'");			

		// and then do it for all the children
		for(Cmnd child: this.getChildren()){
			child.parseExpr(rt);
		}
	}

	//----> DEPRECATED
	// TODO REMOVE ONCE Ramp is obsolete
	/**
	 * register the Double objects values
	 * @param _trackInterpolators
	 */
	public void registerDouble(Hashtable<String, CmndDOUBLE> _doubleInterpolators){
		for(int i = 0; i < trackValueInterpolators.length; i++){
			CmndDOUBLE dbl = _doubleInterpolators.get(myMsg[trackValueMsgArrayIndices[i]].getString());
			if(dbl != null)
				trackValueInterpolators[i] = dbl.getValueObject();
		}
	}
	// <---- DEPRECATED
		
	/**
	 * Sends the message after it takes all the interpolated values.
	 */
	public void lockLessStepper(CMsgShuttle _msg){
		//----> DEPRECATED
		// TODO REMOVE ONCE Ramp is obsolete
		for(int i = 0; i < trackValueInterpolators.length; i++)
			myMsg[trackValueMsgArrayIndices[i]] = Atom.newAtom(trackValueInterpolators[i].getNumberValue());
		// <---- DEPRECATED
		
		for(int i = 0; i < exprValueMsgArrayIndices.length; i++){
			try {
				expressionResults[i].eval();
				if(expressionResults[i].isNumber)
					myMsg[exprValueMsgArrayIndices[i]] = Atom.newAtom(expressionResults[i].getNumberValue());
				else
					myMsg[exprValueMsgArrayIndices[i]] = Atom.newAtom(expressionResults[i].getStringValue());
			} catch (ExpressionException e) {
				Debugger.error("Script - Command <" + cmdName +">", "expression evaluation error: " + e.getMessage());			
			}
		}

		this.getOutput().outputSendMsg(cmdName, myMsg);
	}
	
	public void store(Node _parentElement) {
	}
	
	public void stepper(CMsgShuttle _msg) {
		if(!_msg.isWaitLocked()){
			lockLessStepper(_msg);
			if(cmdName.equals(NODE_NAME_TRIGGER)){
				sendInternalMessage();
			}
		}
	}
	
	private void sendInternalMessage(){
		String[] msg = Atom.toString(myMsg);
		String[] cmnd = new String[msg.length + 2];
		cmnd[0] = cmdName;
		cmnd[1] = parentNode.getQueName();
		for(int i = 0; i < msg.length; i++){
			cmnd[i + 2] = msg[i];
		}
		this.getOutput().outputSelfCommand(cmnd);
	}

	public void resume(long _timePassed) {
	}

}
