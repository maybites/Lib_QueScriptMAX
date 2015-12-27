package ch.maybites.quescript;

import com.cycling74.max.Atom;

public interface OutputInterface {

	public void outputSendMsg(String _msg, Atom[] _vals);

	public void outputTriggerMsg(Atom[] _vals);
	
	public void outputInfoMsg(String _msg, Atom[] _vals);

	public void outputDumpMsg(String _msg, Atom[] _vals);

	public void outputSelfCommand(String[] _comnd);

}
