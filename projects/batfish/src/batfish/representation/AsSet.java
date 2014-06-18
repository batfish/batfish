package batfish.representation;

import java.util.LinkedHashSet;

public class AsSet extends LinkedHashSet<Integer> {

   private static final long serialVersionUID = 1L;
   
   public String getIFString(String indentString) {

	   String retString = indentString + "{ ";
	   
	   for (Integer asNum : this) {
		   retString += String.format(" %d ", asNum);
	   }
	   
	   retString += "}";
	   
	   return retString;
   }

}
