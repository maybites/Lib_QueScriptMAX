import com.cycling74.max.*;
import com.cycling74.jitter.*;
import java.util.*;

import ch.maybites.mxj.utils.pattr.PattrSystem;
import ch.maybites.mxj.utils.pattr.PattrCallback;
import ch.maybites.mxj.utils.pattr.PattrException;
import ch.maybites.mxj.utils.pattr.PattrStore;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.math.la.*;
import ch.maybites.tools.threedee.*;
/**
 * @author Martin Froehlich
 *
 * Max object container for test purposes
 */
public class JPattrStorage extends MaxObject implements PattrCallback{

	private PattrStore storage;
	private String storename;

	private final int PATTR_INLET = 1;
	private final int PATTR_OUTLET = 1;

	public JPattrStorage(Atom args[]){
		if(args.length < 1){
			Debugger.info("JPattrStorage", "no address set. use 'setaddress'. BEWARE: all messages are discarded as long no address is set");
		} else {
			storename = args[0].toString();
		}
		storage = new PattrStore();

		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		createInfoOutlet(false);
	}

	public void notifyDeleted(){
		storage.notifyDeleted();
	}
	
	public void bang(){
		setaddress(storename);
	}
	
	public void setaddress(String address){
		try{
			storage.init(this);
			if(address != null){
				storage.register(address);
				// sets the pattrStorage to send all changes of the clients
				outlet(PATTR_OUTLET, "changemode", Atom.newAtom(1)); 
				outlet(PATTR_OUTLET, "outputmode", Atom.newAtom(2)); 
				// lets the pattrStorage send all the clients names
				outlet(PATTR_OUTLET, "getclientlist"); 		
			}
		}catch(PattrException e){
			error("JPattrStore: Address '"+address+"' already taken.");
		}
	}

	public void setAddressValue(String address, float value){
		this.outlet(PATTR_OUTLET, new Atom[]{Atom.newAtom(address), Atom.newAtom(value)});
		//this.outlet(PATTR_OUTLET, new Atom[]{Atom.newAtom((String)argObjectArray[0]), Atom.newAtom((Float)argObjectArray[1])});
	}

	/**
	 * this method is called for the report of the current client list,
	 * 	cause by the "getclientlist" message inside the bang-method
	 * @param args
	 */
	public void clientlist(Atom[] args){
		if(getInlet() == PATTR_INLET){
			if(args.length == 1){
				if(args[0].toString().equals("done")){
					// lets the pattrStorage send all the clients values
					outlet(PATTR_OUTLET, "dump"); 
				}else
					storage.addClient(args[0].toString());
			}
		}
	}

	/**
	 * This method is called for the report of the current clients values, 
	 * 	caused by the "dump" message inside the clientlist-method
	 */
	public void anything(String message, Atom[] args){
		if(getInlet() == PATTR_INLET){
			if(args.length == 1){
				if(!storage.clientEvent(message, args[0].toFloat())){
					// it must be another message...
				}
			} else {
				message(message, args);
			}
		}
	}

	public void dumpAllValues(){
		outlet(PATTR_OUTLET, "dump"); 
	}
	
	private void message(String message, Atom[] args){
		//post("I received a '" + message + "' message.");
		if (args.length > 0)
		{
			/**post("It has the following arguments: [" + args.length + "]" );
			for (int i=0;i<args.length;i++){
				if(args[i].isFloat())
					post("Float: " + args[i].toFloat());
				if(args[i].isInt())
					post("Int: " + args[i].toInt());
				if(args[i].isFloat())
					post("String: " + args[i].toString());
			}
			**/
		}

	}

}
