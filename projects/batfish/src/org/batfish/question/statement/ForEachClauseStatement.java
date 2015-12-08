package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;

import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;

public class ForEachClauseStatement extends ForEachStatement<PolicyMapClause> {

   public ForEachClauseStatement(List<Statement> statements, String var) {
      super(statements, var);
   }

   @Override
   protected Collection<PolicyMapClause> getCollection(Environment environment) {
      return environment.getPolicyMap().getClauses();
   }

   @Override
   protected PolicyMapClause getOldVarVal(Environment environment) {
      return environment.getPolicyMapClauses().get(_var);
   }

   @Override
   protected void writeVal(Environment environment, PolicyMapClause t) {
      environment.setClause(t);
   }

   @Override
   protected void writeVarVal(Environment environment, PolicyMapClause t) {
      environment.getPolicyMapClauses().put(_var, t);
   }

}
