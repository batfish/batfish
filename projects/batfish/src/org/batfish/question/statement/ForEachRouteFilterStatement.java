package org.batfish.question.statement;

import java.util.List;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterList;

public class ForEachRouteFilterStatement implements Statement {

   List<Statement> _statements;

   public ForEachRouteFilterStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Set<RouteFilterList> routeFilters = environment.getRouteFilterSet();
      for (RouteFilterList routeFilter : routeFilters) {
         Environment statementEnv = environment.copy();
         statementEnv.setRouteFilter(routeFilter);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
