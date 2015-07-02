package org.batfish.question;

import org.batfish.main.BatfishException;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;

public enum StaticInterfaceSelector implements InterfaceSelector {
   FALSE,
   TRUE;

   @Override
   public boolean select(Configuration node, Interface iface) {
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
