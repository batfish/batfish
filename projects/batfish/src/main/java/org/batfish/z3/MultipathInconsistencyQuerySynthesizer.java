package org.batfish.z3;

import java.util.List;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.DropExpr;
import org.batfish.z3.node.OriginateVrfExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class MultipathInconsistencyQuerySynthesizer
      extends BaseQuerySynthesizer {

   private HeaderSpace _headerSpace;

   private String _hostname;

   private String _vrf;

   public MultipathInconsistencyQuerySynthesizer(String hostname, String vrf,
         HeaderSpace headerSpace) {
      _hostname = hostname;
      _vrf = vrf;
      _headerSpace = headerSpace;
   }

   @Override
   public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
      NodProgram program = new NodProgram(baseProgram.getContext());
      OriginateVrfExpr originate = new OriginateVrfExpr(_hostname, _vrf);
      RuleExpr injectSymbolicPackets = new RuleExpr(originate);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(AcceptExpr.INSTANCE);
      queryConditions.addConjunct(DropExpr.INSTANCE);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      queryConditions.addConjunct(Synthesizer.matchHeaderSpace(_headerSpace));
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

}
