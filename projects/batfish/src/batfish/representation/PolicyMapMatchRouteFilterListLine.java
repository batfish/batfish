package batfish.representation;

import java.util.Set;

import batfish.util.Util;

public class PolicyMapMatchRouteFilterListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<RouteFilterList> _lists;

   public PolicyMapMatchRouteFilterListLine(Set<RouteFilterList> lists) {
      _lists = lists;
   }

   @Override
   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "RouteFilerList";

      for (RouteFilterList rfl : _lists) {
         retString += " " + rfl.getName();
      }

      return retString;
   }

   public Set<RouteFilterList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.ROUTE_FILTER_LIST;
   }

   @Override
   public boolean sameParseTree(PolicyMapMatchLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST);
      boolean finalRes = res;
      if (res == false) {
         System.out.println("PoliMapMatchRouteFilterListLine:Type " + prefix);
         return res;
      }

      PolicyMapMatchRouteFilterListLine rfLine = (PolicyMapMatchRouteFilterListLine) line;

      if (_lists.size() != rfLine._lists.size()) {
         System.out.println("PoliMapMatchRouteFilterListLine:Size " + prefix);
         return false;
      }
      else {
         Object[] lhs = _lists.toArray();
         Object[] rhs = rfLine._lists.toArray();
         for (int i = 0; i < _lists.size(); i++) {
            res = ((RouteFilterList) lhs[i]).getName().equals(
                  ((RouteFilterList) rhs[i]).getName());
            if (res == false) {
               System.out.println("PoliMapMatchRouteFilterListLine " + prefix);
               finalRes = res;
            }
         }
      }

      return finalRes;
   }
}
