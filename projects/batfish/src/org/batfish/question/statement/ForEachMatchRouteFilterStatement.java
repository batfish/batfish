package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchType;

public class ForEachMatchRouteFilterStatement implements Statement {

   List<Statement> _statements;

   public ForEachMatchRouteFilterStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      PolicyMapClause clause = environment.getClause();
      for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
         if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
            Environment statementEnv = environment.copy();
            PolicyMapMatchRouteFilterListLine matchRouteFilterLine = (PolicyMapMatchRouteFilterListLine) matchLine;
            statementEnv.setMatchRouteFilterLine(matchRouteFilterLine);
            statementEnv.setRouteFilterSet(matchRouteFilterLine.getLists());
            for (Statement statement : _statements) {
               statement.execute(statementEnv, logger, settings);
            }
         }
      }
   }

}
