package org.batfish.z3;

import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.ExternalDestinationIpExpr;
import org.batfish.z3.node.ExternalSourceIpExpr;
import org.batfish.z3.node.NodeTransitExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RoleOriginateExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

public class RoleTransitQuerySynthesizer implements QuerySynthesizer {

   private String _queryText;

   public RoleTransitQuerySynthesizer(String sourceRole, String transitNode) {
      RoleOriginateExpr roleOriginate = new RoleOriginateExpr(sourceRole);
      NodeTransitExpr nodeTransit = new NodeTransitExpr(transitNode);
      RuleExpr injectSymbolicPackets = new RuleExpr(roleOriginate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(nodeTransit);
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
