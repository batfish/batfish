package batfish.z3;

import java.util.List;

import batfish.z3.node.AndExpr;
import batfish.z3.node.NodeAcceptExpr;
import batfish.z3.node.NodeDropExpr;
import batfish.z3.node.PostInExpr;
import batfish.z3.node.QueryExpr;
import batfish.z3.node.RuleExpr;

public class MultipathInconsistencyQuerySynthesizer extends QuerySynthesizer {

   public MultipathInconsistencyQuerySynthesizer(List<String> vars) {
      super(vars);
      init();
   }

   private void init() {
      PostInExpr postIn = new PostInExpr(Synthesizer.SRC_NODE_VAR);
      RuleExpr injectSymbolicPackets = new RuleExpr(postIn);
      AndExpr queryConditions = new AndExpr();
      NodeAcceptExpr nodeAccept = new NodeAcceptExpr(
            Synthesizer.NODE_ACCEPT_VAR);
      NodeDropExpr nodeDrop = new NodeDropExpr(Synthesizer.NODE_DROP_VAR);
      queryConditions.addConjunct(nodeAccept);
      queryConditions.addConjunct(nodeDrop);
      QueryExpr query = new QueryExpr(queryConditions);
      StringBuilder sb = new StringBuilder();
      injectSymbolicPackets.print(sb, 0);
      sb.append("\n");
      query.print(sb, 0);
      String queryText = sb.toString();
      setQuery(query);
      setQueryText(queryText);
   }

}
