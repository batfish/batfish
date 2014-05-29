package batfish.representation;

import java.util.ArrayList;
import java.util.List;

import batfish.util.NamedStructure;
import batfish.util.Util;

/**
 * 
 * A data structure that represents a list of prefix with their prefix-length to
 * be matched
 * Used for route filter and prefix list in Juniper JunOS
 * Used for prefix list in Cisco IOS
 * 
 */

public class RouteFilterList extends NamedStructure {
	
	//List of lines that stores the prefix
	private List<RouteFilterLine> _lines;

	public RouteFilterList(String name) {
		super(name);
		_lines = new ArrayList<RouteFilterLine>();
	}

	public void addLine(RouteFilterLine r) {
		_lines.add(r);
	}

	public void addLines(List<RouteFilterLine> r) {
		_lines.addAll(r);
	}

	public List<RouteFilterLine> getLines() {
		return _lines;
	}

	public String getIFString(int indentLevel) {
		String retString = Util.getIndentString(indentLevel) + "RouteFilerList " + getName();
		
		for (RouteFilterLine rfl : _lines) {
			retString += "\n" + rfl.getIFString(indentLevel + 1);
		}
		
		return retString;

   }

   public boolean sameParseTree(RouteFilterList list, String prefix) {
      boolean res = _name.equals(list._name);
      boolean finalRes = res;
      
      if(_lines.size() != list._lines.size()){
         System.out.println("RouteFilterLists:Lines:Size "+prefix);
         return false;
      }
      for(int i=0; i < _lines.size(); i++){
         res = _lines.get(i).sameParseTree(list._lines.get(i));
         if(res == false){
            System.out.println("RouteFilterLists:Lines "+prefix);
            finalRes = false;
         }
      }
      return finalRes;
   }

}
