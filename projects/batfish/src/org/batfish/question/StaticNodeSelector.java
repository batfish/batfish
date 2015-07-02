package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.Configuration;

public enum StaticNodeSelector implements NodeSelector {
   FALSE,
   TRUE;

   @Override
   public boolean select(Configuration node) {
      switch (this) {
      case FALSE:
         return false;
      case TRUE:
         return true;
      default:
         throw new BatfishException("Invalid static interface selector");
      }
   }

}
