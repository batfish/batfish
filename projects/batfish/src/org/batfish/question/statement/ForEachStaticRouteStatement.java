package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.StaticRoute;
import org.batfish.question.Environment;

public class ForEachStaticRouteStatement extends ForEachStatement<StaticRoute> {

   public ForEachStaticRouteStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<StaticRoute> getCollection(Environment environment) {
      return environment.getNode().getStaticRoutes();
   }

   @Override
   protected Map<String, Set<StaticRoute>> getSetMap(Environment environment) {
      return environment.getStaticRouteSets();
   }

   @Override
   protected Map<String, StaticRoute> getVarMap(Environment environment) {
      return environment.getStaticRoutes();
   }

   @Override
   protected void writeVal(Environment environment, StaticRoute t) {
      environment.setStaticRoute(t);
   }

}
