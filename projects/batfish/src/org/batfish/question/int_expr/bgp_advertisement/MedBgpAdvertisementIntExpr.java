package org.batfish.question.int_expr.bgp_advertisement;

import org.batfish.common.datamodel.BgpAdvertisement;
import org.batfish.question.Environment;
import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;

public final class MedBgpAdvertisementIntExpr extends BgpAdvertisementIntExpr {

   public MedBgpAdvertisementIntExpr(BgpAdvertisementExpr caller) {
      super(caller);
   }

   @Override
   public Integer evaluate(Environment environment) {
      BgpAdvertisement caller = _caller.evaluate(environment);
      return caller.getMed();
   }

}
