package org.batfish.question.string_expr.bgp_advertisement;

import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public abstract class BgpAdvertisementStringExpr extends BaseStringExpr {

   protected final BgpAdvertisementExpr _caller;

   public BgpAdvertisementStringExpr(BgpAdvertisementExpr caller) {
      _caller = caller;
   }

}
