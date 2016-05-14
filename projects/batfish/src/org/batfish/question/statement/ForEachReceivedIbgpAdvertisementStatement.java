package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.question.Environment;

public final class ForEachReceivedIbgpAdvertisementStatement extends
      ForEachStatement<BgpAdvertisement> {

   public ForEachReceivedIbgpAdvertisementStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<BgpAdvertisement> getCollection(Environment environment) {
      environment.initBgpAdvertisements();
      Configuration node = environment.getNode();
      return node.getReceivedIbgpAdvertisements();
   }

   @Override
   protected Map<String, Set<BgpAdvertisement>> getSetMap(
         Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected Map<String, BgpAdvertisement> getVarMap(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   protected void writeVal(Environment environment, BgpAdvertisement t) {
      environment.setReceivedIbgpAdvertisement(t);
   }

}
