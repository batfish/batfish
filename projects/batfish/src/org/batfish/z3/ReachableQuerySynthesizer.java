package org.batfish.z3;

import java.util.List;

import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.NodeAcceptExpr;
import org.batfish.z3.node.OriginateExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class ReachableQuerySynthesizer extends BaseQuerySynthesizer {

   private String _acceptNode;
   private String _originationNode;
   private String _queryText;

   public ReachableQuerySynthesizer(String originationNode, String acceptNode) {
      _originationNode = originationNode;
      _acceptNode = acceptNode;
      OriginateExpr originate = new OriginateExpr(originationNode);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      if (acceptNode != null) {
         NodeAcceptExpr nodeAccept = new NodeAcceptExpr(acceptNode);
         queryConditions.addConjunct(nodeAccept);
      }
      else {
         queryConditions.addConjunct(AcceptExpr.INSTANCE);
      }
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
   public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
      NodProgram program = new NodProgram(baseProgram.getContext());
      OriginateExpr originate = new OriginateExpr(_originationNode);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      if (_acceptNode == null) {
         queryConditions.addConjunct(AcceptExpr.INSTANCE);
      }
      else {
         queryConditions.addConjunct(new NodeAcceptExpr(_acceptNode));
      }
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      RuleExpr queryRule = new RuleExpr(queryConditions,
            QueryRelationExpr.INSTANCE);
      List<BoolExpr> rules = program.getRules();
      BoolExpr injectSymbolicPacketsBoolExpr = injectSymbolicPackets
            .toBoolExpr(baseProgram);
      rules.add(injectSymbolicPacketsBoolExpr);
      rules.add(queryRule.toBoolExpr(baseProgram));
      QueryExpr query = new QueryExpr(QueryRelationExpr.INSTANCE);
      BoolExpr queryBoolExpr = query.toBoolExpr(baseProgram);
      program.getQueries().add(queryBoolExpr);
      return program;
   }

   @Override
   public String getQueryText() {
      return _queryText;
   }

}
