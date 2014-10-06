package batfish.z3;

import batfish.z3.node.AndExpr;
import batfish.z3.node.DropExpr;
import batfish.z3.node.OriginateExpr;
import batfish.z3.node.QueryExpr;
import batfish.z3.node.RuleExpr;
import batfish.z3.node.SaneExpr;

public class FailureInconsistencyBlackHoleQuerySynthesizer implements
      QuerySynthesizer {

   private String _queryText;

   public FailureInconsistencyBlackHoleQuerySynthesizer(String hostname) {
      OriginateExpr originate = new OriginateExpr(hostname);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(DropExpr.INSTANCE);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      QueryExpr query = new QueryExpr(queryConditions);
      StringBuilder sb = new StringBuilder();
      injectSymbolicPackets.print(sb, 0);
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
