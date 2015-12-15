package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.BgpNeighbor;

public final class ForEachRemoteBgpNeighborStatement extends
      ForEachStatement<BgpNeighbor> {

   public ForEachRemoteBgpNeighborStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<BgpNeighbor> getCollection(Environment environment) {
      return environment.getBgpNeighbor().getCandidateRemoteBgpNeighbors();
   }

   @Override
   protected Map<String, Set<BgpNeighbor>> getSetMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, BgpNeighbor> getVarMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, BgpNeighbor t) {
      environment.setRemoteBgpNeighbor(t);
   }

}
