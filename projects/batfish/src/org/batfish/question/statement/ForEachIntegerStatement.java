package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;

public class ForEachIntegerStatement extends ForEachStatement<Integer> {

   public ForEachIntegerStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<Integer> getCollection(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, Set<Integer>> getSetMap(Environment environment) {
      return environment.getIntegerSets();
   }

   @Override
   protected Map<String, Integer> getVarMap(Environment environment) {
      return environment.getIntegers();
   }

   @Override
   protected void writeVal(Environment environment, Integer t) {
      environment.setInteger(t);
   }

}
