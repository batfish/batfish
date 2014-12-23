package batfish.representation;

import java.util.ArrayList;
import java.util.List;

import batfish.util.NamedStructure;
import batfish.util.Util;

/**
 *
 * A data structure that represents a list of prefix with their prefix-length to
 * be matched Used for route filter and prefix list in Juniper JunOS Used for
 * prefix list in Cisco IOS
 *
 */

public class RouteFilterList extends NamedStructure {

   private static final long serialVersionUID = 1L;

   // List of lines that stores the prefix
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

   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "RouteFilerList "
            + getName();

      for (RouteFilterLine rfl : _lines) {
         retString += "\n" + rfl.getIFString(indentLevel + 1);
      }

      return retString;

   }

   public List<RouteFilterLine> getLines() {
      return _lines;
   }

}
