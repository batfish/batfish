package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.PolicyMap;

public class ForEachPolicyMapStatement extends ForEachStatement<PolicyMap> {

   public ForEachPolicyMapStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<PolicyMap> getCollection(Environment environment) {
      return environment.getNode().getPolicyMaps().values();
   }

   @Override
   protected Map<String, Set<PolicyMap>> getSetMap(Environment environment) {
      return environment.getPolicyMapSets();
   }

   @Override
   protected Map<String, PolicyMap> getVarMap(Environment environment) {
      return environment.getPolicyMaps();
   }

   @Override
   protected void writeVal(Environment environment, PolicyMap t) {
      environment.setPolicyMap(t);
   }

}
