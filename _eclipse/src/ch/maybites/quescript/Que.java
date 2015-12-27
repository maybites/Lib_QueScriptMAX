package ch.maybites.quescript;

import java.util.ArrayList;
import java.util.Hashtable;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import ch.maybites.quescript.commands.CmndInternal;
import ch.maybites.quescript.commands.CmndMessage;
import ch.maybites.quescript.messages.CMsgTrigger;
import ch.maybites.tools.Debugger;

public class Que extends MaxObject implements OutputInterface{
	
	final int OUTLET_SEND = 0;
	final int OUTLET_TRIGGER = 1;
	final int OUTLET_INFO = 2;
	final int OUTLET_DUMP = 3;

	QSManager queManager;
	
	ArrayList<String[]> selfCommands;
	
	int viewplayingquesFreq = 0;
	
	long lastviewTime = 0;
		
	public Que(Atom[] _args){
		
		declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

		queManager = new QSManager();
		selfCommands = new ArrayList<String[]>();
	}
	
	/**
	 * read script file
	 * @param _fileName
	 */
	public void read(String _fileName){
		// pass on the scriptnode my instance so all child nodes 
		// have a way to interface with me.
		queManager.setOutput(this);
		
		queManager.load(_fileName);
	}
	
	/**
	 * create next frame
	 */
	public void bang(){
		// first execute self inflicted commands (the script calls itself)
		if(selfCommands.size() > 0){
			ArrayList<String[]> copyCommands = new ArrayList<String[]>();
			for(String[] cmd: selfCommands){
				copyCommands.add(cmd);
			}
			// then clear all the commands
			selfCommands.clear();
			for(String[] cmd: copyCommands){
				if(cmd[0].equals(CmndInternal.NODE_NAME_PLAY)){
					queManager.play(cmd[2]);
				}else if(cmd[0].equals(CmndInternal.NODE_NAME_STOP)){
					if(cmd[2] == null){ // no name attribute was set at the stop message
						queManager.stopExcept(cmd[1]);
					} else { // there was a name attribute
						queManager.stop(cmd[2]);
					}
				}else if(cmd[0].equals(CmndInternal.NODE_NAME_SHUTDOWN)){
					if(cmd[2] == null){ // no name attribute was set at the shutdown message
						queManager.shutDownExcept(cmd[1]);
					} else { // there was a name attribute
						queManager.shutdown(cmd[2]);
					}
				}else if(cmd[0].equals(CmndInternal.NODE_NAME_PAUSE)){
					if(cmd[2] == null){ // no name attribute was set at the pause message
						queManager.pauseExcept(cmd[1]);
					} else { // there was a name attribute
						queManager.pause(cmd[2]);
					}
				}else if(cmd[0].equals(CmndInternal.NODE_NAME_RESUME)){
					if(cmd[2] == null){ // no name attribute was set at the play message
						queManager.resume();
					} else { // there was a name attribute
						queManager.resume(cmd[2]);
					}
				}else if(cmd[0].equals(CmndMessage.NODE_NAME_TRIGGER)){
					if(cmd.length == 3){ // 
						trigger(cmd[2], null);
					} else if(cmd.length > 3){
						Atom[] args = new Atom[cmd.length - 3];
						for(int i = 3; i < cmd.length; i++){
							args[i - 3] = Atom.newAtom(cmd[i]);
						}
						trigger(cmd[2], args);
					}
				}
			}
		}
		
		long timer = System.currentTimeMillis();
		// and then keep on going
		queManager.bang();
		
		if(viewplayingquesFreq > 0 && lastviewTime + (1000 / viewplayingquesFreq) < timer ){
			lastviewTime = timer;
			outlet(OUTLET_INFO, "playtime", System.currentTimeMillis() - timer);
		}
	}
	
	/**
	 * set global variable
	 * @param name
	 * @param val
	 */
	public void var(String name, float val){
		queManager.var(name, val);
	}
	
	/**
	 * set global variable
	 * @param name
	 * @param val
	 */
	public void var(String name, String val){
		queManager.var(name, val);
	}
	
