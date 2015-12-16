package org.batfish.question.statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;

public class ForEachNodeBgpGeneratedRouteStatement extends
      ForEachStatement<GeneratedRoute> {

   public ForEachNodeBgpGeneratedRouteStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<GeneratedRoute> getCollection(Environment environment) {
      Configuration node = environment.getNode();
      BgpProcess proc = node.getBgpProcess();
      if (proc != null) {
         return proc.getGeneratedRoutes();
      }
      else {
         return Collections.emptyList();
      }
   }

   @Override
   protected Map<String, Set<GeneratedRoute>> getSetMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, GeneratedRoute> getVarMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, GeneratedRoute t) {
      environment.setGeneratedRoute(t);
   }

}
