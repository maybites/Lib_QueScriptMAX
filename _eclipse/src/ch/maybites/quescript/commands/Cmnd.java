package ch.maybites.quescript.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.maybites.quescript.OutputInterface;
import ch.maybites.quescript.expression.Expression;
import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.expression.Expression.ExpressionException;
import ch.maybites.quescript.messages.CMsgShuttle;
import ch.maybites.quescript.messages.CMsgTime;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public abstract class Cmnd{

	protected String cmdName = "undefined";

	private ArrayList<Cmnd> children;
	private HashMap<String, String> attributes;

	Cmnd parentNode;

	protected int level;
	protected int line = -1;
	protected String content;
	public String queName;
	
	protected boolean isQue = false;
	protected boolean isScript = false;

	protected boolean debugMode = false;

	public Cmnd(Cmnd _parentNode){
		parentNode = _parentNode;
		line = getLine();
		level = (parentNode != null)?parentNode.getLevel() + 1 : 1;
		attributes = new HashMap<String, String>();
		children = new ArrayList<Cmnd>();
		content = null;
	}

	public OutputInterface getOutput(){
		return parentNode.getOutput();
	}

	public String getQueName(){
		if(queName != null)
			return queName;
		else if(parentNode != null)
			return parentNode.getQueName();
		return "unknown";
	}

	public boolean getDebugMode(){
		if(parentNode != null){
			return parentNode.getDebugMode();
		}
		return debugMode;
	}

	public void setup(RunTimeEnvironment rt)throws ScriptMsgException{
		for(Cmnd child: children){
			child.setup(rt);
		}
	}

	/**
	 * parses the xml node object and fills this object
	 * @param _xmlNode
	 */
	public void build(Node _xmlNode) throws ScriptMsgException{
		NamedNodeMap values = _xmlNode.getAttributes();

		for(int i = 0; i < values.getLength(); i++){
			attributes.put(values.item(i).getNodeName(), values.item(i).getNodeValue());
		}

		NodeList nodeCildren = _xmlNode.getChildNodes();

		if(nodeCildren.getLength() > 0 ){
			for(int i = 0; i < nodeCildren.getLength(); i++){
				if(nodeCildren.item(i).getNodeType() == 1){
					String childName = nodeCildren.item(i).getNodeName();
					Cmnd child = null;
					if(childName.equals(CmndWait.NODE_NAME))
						child = new CmndWait(this);
					else if(childName.equals(CmndQue.NODE_NAME))
						child = new CmndQue(this);
					else if(childName.equals(CmndMessage.NODE_NAME_SEND))
						child = new CmndMessage(this, CmndMessage.NODE_NAME_SEND);
					else if(childName.equals(CmndMessage.NODE_NAME_OSC))
						child = new CmndMessage(this, CmndMessage.NODE_NAME_OSC);
					else if(childName.equals(CmndMessage.NODE_NAME_PRINT))
						child = new CmndMessage(this, CmndMessage.NODE_NAME_PRINT);
					else if(childName.equals(CmndMessage.NODE_NAME_OUT))
						child = new CmndMessage(this, CmndMessage.NODE_NAME_OUT);
					else if(childName.equals(CmndMessage.NODE_NAME_TRIGGER))
						child = new CmndMessage(this, CmndMessage.NODE_NAME_TRIGGER);
					else if(childName.equals(CmndAnim.NODE_NAME))
						child = new CmndAnim(this);
					else if(childName.equals(CmndTrack.NODE_NAME))
						child = new CmndTrack(this);
					else if(childName.equals(CmndExpr.NODE_NAME))
						child = new CmndExpr(this);
					else if(childName.equals(CmndTimer.NODE_NAME))
						child = new CmndTimer(this);
					else if(childName.equals(CmndWhile.NODE_NAME))
						child = new CmndWhile(this);
					else if(childName.equals(CmndIf.NODE_NAME))
						child = new CmndIf(this);
					else if(childName.equals(CmndElse.NODE_NAME))
						child = new CmndElse(this);
					else if(childName.equals(CmndDebugger.NODE_NAME))
						child = new CmndDebugger(this);
					else if(childName.equals("ramp"))
						child = new CmndRamp(this);
					else if(childName.equals(CmndFade.NODE_NAME))
						child = new CmndFade(this);
					else if(childName.equals(CmndKeys.NODE_NAME))
						child = new CmndKeys(this);
					else if(childName.equals("f1"))
						child = new CmndDOUBLE(this, "f1");
					else if(childName.equals("f2"))
						child = new CmndDOUBLE(this, "f2");
					else if(childName.equals("f3"))
						child = new CmndDOUBLE(this, "f3");
					else if(childName.equals("f4"))
						child = new CmndDOUBLE(this, "f4");
					else if(childName.equals(CmndInternal.NODE_NAME_STOP))
						child = new CmndInternal(this, CmndInternal.NODE_NAME_STOP);
					else if(childName.equals(CmndInternal.NODE_NAME_PLAY))
						child = new CmndInternal(this, CmndInternal.NODE_NAME_PLAY);
					else if(childName.equals(CmndInternal.NODE_NAME_PAUSE))
						child = new CmndInternal(this, CmndInternal.NODE_NAME_PAUSE);
					else if(childName.equals(CmndInternal.NODE_NAME_RESUME))
						child = new CmndInternal(this, CmndInternal.NODE_NAME_RESUME);
					else if(childName.equals(CmndInternal.NODE_NAME_SHUTDOWN))
						child = new CmndInternal(this, CmndInternal.NODE_NAME_SHUTDOWN);
					else
						Debugger.verbose("NodeFactory", "found invalid child node: name = '"+childName+"'");			

					if(child != null){
						child.build(nodeCildren.item(i));
						children.add(child);
					}
				} else if (nodeCildren.item(i).getNodeType() == 3){
					content = nodeCildren.item(0).getNodeValue();
				}
			}
		} 
	}

	public void store(Node _parentElement) {
		// TODO Auto-generated method stub

	}

	/**
	 * steps through all the commands and passes on the message container
	 * child classes need to overwrite this message and act according to 
	 * their behavior
	 */
	abstract public void bang(CMsgShuttle _msg);

	/**
	 * additional method for objects that need to be called even when a lock has been applied
	 */
	abstract public void lockLessBang(CMsgShuttle _msg);

	/**
	 * tells how much time has passed during a pause - resume periode
	 * @param _timePassed time in milliseconds
	 */
	abstract public void resume(long _timePassed);

	/**
	 * clears this object of all its children
	 */
	public void clear(){
		for(Cmnd c : getChildren()){
			c.clear();
		}
		children.clear();
		attributes.clear();
	}

	/**
	 * gets all this objects children
	 * @return
	 */
	public List<Cmnd> getChildren(){
		return children;
	}

	/**
	 * sets this object command name
	 * @param _cmdName
	 */
	protected void setCmndName(String _cmdName){
		cmdName = _cmdName;
	}

	/**
	 * tests if the specified name is this objects command name
	 * @param _cmdName
	 * @return
	 */
	protected boolean isCmndName(String _cmdName){
		return cmdName.equals(_cmdName);
	}

	/**
	 * Helper function returns a time object providing an expression. 
	 * 
	 * @param expr
	 * @param rt
	 * @return
	 * @throws ScriptMsgException
	 * @throws ExpressionException
	 */
	protected CMsgTime getAttributeTime(String expr, RunTimeEnvironment rt) throws ScriptMsgException, ExpressionException{
		if(expr.startsWith("{") && expr.endsWith("}")){
			return new CMsgTime(new Expression(expr, "{", "}").parse(rt), 0);
		} else {
			return new CMsgTime(expr);
		}
	}

	/**
	 * get the specified attribute's value
	 * @param _attrName
	 * @return
	 */
	protected String getAttributeValue(String _attrName){
		return attributes.get(_attrName);
	}

	/**
	 * tests if this attribute actually exists
	 * @param _attrName
	 * @return
	 */
	protected boolean hasAttributeValue(String _attrName){
		return attributes.containsKey(_attrName);
	}

	/**
	 * return all this objects attributes
	 * @return
	 */
	protected Set<String> getAttributes(){
		return attributes.keySet();
	}

	/**
	 * get the nested level of this object
	 */
	public int getLevel(){
		return level;
	}

	/**
	 * get the absolute line inside the que of this object
	 * @return
	 */
	private int getLine(){
		if(parentNode == null){
			return ++line;
		} else {
			return parentNode.getLine();
		}
	}

	public String getAttributeList(){
		StringBuffer list = new StringBuffer();
		for(Iterator<String> e = attributes.keySet().iterator(); e.hasNext();){
			list.append("'" + e.next() + "' ");
		}
		return list.toString();
	}

	public void printStructure(){
		StringBuffer lev = new StringBuffer(level);
		for (int i = 0; i < level * 3; i++){
			lev.append("   ");
		}
		System.out.print(lev + cmdName);
		if(content != null)
			System.out.print(" > '" + content + "'");
		for(Iterator<String> e = attributes.keySet().iterator(); e.hasNext();){
			String key = e.next();
			System.out.print(" | " +key+" = " + attributes.get(key));
		}
		System.out.println();
		for(Cmnd child : children){
			child.printStructure();
		}
	}


}

