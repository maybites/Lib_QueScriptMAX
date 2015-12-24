package ch.maybites.mxj.quescript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.maybites.mxj.expression.Expression;
import ch.maybites.mxj.expression.RunTimeEnvironment;
import ch.maybites.mxj.expression.Expression.ExpressionException;
import ch.maybites.mxj.quescript.messages.CMsgShuttle;
import ch.maybites.mxj.quescript.messages.CMsgTime;
import ch.maybites.mxj.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

public abstract class Cmnd implements CmndInterface{

	protected String cmdName = "undefined";
	private ArrayList<String> attrNames;
	private ArrayList<String> childNames;

	private ArrayList<Cmnd> children = new ArrayList<Cmnd>();

	private Hashtable<String, String> attributes;
	
	protected boolean enabled;
	
	CmndInterface parentNode;
	
	private OutputInterface myOutput;

	protected int level;
	
	protected int line = -1;
	
	protected String content;
	
	public String queName;
	
	protected boolean debugMode = false;
		
	public Cmnd(CmndInterface _parentNode){
		parentNode = _parentNode;
		myOutput = parentNode.getOutput();
		enabled = false;
		line = getLine();
		level = (parentNode != null)?parentNode.getLevel() + 1 : 1;
		attributes = new Hashtable<String, String>();
		attrNames = new ArrayList<String>();
		childNames = new ArrayList<String>();
		content = null;
	}
	
