package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.question.Environment;
import org.batfish.representation.Configuration;

public final class ForEachSentIbgpAdvertisementStatement extends
      ForEachStatement<BgpAdvertisement> {

   public ForEachSentIbgpAdvertisementStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<BgpAdvertisement> getCollection(Environment environment) {
      environment.initBgpAdvertisements();
      Configuration node = environment.getNode();
      return node.getSentIbgpAdvertisements();
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
      environment.setSentIbgpAdvertisement(t);
   }

}
