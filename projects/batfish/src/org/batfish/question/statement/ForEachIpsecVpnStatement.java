package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.Environment;

public class ForEachIpsecVpnStatement extends ForEachStatement<IpsecVpn> {

   public ForEachIpsecVpnStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<IpsecVpn> getCollection(Environment environment) {
      return environment.getNode().getIpsecVpns().values();
   }

   @Override
   protected Map<String, Set<IpsecVpn>> getSetMap(Environment environment) {
      return environment.getIpsecVpnSets();
   }

   @Override
   protected Map<String, IpsecVpn> getVarMap(Environment environment) {
      return environment.getIpsecVpns();
   }

   @Override
   protected void writeVal(Environment environment, IpsecVpn t) {
      environment.setIpsecVpn(t);
   }

}
