package org.batfish.question.string_set_expr.bgp_advertisement;

import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.question.string_set_expr.BaseStringSetExpr;

public abstract class BgpAdvertisementStringSetExpr extends BaseStringSetExpr {

   protected final BgpAdvertisementExpr _caller;

   public BgpAdvertisementStringSetExpr(BgpAdvertisementExpr caller) {
      _caller = caller;
   }

}
