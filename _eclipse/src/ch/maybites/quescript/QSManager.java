package ch.maybites.quescript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.cycling74.jitter.JitterObject;
import com.cycling74.max.Atom;

import ch.maybites.mxj.utils.JitterObjectArray;
import ch.maybites.quescript.commands.Cmnd;
import ch.maybites.quescript.commands.CmndQue;
import ch.maybites.quescript.commands.CmndScript;
import ch.maybites.quescript.expression.Expression;
import ch.maybites.quescript.expression.RunTimeEnvironment;
import ch.maybites.quescript.messages.CMsgTrigger;
import ch.maybites.quescript.messages.ScriptMsgException;
import ch.maybites.tools.Debugger;

/**
 * <Calibration>
 * 	<Vertice index="1" screenX="12" screenY="4"/>
 * 	<Vertice index="2" screenX="15" screenY="435"/>
 * 	<Vertice index="3" screenX="1" screenY="5"/>
 * 	<Vertice index="67" screenX="152" screenY="95"/>
 * </Calibration>
 * 
 * @author maybites
 *
 */
public class QSManager{
	private static String SCHEMA_FILENAME = "/queListSchema.xsd";
	
	protected static final String ROOT_NODE = "script";
	protected static final String BASE_NODE = "que";

	private Document document;

	private Map<String, CmndQue> scriptNodes;
	private int sceenOffsetVertice;
	
	protected String filepath;

	private JitterObjectArray drawable;
	
	private ArrayList<CMsgTrigger> triggerQueue;
	private ArrayList<CMsgTrigger> triggerQueCopy;
	private OutputInterface myOutput;
	
	Validator validator;
	
	boolean debugMode = false;
	
	boolean autostart = false;

	String fileName;

	RunTimeEnvironment globalExprEnvironment;
	
	CmndScript myScript;
		
	protected QSManager(){
		scriptNodes = new Hashtable<String, CmndQue>();
		
		triggerQueue = new ArrayList<CMsgTrigger>();
		triggerQueCopy = new ArrayList<CMsgTrigger>();

		// creating a validator for validating the script files
		try {
			// create a SchemaFactory capable of understanding WXS schemas
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// load a WXS schema, represented by a Schema instance 
			
			// first locate the schema inside the jar file
			URL locator = this.getClass().getResource(SCHEMA_FILENAME);
			// open an inputstream
			InputStream stream = locator.openStream();
			// and load the schema file
			Source schemaFile = new StreamSource(stream);
	    
	    	Schema schema = factory.newSchema(schemaFile);

	    	// create a Validator instance, which can be used to validate an instance document
	    	validator = schema.newValidator();

	    	// validate the DOM tree
	    	validator.setErrorHandler(new MyErrorHandler());

	    	stream.close();
	    	
	    	globalExprEnvironment = new RunTimeEnvironment();
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		myScript = new CmndScript();
	}
	
	protected void var(String name, double value){
		globalExprEnvironment.setPublicVariable(name, value);
	}
	
	protected void var(String name, String value){
		globalExprEnvironment.setPublicVariable(name, value);
	}
	
	protected void clearGlobalVars(){
		globalExprEnvironment.getPublicVars().clear();
	}
	
	protected void reset(){
		clearGlobalVars();
		stop();
		if(fileName != null)
			load(fileName);

	}
	
	protected void play(String _queName){
		if(scriptNodes.containsKey(_queName)){
			if(debugMode)
				Debugger.verbose("QueScript", "play... :" + _queName);	
			scriptNodes.get(_queName).play(debugMode);
			if(debugMode)
				Debugger.verbose("QueScript", "... play :" + _queName);	
		}
	}
	
	/**
	 * ShutDown all ques
	 */
	protected void shutdown(){
		if(debugMode)
			Debugger.verbose("QueScript", "shuting down... : all");	
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			e.next().shutDown();
		}
		if(debugMode)
			Debugger.verbose("QueScript", "... shut all down");	
	}

