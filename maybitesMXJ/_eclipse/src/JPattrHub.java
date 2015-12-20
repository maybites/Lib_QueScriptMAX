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
 * @author Martin Fršhlich
 *
 * Max object container for test purposes
 */
public class JPattrHub extends MaxObject implements PattrCallback{

	private PattrStore storage;
	private String storename;

	private final int PATTR_INLET = 1;
	private final int PATTR_OUTLET = 1;

	public JPattrHub(Atom args[]){
		if(args.length < 1){
			this.bail("JPattrStore: requires address");
		}
		storage = new PattrStore();
		storename = args[0].toString();

		declareAttribute("storename", null, "storename");
		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		createInfoOutlet(false);
	}

	public void notifyDeleted(){
		storage.notifyDeleted();
	}

	public void bang(){
		storage.init(this);
		try{
			storage.register(storename);
		}catch(PattrException e){
			error("JPattrHub: Address '"+storename+"' already taken.");
		}
		if(getInlet() == 0){
			// lets the pattrStorage send all the clients names
			outlet(PATTR_OUTLET, "getattributes"); 
		}
	}

	public void setAddressValue(String address, float value){
		this.outlet(PATTR_OUTLET, new Atom[]{Atom.newAtom(address), Atom.newAtom(value)});
		//this.outlet(PATTR_OUTLET, new Atom[]{Atom.newAtom((String)argObjectArray[0]), Atom.newAtom((Float)argObjectArray[1])});
	}


	public void attributes(Atom[] args){
		if(getInlet() == PATTR_INLET){
			for(int i = 0; i < args.length; i++){
				storage.addClient(args[i].toString());			
			}
			notifylisteners();
			// and request all the values of the clients
			outlet(PATTR_OUTLET, "getstate"); 
		}
	}

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

	private void message(String message, Atom[] args){
		post("I received a '" + message + "' message.");
		if (args.length > 0)
		{
			post("It has the following arguments: [" + args.length + "]" );
			for (int i=0;i<args.length;i++){
				if(args[i].isFloat())
					post("Float: " + args[i].toFloat());
				if(args[i].isInt())
					post("Int: " + args[i].toInt());
				if(args[i].isFloat())
					post("String: " + args[i].toString());
			}
		}

	}

	private void notifylisteners(){
	}

	@Override
	public void dumpAllValues() {
		// TODO Auto-generated method stub
		
	}

}
