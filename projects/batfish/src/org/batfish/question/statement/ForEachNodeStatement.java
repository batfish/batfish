package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;

import org.batfish.question.Environment;
import org.batfish.representation.Configuration;

public class ForEachNodeStatement extends ForEachStatement<Configuration> {

   public ForEachNodeStatement(List<Statement> statements, String var) {
      super(statements, var);
   }

   @Override
   protected Collection<Configuration> getCollection(Environment environment) {
      return environment.getAllNodes();
   }

   @Override
   protected Configuration getOldVarVal(Environment environment) {
      return environment.getNodes().get(_var);
   }

   @Override
   protected void writeVal(Environment environment, Configuration t) {
      environment.setNode(t);
   }

   @Override
   protected void writeVarVal(Environment environment, Configuration t) {
      environment.getNodes().put(_var, t);
   }

}
