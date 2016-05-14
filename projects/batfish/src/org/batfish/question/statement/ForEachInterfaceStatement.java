package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.Interface;
import org.batfish.question.Environment;

public class ForEachInterfaceStatement extends ForEachStatement<Interface> {

   public ForEachInterfaceStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<Interface> getCollection(Environment environment) {
      return environment.getNode().getInterfaces().values();
   }

   @Override
   protected Map<String, Set<Interface>> getSetMap(Environment environment) {
      return environment.getInterfaceSets();
   }

   @Override
   protected Map<String, Interface> getVarMap(Environment environment) {
      return environment.getInterfaces();
   }

   @Override
   protected void writeVal(Environment environment, Interface t) {
      environment.setInterface(t);
   }

}
