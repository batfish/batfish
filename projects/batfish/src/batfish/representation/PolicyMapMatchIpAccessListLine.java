package batfish.representation;

import java.util.Set;

import batfish.util.Util;

public class PolicyMapMatchIpAccessListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<IpAccessList> _lists;

   public PolicyMapMatchIpAccessListLine(Set<IpAccessList> lists) {
      _lists = lists;
      if (_lists.contains(null)) {
         throw new Error("null list");
      }
   }

   @Override
   public String getIFString(int indentLevel) {

      String retString = Util.getIndentString(indentLevel) + "IpAccessList";

      for (IpAccessList ipal : _lists) {
         retString += "\n" + ipal.getIFString(indentLevel + 1);
      }

      return retString;
   }

   public Set<IpAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.IP_ACCESS_LIST;
   }

   @Override
   public boolean sameParseTree(PolicyMapMatchLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapMatchType.IP_ACCESS_LIST);
      boolean finalRes = res;

      if (res == false) {
         System.out.println("PoliMapMatchIpACLLine:Type " + prefix);
         finalRes = res;
      }

      PolicyMapMatchIpAccessListLine ipLine = (PolicyMapMatchIpAccessListLine) line;

      if (_lists.size() != ipLine._lists.size()) {
         System.out.println("PoliMapMatchIpACLLine:Size " + prefix);
         return false;
      }
      else {
         for (IpAccessList lhs : _lists) {
            boolean found = false;
            for (IpAccessList rhs : ipLine._lists) {
               if (lhs.sameParseTree(rhs, prefix, false)) {
                  found = true;
                  break;
               }
            }
            if (found == false) {
               System.out.println("PoliMapMatchIpACLLine " + prefix);
               finalRes = res;
            }
         }
         /*
          * Object[] lhs = _lists.toArray(); Object[] rhs =
          * ipLine._lists.toArray(); for (int i = 0; i < _lists.size(); i++) {
          * if((lhs[i] != null) && (rhs[i] != null)){ res = res &&
          * ((IpAccessList) lhs[i]).getName().equals(((IpAccessList)
          * rhs[i]).getName()); }else{ res = res && lhs[i] == rhs[i]; } if(res
          * == false){ System.out.print("PoliMapMatchIpACLLine "); return res; }
          * }
          */
      }

      return finalRes;
   }

}
