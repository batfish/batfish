package org.batfish.question.bgp_advertisement_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.question.Environment;

public enum BaseCaseBgpAdvertisementExpr implements BgpAdvertisementExpr {
   RECEIVED_EBGP_ADVERTISEMENT,
   RECEIVED_IBGP_ADVERTISEMENT,
   SENT_EBGP_ADVERTISEMENT,
   SENT_IBGP_ADVERTISEMENT;

   @Override
   public BgpAdvertisement evaluate(Environment environment) {
      switch (this) {
      case RECEIVED_EBGP_ADVERTISEMENT:
         return environment.getReceivedEbgpAdvertisement();

      case RECEIVED_IBGP_ADVERTISEMENT:
         return environment.getReceivedIbgpAdvertisement();

      case SENT_EBGP_ADVERTISEMENT:
         return environment.getSentEbgpAdvertisement();

      case SENT_IBGP_ADVERTISEMENT:
         return environment.getSentIbgpAdvertisement();

      default:
         throw new BatfishException("Invalid "
               + this.getClass().getSimpleName());
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseBgpAdvertisementExpr.print(this, environment);
   }

}
