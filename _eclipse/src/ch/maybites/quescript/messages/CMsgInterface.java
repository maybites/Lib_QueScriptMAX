package ch.maybites.quescript.messages;

public interface CMsgInterface {

	public boolean isTrigger(String _name);

	public boolean isFade(String _name);
	
	public boolean isFade();
	
	public boolean isAnim(String _name);
	
	public boolean isStop();

}
