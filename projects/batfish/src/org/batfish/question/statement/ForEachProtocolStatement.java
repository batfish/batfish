package org.batfish.question.statement;

import java.util.List;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.RoutingProtocol;

public class ForEachProtocolStatement implements Statement {

   List<Statement> _statements;

   public ForEachProtocolStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Set<RoutingProtocol> protocols = environment.getProtocols();
      for (RoutingProtocol protocol : protocols) {
         Environment statementEnv = environment.copy();
         statementEnv.setRoutingProtocol(protocol);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
