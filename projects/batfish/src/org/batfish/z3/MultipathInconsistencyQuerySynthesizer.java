package org.batfish.z3;

import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.DropExpr;
import org.batfish.z3.node.OriginateExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

public class MultipathInconsistencyQuerySynthesizer implements QuerySynthesizer {

   private String _queryText;

   public MultipathInconsistencyQuerySynthesizer(String hostname) {
      OriginateExpr originate = new OriginateExpr(hostname);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(AcceptExpr.INSTANCE);
      queryConditions.addConjunct(DropExpr.INSTANCE);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
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
