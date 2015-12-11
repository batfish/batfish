package org.batfish.question.statement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchType;

public class ForEachMatchRouteFilterStatement extends
      ForEachStatement<PolicyMapMatchRouteFilterListLine> {

   public ForEachMatchRouteFilterStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected void elementSideEffect(Environment environment,
         PolicyMapMatchRouteFilterListLine t) {
      environment.setRouteFilterSet(t.getLists());
   }

   @Override
   protected Collection<PolicyMapMatchRouteFilterListLine> getCollection(
         Environment environment) {
      Set<PolicyMapMatchRouteFilterListLine> lines = new LinkedHashSet<PolicyMapMatchRouteFilterListLine>();
      PolicyMapClause clause = environment.getPolicyMapClause();
      for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
         if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
            PolicyMapMatchRouteFilterListLine line = (PolicyMapMatchRouteFilterListLine) matchLine;
            lines.add(line);
         }
      }
      return lines;
   }

   @Override
   protected Map<String, Set<PolicyMapMatchRouteFilterListLine>> getSetMap(
         Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, PolicyMapMatchRouteFilterListLine> getVarMap(
         Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment,
         PolicyMapMatchRouteFilterListLine t) {
      environment.setMatchRouteFilterLine(t);
   }

}
