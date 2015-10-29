package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.Configuration;
import org.batfish.representation.StaticRoute;

public class ForEachStaticRouteStatement implements Statement {

   List<Statement> _statements;

   public ForEachStaticRouteStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Configuration node = environment.getNode();
      for (StaticRoute staticRoute : node.getStaticRoutes()) {
         Environment statementEnv = environment.copy();
         statementEnv.setStaticRoute(staticRoute);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
