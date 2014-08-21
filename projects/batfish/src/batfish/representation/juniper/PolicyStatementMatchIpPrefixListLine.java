package batfish.representation.juniper;

import java.util.List;

public class PolicyStatementMatchIpPrefixListLine extends PolicyStatementMatchLine {

   private static final long serialVersionUID = 1L;
   
   private List<String> _listNames;

   public PolicyStatementMatchIpPrefixListLine(List<String> listNames) {
      _listNames = listNames;
   }

   @Override
   public MatchType getType() {
      return MatchType.ROUTE_FILTER;
   }

   public List<String> getListNames() {
      return _listNames;
   }
   
}
