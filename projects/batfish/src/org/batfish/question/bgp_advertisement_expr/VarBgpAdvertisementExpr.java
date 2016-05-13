package org.batfish.question.bgp_advertisement_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.question.Environment;

public final class VarBgpAdvertisementExpr extends BaseBgpAdvertisementExpr {

   private final String _variable;

   public VarBgpAdvertisementExpr(String variable) {
      _variable = variable;
   }

   @Override
   public BgpAdvertisement evaluate(Environment environment) {
      BgpAdvertisement value = environment.getBgpAdvertisements()
            .get(_variable);
      if (value == null) {
         throw new BatfishException(
               "Reference to undefined bgp_neighbor variable: \"" + _variable
                     + "\"");
      }
      else {
         return value;
      }
   }

}