	public OutputInterface getOutput(){
		return myOutput;
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
	
	public abstract void parse(Node _xmlNode) throws ScriptMsgException;

	public void parseExpr(RunTimeEnvironment rt)throws ScriptMsgException{
		for(Cmnd child: children){
			child.parseExpr(rt);
		}
	}

	/**
	 * parses the xml node object and fills this object
	 * @param _xmlNode
	 */
	protected void parseRaw(Node _xmlNode) throws ScriptMsgException{
		NamedNodeMap values = _xmlNode.getAttributes();
		
		for(int i = 0; i < attrNames.size(); i++){
			if(values.getNamedItem(attrNames.get(i)) != null){
				attributes.put(attrNames.get(i), values.getNamedItem(attrNames.get(i)).getNodeValue());
			}
		}
		
		NodeList nodeCildren = _xmlNode.getChildNodes();
		
		if(childNames.size() > 0){
			for(int i = 0; i < nodeCildren.getLength(); i++){
				if(nodeCildren.item(i).getNodeType() == 1){
					String childName = nodeCildren.item(i).getNodeName();
					if(childNames.contains(childName)){
						CmndInterface child = null;
						if(childName.equals("wait"))
							child = new CmndWait(this);
						if(childName.equals(CmndMessage.NODE_NAME_SEND))
							child = new CmndMessage(this, CmndMessage.NODE_NAME_SEND);
						if(childName.equals(CmndMessage.NODE_NAME_OSC))
							child = new CmndMessage(this, CmndMessage.NODE_NAME_OSC);
						if(childName.equals(CmndMessage.NODE_NAME_PRINT))
							child = new CmndMessage(this, CmndMessage.NODE_NAME_PRINT);
						if(childName.equals(CmndMessage.NODE_NAME_OUT))
							child = new CmndMessage(this, CmndMessage.NODE_NAME_OUT);
						if(childName.equals(CmndMessage.NODE_NAME_TRIGGER))
							child = new CmndMessage(this, CmndMessage.NODE_NAME_TRIGGER);
						if(childName.equals(CmndAnim.NODE_NAME))
							child = new CmndAnim(this);
						if(childName.equals(CmndTrack.NODE_NAME))
							child = new CmndTrack(this);
						if(childName.equals(CmndExpr.NODE_NAME))
							child = new CmndExpr(this);
						if(childName.equals(CmndTimer.NODE_NAME))
							child = new CmndTimer(this);
						if(childName.equals(CmndWhile.NODE_NAME))
							child = new CmndWhile(this);
						if(childName.equals(CmndIf.NODE_NAME))
							child = new CmndIf(this);
						if(childName.equals(CmndElse.NODE_NAME))
							child = new CmndElse(this);
						if(childName.equals(CmndDebugger.NODE_NAME))
							child = new CmndDebugger(this);
						if(childName.equals("ramp"))
							child = new CmndRamp(this);
						if(childName.equals("break"))
							child = new CmndBreak(this);
						if(childName.equals(CmndFade.NODE_NAME))
							child = new CmndFade(this);
						if(childName.equals(CmndKeys.NODE_NAME))
							child = new CmndKeys(this);
						if(childName.equals("f1"))
							child = new CmndDOUBLE(this, "f1");
						if(childName.equals("f2"))
							child = new CmndDOUBLE(this, "f2");
						if(childName.equals("f3"))
							child = new CmndDOUBLE(this, "f3");
						if(childName.equals("f4"))
							child = new CmndDOUBLE(this, "f4");
						if(childName.equals(CmndInternal.NODE_NAME_STOP))
							child = new CmndInternal(this, CmndInternal.NODE_NAME_STOP);
						if(childName.equals(CmndInternal.NODE_NAME_PLAY))
							child = new CmndInternal(this, CmndInternal.NODE_NAME_PLAY);
						if(childName.equals(CmndInternal.NODE_NAME_PAUSE))
							child = new CmndInternal(this, CmndInternal.NODE_NAME_PAUSE);
						if(childName.equals(CmndInternal.NODE_NAME_RESUME))
							child = new CmndInternal(this, CmndInternal.NODE_NAME_RESUME);
						if(childName.equals(CmndInternal.NODE_NAME_SHUTDOWN))
							child = new CmndInternal(this, CmndInternal.NODE_NAME_SHUTDOWN);
						
						if(child != null){
							child.parse(nodeCildren.item(i));
							children.add((Cmnd)child);
						}
					}else{
						Debugger.verbose("NodeFactory", "found invalid child node: name = '"+nodeCildren.item(i).getNodeName()+"'");			
					}
				}
			}
		} else {
			if(nodeCildren.item(0) != null && nodeCildren.item(0).getNodeType() == 3){
				content = nodeCildren.item(0).getNodeValue();
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
	abstract public void stepper(CMsgShuttle _msg);

	/**
	 * additional method for objects that need to be called even when a lock has been applied
	 */
	abstract public void lockLessStepper(CMsgShuttle _msg);

	
	/**
	 * tells how much time has passed during a pause - resume periode
	 * @param _timePassed time in milliseconds
	 */
	abstract public void resume(long _timePassed);

	protected void removeChild(int _index){
		children.remove(_index).clear();
	}
	
	/**
	 * clears this object of all its children
	 */
	protected void clear(){
		for(Cmnd c : getChildren()){
			c.clear();
		}
		children.clear();
	}
	
	/**
	 * gets all this objects children
	 * @return
	 */
	protected List<Cmnd> getChildren(){
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
	 * tests if the specified name is this objects command name
	 * @param _cmdName
	 * @return
	 */
	protected boolean isCmndName(String _cmdName){
		return cmdName.equals(_cmdName);
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
	protected ArrayList<String> getAttributes(){
		ArrayList<String> atrs = new ArrayList<String>();
		for(Enumeration<String> e = attributes.keys(); e.hasMoreElements();){
			atrs.add(e.nextElement());
		}
		return atrs;
	}
	
	protected void setAttrNames(String[] _attrNames){
		for(int i = 0; i < _attrNames.length; i++)
			attrNames.add(_attrNames[i]);
	}

	protected void setChildNames(String[] _childNames){
		for(int i = 0; i < _childNames.length; i++)
			childNames.add(_childNames[i]);
	}

	/**
	 * tells if this object contains content
	 * @return
	 */
	protected boolean isContentNode(){
		return (content == null)? false: true;
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
	public int getLine(){
		if(cmdName.equals(CmndQue.NODE_NAME)){
			return ++line;
		} else {
			return parentNode.getLine();
		}
	}
	
	public String getAttributeList(){
		StringBuffer list = new StringBuffer();
		for(Enumeration<String> e = attributes.keys(); e.hasMoreElements();){
			list.append("'" + e.nextElement() + "' ");
		}
		return list.toString();
	}
	
	public Cmnd getThis(){
		return this;
	}
	
	public void printStructure(){
		StringBuffer lev = new StringBuffer(level);
		for (int i = 0; i < level * 3; i++){
			lev.append("   ");
		}
		System.out.print(lev + cmdName);
		if(content != null)
			System.out.print(" > '" + content + "'");
		for(Enumeration<String> e = attributes.keys(); e.hasMoreElements();){
			String key = e.nextElement();
			System.out.print(" | " +key+" = " + attributes.get(key));
		}
		System.out.println();
		for(Cmnd child : children){
			child.printStructure();
		}
	}

	
}

