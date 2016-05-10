package org.batfish.question.prefix_expr.bgp_advertisement;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;

public final class NetworkBgpAdvertisementPrefixExpr extends
      BgpAdvertisementPrefixExpr {

   public NetworkBgpAdvertisementPrefixExpr(BgpAdvertisementExpr caller) {
      super(caller);
   }

   @Override
   public Prefix evaluate(Environment environment) {
      BgpAdvertisement caller = _caller.evaluate(environment);
      return caller.getNetwork();
   }

}
