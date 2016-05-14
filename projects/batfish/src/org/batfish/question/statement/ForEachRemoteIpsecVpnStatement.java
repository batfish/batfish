package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;

public class ForEachRemoteIpsecVpnStatement extends ForEachStatement<IpsecVpn> {

   public ForEachRemoteIpsecVpnStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<IpsecVpn> getCollection(Environment environment) {
      return environment.getIpsecVpn().getCandidateRemoteIpsecVpns();
   }

   @Override
   protected Map<String, Set<IpsecVpn>> getSetMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, IpsecVpn> getVarMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, IpsecVpn t) {
      environment.setRemoteIpsecVpn(t);
   }

}
