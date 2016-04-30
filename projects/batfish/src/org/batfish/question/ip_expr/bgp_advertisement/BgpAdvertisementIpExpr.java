package org.batfish.question.ip_expr.bgp_advertisement;

import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.question.ip_expr.BaseIpExpr;

public abstract class BgpAdvertisementIpExpr extends BaseIpExpr {

   protected final BgpAdvertisementExpr _caller;

   public BgpAdvertisementIpExpr(BgpAdvertisementExpr caller) {
      _caller = caller;
   }

}
