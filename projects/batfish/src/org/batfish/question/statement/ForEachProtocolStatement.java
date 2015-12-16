package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.RoutingProtocol;

public class ForEachProtocolStatement extends ForEachStatement<RoutingProtocol> {

   public ForEachProtocolStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<RoutingProtocol> getCollection(Environment environment) {
      return environment.getProtocols();
   }

   @Override
   protected Map<String, Set<RoutingProtocol>> getSetMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, RoutingProtocol> getVarMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, RoutingProtocol t) {
      environment.setRoutingProtocol(t);
   }

}
