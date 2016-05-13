package org.batfish.question.string_set_expr.bgp_advertisement;

import java.util.Set;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.question.Environment;
import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;

public final class CommunitiesBgpAdvertisementStringSetExpr extends
      BgpAdvertisementStringSetExpr {

   public CommunitiesBgpAdvertisementStringSetExpr(BgpAdvertisementExpr caller) {
      super(caller);
   }

   @Override
   public Set<String> evaluate(Environment environment) {
      BgpAdvertisement caller = _caller.evaluate(environment);
      return caller.getCommunities().asStringSet();
   }

}
