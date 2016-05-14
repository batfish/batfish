package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.RouteFilterLine;
import org.batfish.question.Environment;

public class ForEachLineStatement extends ForEachStatement<RouteFilterLine> {

   public ForEachLineStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<RouteFilterLine> getCollection(Environment environment) {
      return environment.getRouteFilter().getLines();
   }

   @Override
   protected Map<String, Set<RouteFilterLine>> getSetMap(Environment environment) {
      return environment.getRouteFilterLineSets();
   }

   @Override
   protected Map<String, RouteFilterLine> getVarMap(Environment environment) {
      return environment.getRouteFilterLines();
   }

   @Override
   protected void writeVal(Environment environment, RouteFilterLine t) {
      environment.setRouteFilterLine(t);
   }

}
