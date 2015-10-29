package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapClause;

public class ForEachClauseStatement implements Statement {

   List<Statement> _statements;

   public ForEachClauseStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      PolicyMap policyMap = environment.getPolicyMap();
      for (PolicyMapClause clause : policyMap.getClauses()) {
         Environment statementEnv = environment.copy();
         statementEnv.setClause(clause);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
