package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterLine;

public class ForEachLineStatement implements Statement {

   private final List<Statement> _statements;

   public ForEachLineStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      List<RouteFilterLine> lines = environment.getRouteFilter().getLines();
      for (RouteFilterLine line : lines) {
         Environment statementEnv = environment.copy();
         statementEnv.setRouteFilterLine(line);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
