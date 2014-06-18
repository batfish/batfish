package batfish.representation;

import java.util.ArrayList;

public class AsPath extends ArrayList<AsSet> {

   private static final long serialVersionUID = 1L;

   public String getIFString(String indentString) {

	   String retString = String.format("%s AsPath: ", indentString);
	   
	   for (AsSet asSet : this) {
		   retString += asSet.getIFString("");
	   }
	   
	   return retString;
	   
   }

}
