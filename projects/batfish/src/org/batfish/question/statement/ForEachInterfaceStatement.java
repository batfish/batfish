package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.Interface;

public class ForEachInterfaceStatement implements Statement {

   private List<Statement> _statements;

   public ForEachInterfaceStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      for (Interface iface : environment.getNode().getInterfaces().values()) {
         Environment statementEnv = environment.copy();
         statementEnv.setInterface(iface);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
