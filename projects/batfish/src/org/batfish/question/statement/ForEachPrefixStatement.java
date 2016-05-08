package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.common.datamodel.Prefix;
import org.batfish.question.Environment;

public class ForEachPrefixStatement extends ForEachStatement<Prefix> {

   public ForEachPrefixStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<Prefix> getCollection(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, Set<Prefix>> getSetMap(Environment environment) {
      return environment.getPrefixSets();
   }

   @Override
   protected Map<String, Prefix> getVarMap(Environment environment) {
      return environment.getPrefixes();
   }

   @Override
   protected void writeVal(Environment environment, Prefix t) {
      environment.setPrefix(t);
   }

}
