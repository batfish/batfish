package batfish.representation.juniper;

import java.util.List;

public class PolicyStatementMatchIpAccessListLine extends PolicyStatementMatchLine {

   private List<String> _listNames;

   public PolicyStatementMatchIpAccessListLine(List<String> listNames) {
      _listNames = listNames;
   }

   @Override
   public MatchType getType() {
      return MatchType.IP_ACCESS_LIST;
   }

   public List<String> getListNames() {
      return _listNames;
   }
   
}
