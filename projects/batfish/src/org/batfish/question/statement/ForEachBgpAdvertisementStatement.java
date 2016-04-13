package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.representation.BgpAdvertisement;

public final class ForEachBgpAdvertisementStatement extends
      ForEachStatement<BgpAdvertisement> {

   public ForEachBgpAdvertisementStatement(List<Statement> statements,
         String var, String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<BgpAdvertisement> getCollection(Environment environment) {
      environment.initBgpAdvertisements();
      return environment.getNode().getBgpAdvertisements();
   }

   @Override
   protected Map<String, Set<BgpAdvertisement>> getSetMap(
         Environment environment) {
      return environment.getBgpAdvertisementSets();

   }

   @Override
   protected Map<String, BgpAdvertisement> getVarMap(Environment environment) {
      return environment.getBgpAdvertisements();
   }

   @Override
   protected void writeVal(Environment environment, BgpAdvertisement t) {
      environment.setBgpAdvertisement(t);
   }

}
