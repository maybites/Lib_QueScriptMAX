package ch.maybites.mxj.quescript;

public class MutableDouble extends Number {

	private double value;

	public MutableDouble(double value) {
		this.value = value;
	}

	@Override
	public int intValue() {
		// TODO Auto-generated method stub
		return (int)value;
	}

	@Override
	public long longValue() {
		// TODO Auto-generated method stub
		return (long)value;
	}

	@Override
	public float floatValue() {
		// TODO Auto-generated method stub
		return (float)value;
	}

	@Override
	public double doubleValue() {
		// TODO Auto-generated method stub
		return value;
	}
	
	public void setDouble(double value){
		this.value = value;
	}
	
	public String toString(){
		return ""+value;
	}

}
