package org.batfish.question.prefix_expr.bgp_advertisement;

import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.question.prefix_expr.BasePrefixExpr;

public abstract class BgpAdvertisementPrefixExpr extends BasePrefixExpr {

   protected final BgpAdvertisementExpr _caller;

   public BgpAdvertisementPrefixExpr(BgpAdvertisementExpr caller) {
      _caller = caller;
   }

}
