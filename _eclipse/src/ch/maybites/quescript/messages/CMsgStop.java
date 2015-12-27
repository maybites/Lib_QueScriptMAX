package ch.maybites.quescript.messages;


public class CMsgStop implements CMsgInterface{
	public static int SHUTDOWN = 1;
	public static int FULLSTOP = 2;
	
	private int stopMode;
	
	public CMsgStop(int _stopMode){
		stopMode = _stopMode;
	}
	
	public boolean isFullStop(){
		return (stopMode == FULLSTOP)? true: false;
	}

	public boolean isShutDown(){
		return (stopMode == SHUTDOWN)? true: false;
	}

	public boolean isTrigger(String _name) {
		return false;
	}

	public boolean isFade(String _name) {
		return false;
	}

	public boolean isAnim(String _name) {
		return false;
	}

	public boolean isStop() {
		return true;
	}

	public boolean isFade() {
		return false;
	}
}
