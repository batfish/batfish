package batfish.representation;

import java.io.Serializable;
import java.util.List;

import batfish.util.Util;

public class PolicyMap implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<PolicyMapClause> _clauses;
   private String _mapName;

   public PolicyMap(String name, List<PolicyMapClause> clauses) {
      _mapName = name;
      _clauses = clauses;
   }

   public List<PolicyMapClause> getClauses() {
      return _clauses;
   }

   public String getIFString(int indentLevel) {

      String retString = Util.getIndentString(indentLevel) + "PolicyMap "
            + _mapName;

      for (PolicyMapClause pmc : _clauses) {
         retString += "\n" + pmc.getIFString(indentLevel + 1);
      }

      return retString;
   }

   public String getMapName() {
      return _mapName;
   }

   public boolean sameParseTree(PolicyMap map, String prefix) {
      boolean res = _mapName.equals(map._mapName);
      boolean finalRes = res;

      if (_clauses.size() != map._clauses.size()) {
         System.out.println("PoliMap:Clause:Size " + prefix);
         return false;
      }
      else {
         for (int i = 0; i < _clauses.size(); i++) {
            res = _clauses.get(i).sameParseTree(map._clauses.get(i),
                  "PoliMap:Clause " + prefix);
            if (res == false) {
               finalRes = false;
            }
         }
      }
      return finalRes;

   }

}
