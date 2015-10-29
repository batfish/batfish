package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;

public class ForEachNodeBgpGeneratedRouteStatement implements Statement {

   private List<Statement> _statements;

   public ForEachNodeBgpGeneratedRouteStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Configuration node = environment.getNode();
      BgpProcess proc = node.getBgpProcess();
      if (proc != null) {
         for (GeneratedRoute generatedRoute : proc.getGeneratedRoutes()) {
            Environment statementEnv = environment.copy();
            statementEnv.setGeneratedRoute(generatedRoute);
            for (Statement statement : _statements) {
               statement.execute(statementEnv, logger, settings);
            }
         }
      }
   }

}
