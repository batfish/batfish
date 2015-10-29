package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMap;

public class ForEachOspfOutboundPolicyStatement implements Statement {

   List<Statement> _statements;

   public ForEachOspfOutboundPolicyStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Configuration node = environment.getNode();
      for (PolicyMap policyMap : node.getOspfProcess().getOutboundPolicyMaps()) {
         Environment statementEnv = environment.copy();
         statementEnv.setPolicyMap(policyMap);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
