package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.Ip;

public class ForEachIpStatement extends ForEachStatement<Ip> {

   public ForEachIpStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<Ip> getCollection(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, Set<Ip>> getSetMap(Environment environment) {
      return environment.getIpSets();
   }

   @Override
   protected Map<String, Ip> getVarMap(Environment environment) {
      return environment.getIps();
   }

   @Override
   protected void writeVal(Environment environment, Ip t) {
      environment.setIp(t);
   }

}
