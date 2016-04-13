package org.batfish.question.string_expr.bgp_advertisement;

import org.batfish.question.Environment;
import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.representation.BgpAdvertisement;

public final class AsPathBgpAdvertisementStringExpr extends
      BgpAdvertisementStringExpr {

   public AsPathBgpAdvertisementStringExpr(BgpAdvertisementExpr caller) {
      super(caller);
   }

   @Override
   public String evaluate(Environment environment) {
      BgpAdvertisement caller = _caller.evaluate(environment);
      return caller.getAsPath().toString();
   }

}
