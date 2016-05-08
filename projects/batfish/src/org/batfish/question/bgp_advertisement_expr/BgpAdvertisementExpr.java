package org.batfish.question.bgp_advertisement_expr;

import org.batfish.common.datamodel.BgpAdvertisement;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface BgpAdvertisementExpr extends Expr {

   @Override
   public BgpAdvertisement evaluate(Environment environment);

}
