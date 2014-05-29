package batfish.representation;

import java.util.List;

import batfish.util.Util;

public class PolicyMap {
   private String _mapName;
   private List<PolicyMapClause> _clauses;

   public PolicyMap(String name, List<PolicyMapClause> clauses) {
      _mapName = name;
      _clauses = clauses;
   }

   public String getMapName() {
      return _mapName;
   }

   public List<PolicyMapClause> getClauses() {
      return _clauses;
   }

   public String getIFString(int indentLevel) {

	   String retString = Util.getIndentString(indentLevel) + "PolicyMap " + _mapName;

	   for (PolicyMapClause pmc : _clauses) {
		   retString += "\n" + pmc.getIFString(indentLevel + 1);
	   }

	   return retString;
   }

   public boolean sameParseTree(PolicyMap map, String prefix) {
      boolean res = _mapName.equals(map._mapName);
      boolean finalRes = res;

      if (_clauses.size() != map._clauses.size()) {
         System.out.println("PoliMap:Clause:Size "+prefix);
         return false;
      }
      else {
         for (int i = 0; i < _clauses.size(); i++) {
            res = _clauses.get(i).sameParseTree(map._clauses.get(i),"PoliMap:Clause "+prefix);
            if (res == false) {
               finalRes = false;
            }
         }
      }
      return finalRes;

   }

}
