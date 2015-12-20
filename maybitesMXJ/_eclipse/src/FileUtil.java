import com.cycling74.max.*;

import java.io.File;
import java.util.*;

/**
 * @author Martin Fršhlich
 *
 * Max object container for test purposes
 */
public class FileUtil extends MaxObject{

	public FileUtil(Atom args[]){
		declareInlets(new int[]{ DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL});
		createInfoOutlet(true);
	}

	public void mkdir(String foldername){
		boolean success = false;
		
		File newFolder = new File(foldername);
		
		try{
			success = newFolder.mkdir();
		} catch (SecurityException Se){
			this.error("Error creating Folder: '" + foldername + "' -> " + Se.toString());
		}
		
		if(success)
			outlet(1, "done", "mkdir");
		else if(!success)
			outlet(1, "error", "mkdir");
	}
	
	public void mkdirs(String foldername){
		boolean success = false;
		
		File newFolder = new File(foldername);
		
		try{
			success = newFolder.mkdirs();
		} catch (SecurityException Se){
			this.error("Error creating Folder: '" + foldername + "' -> " + Se.toString());
		}
		
		if(success)
			outlet(1, "done", "mkdirs");
		else if(!success)
			outlet(1, "error", "mkdirs");
	}
}
