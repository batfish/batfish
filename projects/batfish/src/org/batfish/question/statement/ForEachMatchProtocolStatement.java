package org.batfish.question.statement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchType;

public class ForEachMatchProtocolStatement extends
      ForEachStatement<PolicyMapMatchProtocolLine> {

   public ForEachMatchProtocolStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected void elementSideEffect(Environment environment,
         PolicyMapMatchProtocolLine t) {
      environment.setProtocolSet(Collections.singleton(t.getProtocol()));
   }

   @Override
   protected Collection<PolicyMapMatchProtocolLine> getCollection(
         Environment environment) {
      PolicyMapClause clause = environment.getPolicyMapClause();
      Set<PolicyMapMatchProtocolLine> lines = new LinkedHashSet<PolicyMapMatchProtocolLine>();
      for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
         if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
            PolicyMapMatchProtocolLine line = (PolicyMapMatchProtocolLine) matchLine;
            lines.add(line);
         }
      }
      return lines;
   }

   @Override
   protected Map<String, Set<PolicyMapMatchProtocolLine>> getSetMap(
         Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, PolicyMapMatchProtocolLine> getVarMap(
         Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, PolicyMapMatchProtocolLine t) {
      environment.setMatchProtocolLine(t);
   }

}