	/**
	 * autostart = 1 will play the first que of the script upon loading the script
	 * @param _autostart
	 */
	public void autostart(int _autostart){
		queManager.autostart(_autostart);
	}

	/**
	 * clears all global Variables, stops all que's and reloads the script
	 */
	public void reset(){
		queManager.reset();
	}

	/**
	 * Start the output of information about the currently playing que's, including the time each
	 * frame takes in milliseconds.
	 * @param _frequency the number of updates per second.
	 */
	public void viewplayingques(int _frequency){
		viewplayingquesFreq = _frequency;
	}
	
	/**
	 * Start playing specified que name
	 * @param queName
	 */
	public void play(String queName){
		queManager.play(queName);
	}

	/**
	 * trigger message
	 * @param _args list
	 */
	public void trigger(Atom[] _args){
		Atom[] args = new Atom[_args.length - 1];
		for(int i = 1; i < _args.length; i++){
			args[i - 1] = _args[i];
		}		
		trigger(_args[0].toString(), args);
	}

	/**
	 * trigger message
	 * @param _triggerName string
	 */
	public void trigger(String _triggerName){
		trigger(_triggerName, null);
	}

	/**
	 * trigger message 
	 * @param _triggerName
	 * @param args
	 */
	public void trigger(String _triggerName, Atom[] args){
		queManager.trigger(new CMsgTrigger(_triggerName, args));
	}
		
	/**
	 * stops all running que's
	 */
	public void stop(){
		queManager.stop();
	}
	
	/**
	 * stops specified que
	 * @param queName
	 */
	public void stop(String queName){
		queManager.stop(queName);
	}
	
	/**
	 * resumes playing all paused que's
	 */
	public void resume(){
		queManager.resume();
	}
	
	/**
	 * resumes playing specified que
	 * @param queName
	 */
	public void resume(String queName){
		queManager.resume(queName);
	}

	/**
	 * pause all running que's
	 */
	public void pause(){
		queManager.pause();
	}
	
	/**
	 * pause specified que
	 * @param queName
	 */
	public void pause(String queName){
		queManager.pause(queName);
	}

	/**
	 * shutdown all que's
	 */
	public void shutdown(){
		queManager.shutdown();
	}
	
	/**
	 * Shutdown specified que
	 * @param queName
	 */
	public void shutdown(String queName){
		queManager.shutdown(queName);
	}

	/**
	 * switch debug mode
	 * @param _debug 0 = off, 1 = on
	 */
	public void debug(int _debug){
		queManager.setDebug(_debug);
	}
	
	/**
	 * Debugger level 
	 * @param _level (verbose, debug, info, warning, error, fatal)
	 */
	public void java_debug(String _level){
		if(_level.equals("verbose"))
			Debugger.setLevelToVerbose();
		else if(_level.equals("debug"))
			Debugger.setLevelToDebug();
		else if(_level.equals("info"))
			Debugger.setLevelToInfo();
		else if(_level.equals("warning"))
			Debugger.setLevelToWarning();
		else if(_level.equals("error"))
			Debugger.setLevelToError();
		else if(_level.equals("fatal"))
			Debugger.setLevelToFatal();
	}

	public void outputSendMsg(String _msg, Atom[] _vals) {
		if(_vals != null)
			outlet(OUTLET_SEND, _msg, _vals);
		else
			outlet(OUTLET_SEND, _msg);
	}

	public void outputTriggerMsg(Atom[] _vals) {
		outlet(OUTLET_TRIGGER, _vals);
	}

	public void outputInfoMsg(String _msg, Atom[] _vals) {
		if(_vals != null)
			outlet(OUTLET_INFO, _msg, _vals);
		else
			outlet(OUTLET_INFO, _msg);
	}
	
	public void outputDumpMsg(String _msg, Atom[] _vals) {
		if(_vals != null)
			outlet(OUTLET_DUMP, _msg, _vals);
		else
			outlet(OUTLET_DUMP, _msg);
	}
	
	public void outputSelfCommand(String[] _comnd) {
		selfCommands.add(_comnd);
	}

}
