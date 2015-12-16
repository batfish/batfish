package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;

public class ForEachPolicyMapClauseStatement extends
      ForEachStatement<PolicyMapClause> {

   public ForEachPolicyMapClauseStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<PolicyMapClause> getCollection(Environment environment) {
      return environment.getPolicyMap().getClauses();
   }

   @Override
   protected Map<String, Set<PolicyMapClause>> getSetMap(Environment environment) {
      return environment.getPolicyMapClauseSets();
   }

   @Override
   protected Map<String, PolicyMapClause> getVarMap(Environment environment) {
      return environment.getPolicyMapClauses();
   }

   @Override
   protected void writeVal(Environment environment, PolicyMapClause t) {
      environment.setPolicyMapClause(t);
   }

}
