package batfish.z3;

import batfish.z3.node.AndExpr;
import batfish.z3.node.ExternalDestinationIpExpr;
import batfish.z3.node.ExternalSourceIpExpr;
import batfish.z3.node.OriginateExpr;
import batfish.z3.node.QueryExpr;
import batfish.z3.node.QueryRelationExpr;
import batfish.z3.node.RoleAcceptExpr;
import batfish.z3.node.RuleExpr;
import batfish.z3.node.SaneExpr;

public class RoleReachabilityQuerySynthesizer implements QuerySynthesizer {

   private String _queryText;

   public RoleReachabilityQuerySynthesizer(String hostname, String role) {
      OriginateExpr originate = new OriginateExpr(hostname);
      RoleAcceptExpr roleAccept = new RoleAcceptExpr(role);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(roleAccept);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      queryConditions.addConjunct(ExternalSourceIpExpr.INSTANCE);
      queryConditions.addConjunct(ExternalDestinationIpExpr.INSTANCE);
      RuleExpr queryRule = new RuleExpr(queryConditions,
            QueryRelationExpr.INSTANCE);
      QueryExpr query = new QueryExpr(QueryRelationExpr.INSTANCE);
      StringBuilder sb = new StringBuilder();
      injectSymbolicPackets.print(sb, 0);
      sb.append("\n");
      queryRule.print(sb, 0);
      sb.append("\n");
      query.print(sb, 0);
      sb.append("\n");
      String queryText = sb.toString();
      _queryText = queryText;
   }

   @Override
   public String getQueryText() {
      return _queryText;
   }

}
