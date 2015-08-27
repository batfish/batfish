package org.batfish.z3;

import java.util.List;

import org.batfish.representation.Edge;
import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.OriginateExpr;
import org.batfish.z3.node.PreInInterfaceExpr;
import org.batfish.z3.node.PreOutEdgeExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class ReachEdgeQuerySynthesizer extends BaseQuerySynthesizer {

   private Edge _edge;

   private String _originationNode;

   private boolean _requireAcceptance;

   public ReachEdgeQuerySynthesizer(String originationNode, Edge edge,
         boolean requireAcceptance) {
      _originationNode = originationNode;
      _edge = edge;
      _requireAcceptance = requireAcceptance;
   }

   @Override
   public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
      NodProgram program = new NodProgram(baseProgram.getContext());
      OriginateExpr originate = new OriginateExpr(_originationNode);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(new PreOutEdgeExpr(_edge));
      queryConditions.addConjunct(new PreInInterfaceExpr(_edge.getNode2(),
            _edge.getInt2()));
      if (_requireAcceptance) {
         queryConditions.addConjunct(AcceptExpr.INSTANCE);
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
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
