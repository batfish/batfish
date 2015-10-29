package org.batfish.question.statement;

import java.util.Collections;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchType;

public class ForEachMatchProtocolStatement implements Statement {

   List<Statement> _statements;

   public ForEachMatchProtocolStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      PolicyMapClause clause = environment.getClause();
      for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
         if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
            Environment statementEnv = environment.copy();
            PolicyMapMatchProtocolLine matchProtocolLine = (PolicyMapMatchProtocolLine) matchLine;
            statementEnv.setMatchProtocolLine(matchProtocolLine);
            statementEnv.setProtocolSet(Collections.singleton(matchProtocolLine
                  .getProtocol()));
            for (Statement statement : _statements) {
               statement.execute(statementEnv, logger, settings);
            }
         }
      }
   }

}
