package org.batfish.question.statement;

import java.util.List;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;

public class ForEachGeneratedRouteStatement implements Statement {

   private List<Statement> _statements;

   public ForEachGeneratedRouteStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Configuration node = environment.getNode();
      Set<GeneratedRoute> generatedRoutes;
      BgpNeighbor bgpNeighbor = environment.getBgpNeighbor();
      if (bgpNeighbor != null) {
         generatedRoutes = bgpNeighbor.getGeneratedRoutes();
      }
      else {
         generatedRoutes = node.getGeneratedRoutes();
      }
      for (GeneratedRoute generatedRoute : generatedRoutes) {
         Environment statementEnv = environment.copy();
         statementEnv.setGeneratedRoute(generatedRoute);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
