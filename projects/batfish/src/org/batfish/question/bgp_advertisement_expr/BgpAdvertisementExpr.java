package org.batfish.question.bgp_advertisement_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.BgpAdvertisement;

public interface BgpAdvertisementExpr extends Expr {

   @Override
   public BgpAdvertisement evaluate(Environment environment);

}
