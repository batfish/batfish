package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.question.Environment;

public class ForEachGeneratedRouteStatement extends
      ForEachStatement<GeneratedRoute> {

   public ForEachGeneratedRouteStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<GeneratedRoute> getCollection(Environment environment) {
      Configuration node = environment.getNode();
      BgpNeighbor bgpNeighbor = environment.getBgpNeighbor();
      if (bgpNeighbor != null) {
         return bgpNeighbor.getGeneratedRoutes();
      }
      else {
         return node.getGeneratedRoutes();
      }
   }

   @Override
   protected Map<String, Set<GeneratedRoute>> getSetMap(Environment environment) {
      return environment.getGeneratedRouteSets();
   }

   @Override
   protected Map<String, GeneratedRoute> getVarMap(Environment environment) {
      return environment.getGeneratedRoutes();
   }

   @Override
   protected void writeVal(Environment environment, GeneratedRoute t) {
      environment.setGeneratedRoute(t);
   }

}
