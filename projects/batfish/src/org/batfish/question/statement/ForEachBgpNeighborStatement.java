package org.batfish.question.statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;

public class ForEachBgpNeighborStatement extends ForEachStatement<BgpNeighbor> {

   public ForEachBgpNeighborStatement(List<Statement> statements, String var) {
      super(statements, var);
   }

   @Override
   protected Collection<BgpNeighbor> getCollection(Environment environment) {
      BgpProcess proc = environment.getNode().getBgpProcess();
      if (proc != null) {
         return proc.getNeighbors().values();
      }
      else {
         return Collections.emptyList();
      }
   }

   @Override
   protected BgpNeighbor getOldVarVal(Environment environment) {
      return environment.getBgpNeighbors().get(_var);
   }

   @Override
   protected void writeVal(Environment environment, BgpNeighbor t) {
      environment.setBgpNeighbor(t);
   }

   @Override
   protected void writeVarVal(Environment environment, BgpNeighbor t) {
      environment.getBgpNeighbors().put(_var, t);
   }

}
