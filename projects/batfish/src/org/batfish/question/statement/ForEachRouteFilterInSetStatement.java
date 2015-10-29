package org.batfish.question.statement;

import java.util.List;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterList;

public class ForEachRouteFilterInSetStatement implements Statement {

   private final String _set;

   private final List<Statement> _statements;

   public ForEachRouteFilterInSetStatement(List<Statement> statements,
         String set) {
      _statements = statements;
      _set = set;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Set<RouteFilterList> routeFilters = environment.getRouteFilterSets().get(
            _set);
      for (RouteFilterList routeFilter : routeFilters) {
         Environment statementEnv = environment.copy();
         statementEnv.setRouteFilter(routeFilter);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
