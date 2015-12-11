package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;

public class ForEachStringStatement extends ForEachStatement<String> {

   public ForEachStringStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<String> getCollection(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, Set<String>> getSetMap(Environment environment) {
      return environment.getStringSets();
   }

   @Override
   protected Map<String, String> getVarMap(Environment environment) {
      return environment.getStrings();
   }

   @Override
   protected void writeVal(Environment environment, String t) {
      environment.setString(t);
   }

}
