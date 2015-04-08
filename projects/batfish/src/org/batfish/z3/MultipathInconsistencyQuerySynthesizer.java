package org.batfish.z3;

import java.util.List;

import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.DropExpr;
import org.batfish.z3.node.OriginateExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class MultipathInconsistencyQuerySynthesizer implements QuerySynthesizer {

   private String _hostname;
   private String _queryText;

   public MultipathInconsistencyQuerySynthesizer(String hostname) {
      _hostname = hostname;
   }

   @Override
   public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
      NodProgram program = new NodProgram(baseProgram.getContext());
      OriginateExpr originate = new OriginateExpr(_hostname);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(AcceptExpr.INSTANCE);
      queryConditions.addConjunct(DropExpr.INSTANCE);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      RuleExpr queryRule = new RuleExpr(queryConditions,
            QueryRelationExpr.INSTANCE);
      QueryExpr query = new QueryExpr(QueryRelationExpr.INSTANCE);
      List<BoolExpr> rules = program.getRules();
      rules.add(injectSymbolicPackets.toBoolExpr(baseProgram));
      rules.add(queryRule.toBoolExpr(baseProgram));
      program.getQueries().add(query.toBoolExpr(baseProgram));
      return program;
   }

   @Override
   public String getQueryText() {
      OriginateExpr originate = new OriginateExpr(_hostname);
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
      return _queryText;
   }

}
