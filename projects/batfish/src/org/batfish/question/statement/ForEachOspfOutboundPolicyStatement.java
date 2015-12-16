package org.batfish.question.statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.OspfProcess;
import org.batfish.representation.PolicyMap;

public class ForEachOspfOutboundPolicyStatement extends
      ForEachStatement<PolicyMap> {

   public ForEachOspfOutboundPolicyStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<PolicyMap> getCollection(Environment environment) {
      OspfProcess proc = environment.getNode().getOspfProcess();
      if (proc != null) {
         return proc.getOutboundPolicyMaps();
      }
      else {
         return Collections.emptyList();
      }
   }

   @Override
   protected Map<String, Set<PolicyMap>> getSetMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, PolicyMap> getVarMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, PolicyMap t) {
      environment.setPolicyMap(t);
   }

}