	/**
	 * shutdown only specified que
	 * @param _exception
	 */
	protected void shutdown(String _name){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(_next.queName.equals(_name)){
				if(debugMode)
					Debugger.verbose("QueScript", "shutDown... : " + _name);	
				_next.shutDown();
				if(debugMode)
					Debugger.verbose("QueScript", ".... shutDown : " + _name);	
			}
		}
	}

	/**
	 * shutdown all ques except the specified
	 * @param _exceptionName
	 */
	protected void shutDownExcept(String _exceptionName){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(!_next.queName.equals(_exceptionName)){
				if(debugMode)
					Debugger.verbose("QueScript", "shutDownExcept.... : " + _exceptionName);	
				_next.shutDown();
				if(debugMode)
					Debugger.verbose("QueScript", ".... shutDownExcept: " + _exceptionName);	
			}
		}
	}

	/**
	 * resume all executed que's
	 */
	protected void resume(){
		if(debugMode)
			Debugger.verbose("QueScript", "resume all executed que's");	
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			e.next().resume();
		}
		if(debugMode)
			Debugger.verbose("QueScript", "... all paused que's are resumed");	
	}

	/**
	 * resume specified executed que
	 * @param _name
	 */
	protected void resume(String _name){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(_next.queName.equals(_name)){
				if(debugMode)
					Debugger.verbose("QueScript", "resume paused que: " + _name);	
				_next.resume();
				if(debugMode)
					Debugger.verbose("QueScript", "... paused que is resumed playing: " + _name);	
			}
		}
	}

	/**
	 * pause all executed que's
	 */
	protected void pause(){
		if(debugMode)
			Debugger.verbose("QueScript", "pause all executed que's");	
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			e.next().pause();
		}
		if(debugMode)
			Debugger.verbose("QueScript", "... all executed que's are paused");	
	}

	/**
	 * pause specified executed que
	 * @param _name
	 */
	protected void pause(String _name){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(_next.queName.equals(_name)){
				if(debugMode)
					Debugger.verbose("QueScript", "pause executed que: " + _name);	
				_next.pause();
				if(debugMode)
					Debugger.verbose("QueScript", "... executed que is paused: " + _name);	
			}
		}
	}

	/**
	 * Pause all que's except the specified
	 * @param _exceptionName
	 */
	protected void pauseExcept(String _exceptionName){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(!_next.queName.equals(_exceptionName)){
				if(debugMode)
					Debugger.verbose("QueScript", "pause all, except.... : " + _exceptionName);	
				_next.pause();
				if(debugMode)
					Debugger.verbose("QueScript", "... paused all, except: " + _exceptionName);	
			}
		}
	}

	/**
	 * Stop all ques
	 */
	protected void stop(){
		if(debugMode)
			Debugger.verbose("QueScript", "stoping all...");	
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			e.next().stop();
		}
		if(debugMode)
			Debugger.verbose("QueScript", "... all stopped");	
	}

	/**
	 * Stops only specified que
	 * @param _exception
	 */
	protected void stop(String _name){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(_next.queName.equals(_name)){
				if(debugMode)
					Debugger.verbose("QueScript", "stopping... : " + _name);	
				_next.stop();
				if(debugMode)
					Debugger.verbose("QueScript", "...stopped: " + _name);	
			}
		}
	}

	/**
	 * Stops all ques except the specified
	 * @param _exceptionName
	 */
	protected void stopExcept(String _exceptionName){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue _next = e.next();
			if(!_next.queName.equals(_exceptionName)){
				if(debugMode)
					Debugger.verbose("QueScript", "stopExcept.... : " + _exceptionName);	
				_next.stop();
				if(debugMode)
					Debugger.verbose("QueScript", "... stopped Except: " + _exceptionName);	
			}
		}
	}

	protected void bang(){	
		Calendar md = Calendar.getInstance();
		globalExprEnvironment.setPublicVariable("$HOUR", md.get(Calendar.HOUR_OF_DAY));
		globalExprEnvironment.setPublicVariable("$MIN", md.get(Calendar.MINUTE));
		globalExprEnvironment.setPublicVariable("$SEC", md.get(Calendar.SECOND));
		globalExprEnvironment.setPublicVariable("$MILLI", md.get(Calendar.MILLISECOND));

		// all the que's receive a bang message, since some of them might still be in shutdown mode
		CmndQue nextElement;
		
		// make sure that no concurrent triggers get lost.
		triggerQueCopy = triggerQueue;
		if(triggerQueue.size() > 0)
			triggerQueue = new ArrayList<CMsgTrigger>();

		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			nextElement = e.next();
//			Debugger.verbose("QueScript", "banging...: " + nextElement.getQueName());	
			nextElement.bang(triggerQueCopy);
//			Debugger.verbose("QueScript", "... banged: " + nextElement.getQueName());	
		}
		triggerQueue.clear();
	}
	
	protected void autostart(int _autostart){
		autostart = (_autostart == 1)? true: false;
	}
	
	protected void trigger(CMsgTrigger _trigger){
		triggerQueue.add(_trigger);
	}
		
	/**
	 * loads a script file and returns the que's names it contains
	 * @param _filepath
	 */
	protected void load(String _filepath){
		// before loading the new ques, all ques that are not playing will be removed
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			CmndQue que = e.next();
			if(!que.isPlaying){
				que.clear();
				e.remove();
			}
		}
		myScript.clear();
				
		String  firstQueName = null;
		
		File dieXMLDatei = new File(_filepath);
				
		try {
			DocumentBuilderFactory dasDBFactoryObjekt = DocumentBuilderFactory.newInstance();
			DocumentBuilder dasDBObjekt = dasDBFactoryObjekt.newDocumentBuilder();
			document = dasDBObjekt.parse(dieXMLDatei);

		    // preparing the XML file as a SAX source
		    SAXSource source = new SAXSource(new InputSource(new java.io.FileInputStream(_filepath)));

		    validator.validate(source);

			filepath = _filepath;
			
			document.getDocumentElement().normalize();	
						
			myScript.build(document.getFirstChild());
			myScript.setup(globalExprEnvironment);
			
			for(Iterator<Cmnd> q = myScript.getChildren().iterator(); q.hasNext();){
				CmndQue que = (CmndQue) q.next();
				scriptNodes.put(que.getQueName(), que);
				if(firstQueName == null)
					firstQueName = que.getQueName();
				q.remove();
			}
								
			Debugger.info("QueScript", "loaded " +_filepath + " with " + scriptNodes.size() + " que's");

			getOutput().outputInfoMsg("parsing", new Atom[]{Atom.newAtom("ok")});

		} catch (SAXParseException e) {
			Debugger.error("QueScript", "Error at line[" + e.getLineNumber() + 
					"] col[" + e.getColumnNumber() + "]: " + 
					e.getMessage().substring(e.getMessage().indexOf(":")+1));
			getOutput().outputInfoMsg("parsing", new Atom[]{Atom.newAtom("error"), Atom.newAtom("line"), Atom.newAtom("line[" + e.getLineNumber() + "] col[" + e.getColumnNumber() + "]")});
			return;

		} catch (ScriptMsgException e) {
			Debugger.error("QueScript", "Error: " + e.getMessage());
			getOutput().outputInfoMsg("parsing", new Atom[]{Atom.newAtom("error"), Atom.newAtom("unknown"), Atom.newAtom( e.getMessage())});
			return;

		} catch (Exception e) {
			Debugger.error("QueScript", "DocumentBuilder Exceptions:" + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// if autostart is selected, play the first que of the new loaded file
		if(autostart){
			play(firstQueName);
		}
		
		getOutput().outputDumpMsg("quelist", null);
		for(Iterator<String> e = scriptNodes.keySet().iterator(); e.hasNext();){
			getOutput().outputDumpMsg("que", Atom.newAtom(new String[]{e.next()}));			
		}
		getOutput().outputDumpMsg("quelistdone", null);

		fileName = _filepath;
	}
	
	public int getLevel() {
		return 0;
	}

	public void printStructure(){
		for(Iterator<CmndQue> e = scriptNodes.values().iterator(); e.hasNext();){
			e.next().printStructure();
		}
	}

	protected void notifyDeleted(){
	}

	public static void main(String[] args) {
		
		QSManager obj = new QSManager();

//		obj.load("/Users/maybites/Arbeiten/01_projekte/150816_SPARCK/01_dev/_projects/sparck/_scripts/_cuelists/listA.xml", 
//				"/Users/maybites/Arbeiten/01_projekte/150816_SPARCK/01_dev/_projects/sparck/_scripts/_cuelists/schema.xsd");
		
		obj.printStructure();
		
		//obj.saveas("/Users/maybites/Arbeiten/02_code/eclipse/git/Prj_BeamStreamer/Prj_BeamStreamer/extDev/data/projects/scripts/_cuelists/listSave.yml");
		
		/* multiplying matrices */

	}
	
	private static class MyErrorHandler extends DefaultHandler {
		public void warning(SAXParseException e) throws SAXException {
			printInfo(e);
		}

		public void error(SAXParseException e) throws SAXException {
			printInfo(e);
		}

		public void fatalError(SAXParseException e) throws SAXException {
			printInfo(e);
		}

		private void printInfo(SAXParseException e) {
			Debugger.error("QueScript", "Error at line(" + e.getLineNumber() + ") col(" + e.getColumnNumber() + 
					"): " + e.getMessage().substring(e.getMessage().indexOf(":")+2));
		}
	}

	public void setOutput(OutputInterface _output){
		myScript.setOutput(_output);
		myOutput = _output;
	}

	public int getLine() {
		return -1;
	}
	
	public void setDebug(int _debug){
		debugMode = (_debug == 1)? true: false;
	}

	public boolean getDebugMode() {
		return debugMode;
	}

	public OutputInterface getOutput() {
		return myOutput;
	}
}
