package batfish.representation;

import java.util.List;

import batfish.util.NamedStructure;
import batfish.util.Util;

public class IpAccessList extends NamedStructure {
   private List<IpAccessListLine> _lines;

   public IpAccessList(String name, List<IpAccessListLine> lines) {
      super(name);
      _lines = lines;
   }

   public List<IpAccessListLine> getLines() {
      return _lines;
   }

   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _name;
      for (IpAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }

   public String getIFString(int indentLevel) {

	   String retString = Util.getIndentString(indentLevel) + "IpAccessList " + getName();
	   
	   for (IpAccessListLine ipall : _lines) {
		   retString += "\n" + ipall.getIFString(indentLevel + 1);
	   }
	   
	   return retString;
   }

   public boolean sameParseTree(IpAccessList list, String prefix,
         boolean display) {
      boolean res = true;
      boolean finalRes = res;

      if (_lines.size() != list._lines.size()) {
         if (display) {
            System.out.println("IpAccessList:Lines:Size "+prefix);
         }
         return false;
      }
      else {
         for (int i = 0; i < _lines.size(); i++) {
            res = _lines.get(i).toString()
                  .equals(list._lines.get(i).toString());
            if (res == false) {
               if (display) {
                  System.out.println("IpAccessList:Lines "+prefix);
               }
               finalRes = res;
            }
         }
      }
      return finalRes;
   }

}
