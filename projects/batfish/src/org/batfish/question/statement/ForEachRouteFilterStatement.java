package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterList;

public class ForEachRouteFilterStatement extends
      ForEachStatement<RouteFilterList> {

   public ForEachRouteFilterStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<RouteFilterList> getCollection(Environment environment) {
      return environment.getRouteFilterSet();
   }

   @Override
   protected Map<String, Set<RouteFilterList>> getSetMap(Environment environment) {
      return environment.getRouteFilterSets();
   }

   @Override
   protected Map<String, RouteFilterList> getVarMap(Environment environment) {
      return environment.getRouteFilters();
   }

   @Override
   protected void writeVal(Environment environment, RouteFilterList t) {
      environment.setRouteFilter(t);
   }

}
