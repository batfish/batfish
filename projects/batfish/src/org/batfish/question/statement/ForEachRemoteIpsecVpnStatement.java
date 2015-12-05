package org.batfish.question.statement;

import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.IpsecVpn;

public class ForEachRemoteIpsecVpnStatement implements Statement {

   private List<Statement> _statements;

   public ForEachRemoteIpsecVpnStatement(List<Statement> statements) {
      _statements = statements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      for (IpsecVpn remoteIpsecVpn : environment.getIpsecVpn()
            .getCandidateRemoteIpsecVpns()) {
         Environment statementEnv = environment.copy();
         statementEnv.setRemoteIpsecVpn(remoteIpsecVpn);
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

}
