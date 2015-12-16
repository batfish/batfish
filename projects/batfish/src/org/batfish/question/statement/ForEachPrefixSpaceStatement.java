package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.PrefixSpace;

public class ForEachPrefixSpaceStatement extends ForEachStatement<PrefixSpace> {

   public ForEachPrefixSpaceStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<PrefixSpace> getCollection(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, Set<PrefixSpace>> getSetMap(Environment environment) {
      return environment.getPrefixSpaceSets();
   }

   @Override
   protected Map<String, PrefixSpace> getVarMap(Environment environment) {
      return environment.getPrefixSpaces();
   }

   @Override
   protected void writeVal(Environment environment, PrefixSpace t) {
      environment.setPrefixSpace(t);
   }

}
