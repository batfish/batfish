package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.Configuration;

public class ForEachNodeStatement extends ForEachStatement<Configuration> {

   public ForEachNodeStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<Configuration> getCollection(Environment environment) {
      return environment.getConfigurations().values();
   }

   @Override
   protected Map<String, Set<Configuration>> getSetMap(Environment environment) {
      return environment.getNodeSets();
   }

   @Override
   protected Map<String, Configuration> getVarMap(Environment environment) {
      return environment.getNodes();
   }

   @Override
   protected void writeVal(Environment environment, Configuration t) {
      environment.setNode(t);
   }

}
