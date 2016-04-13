package org.batfish.question.int_expr.bgp_advertisement;

import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.question.int_expr.BaseIntExpr;

public abstract class BgpAdvertisementIntExpr extends BaseIntExpr {

   protected final BgpAdvertisementExpr _caller;

   public BgpAdvertisementIntExpr(BgpAdvertisementExpr caller) {
      _caller = caller;
   }

}
