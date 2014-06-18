package batfish.representation;

import java.util.List;

import batfish.util.Util;

public class PolicyMapSetCommunityLine extends PolicyMapSetLine {

   private List<Long> _communities;

   public PolicyMapSetCommunityLine(List<Long> communities) {
      _communities = communities;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.COMMUNITY;
   }

   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.COMMUNITY);
      boolean finalRes = res;
      if(res == false){
         System.out.println("PoliMapSetCommLine:Type "+prefix);
         return res;
      }
      
      PolicyMapSetCommunityLine commLine = (PolicyMapSetCommunityLine) line;
      if(_communities.size() != commLine._communities.size()){
         System.out.println("PoliMapSetCommLine:Size "+prefix);
         return false;
      }else{
         for(int i=0; i< _communities.size(); i++){
            res = (_communities.get(i).equals(commLine._communities.get(i)));
            if(res == false){
               System.out.println("PoliMapSetCommLine "+prefix);
               finalRes = res;
            }
         }
      }
      
      return finalRes;
   }
   
   @Override
   public String getIFString(int indentLevel) {
	   String retString = Util.getIndentString(indentLevel) + "Community";
	   
	   for (long comm : _communities) {
		   retString += " " + comm;
	   }
	   
	   return retString;
   }

}
