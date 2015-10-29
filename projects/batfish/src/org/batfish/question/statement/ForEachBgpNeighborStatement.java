package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;

public class ForEachBgpNeighborStatement implements Statement {

   private List<Statement> _statements;

   public ForEachBgpNeighborStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      BgpProcess proc = environment.getNode().getBgpProcess();
      if (proc != null) {

         for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
            Environment statementEnv = environment.copy();
            statementEnv.setBgpNeighbor(bgpNeighbor);
            for (Statement statement : _statements) {
               statement.execute(statementEnv, logger, settings);
            }
         }
      }
   }

}
