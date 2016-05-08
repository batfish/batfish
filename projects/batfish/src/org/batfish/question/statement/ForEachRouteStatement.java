package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.common.datamodel.PrecomputedRoute;
import org.batfish.question.Environment;

public final class ForEachRouteStatement extends
      ForEachStatement<PrecomputedRoute> {

   public ForEachRouteStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<PrecomputedRoute> getCollection(Environment environment) {
      environment.initRoutes();
      return environment.getNode().getRoutes();
   }

   @Override
   protected Map<String, Set<PrecomputedRoute>> getSetMap(
         Environment environment) {
      return environment.getRouteSets();

   }

   @Override
   protected Map<String, PrecomputedRoute> getVarMap(Environment environment) {
      return environment.getRoutes();
   }

   @Override
   protected void writeVal(Environment environment, PrecomputedRoute t) {
      environment.setRoute(t);
   }
}
