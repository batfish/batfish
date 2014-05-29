package batfish.representation;

import java.util.Set;

import batfish.util.Util;

public class PolicyMapMatchAsPathAccessListLine extends PolicyMapMatchLine {

   private Set<AsPathAccessList> _lists;

   public PolicyMapMatchAsPathAccessListLine(Set<AsPathAccessList> lists) {
      _lists = lists;
   }

   public Set<AsPathAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.AS_PATH_ACCESS_LIST;
   }

   @Override
   public boolean sameParseTree(PolicyMapMatchLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapMatchType.AS_PATH_ACCESS_LIST);
      boolean finalRes = res;
      
      if(res == false){
         System.out.println("PoliMapMatchAsPathACLLine:Type "+prefix);
         finalRes = res;
      }
      
      PolicyMapMatchAsPathAccessListLine asLine = (PolicyMapMatchAsPathAccessListLine) line;
      
      if (_lists.size() != asLine._lists.size()) {
         System.out.println("PoliMapMatchAsPathACLLine:Size "+prefix);
         return false;
      }
      else {
         Object[] lhs = _lists.toArray();
         Object[] rhs = asLine._lists.toArray();
         for (int i = 0; i < _lists.size(); i++) {
            if((lhs[i] != null) && (rhs[i] != null)){
               res = ((AsPathAccessList) lhs[i]).getName().equals(((AsPathAccessList) rhs[i]).getName());
            }else{
               res = lhs[i] == rhs[i];
            }   
            if(res == false){
               System.out.println("PoliMapMatchAsPathACLLine "+prefix);
               finalRes = res;
            }
         }
      }
      
      return finalRes;
   }
   
   @Override
   public String getIFString(int indentLevel) {
	   String retString = Util.getIndentString(indentLevel) + "AsPathAccessList";
	   
	   for (AsPathAccessList apal : _lists) {
		   retString += "\n" + apal.getIFString(indentLevel+1);
	   }
		   
	   return retString;
   }
}
