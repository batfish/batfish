package org.batfish.z3;

import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.ExternalDestinationIpExpr;
import org.batfish.z3.node.ExternalSourceIpExpr;
import org.batfish.z3.node.OriginateVrfExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RoleAcceptExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

import com.microsoft.z3.Z3Exception;

public class RoleReachabilityQuerySynthesizer extends BaseQuerySynthesizer {

   public RoleReachabilityQuerySynthesizer(String hostname, String vrf,
         String role) {
      OriginateVrfExpr originate = new OriginateVrfExpr(hostname, vrf);
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
   }

   @Override
   public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
