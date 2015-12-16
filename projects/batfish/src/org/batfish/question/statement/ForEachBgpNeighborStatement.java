package org.batfish.question.statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;

public class ForEachBgpNeighborStatement extends ForEachStatement<BgpNeighbor> {

   public ForEachBgpNeighborStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
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
   protected Map<String, Set<BgpNeighbor>> getSetMap(Environment environment) {
      return environment.getBgpNeighborSets();

   }

   @Override
   protected Map<String, BgpNeighbor> getVarMap(Environment environment) {
      return environment.getBgpNeighbors();
   }

   @Override
   protected void writeVal(Environment environment, BgpNeighbor t) {
      environment.setBgpNeighbor(t);
   }

}
