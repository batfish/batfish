package org.batfish.question;

import org.batfish.common.BatfishException;

public enum ForwardingAction {
   ACCEPT,
   DROP,
   DROP_ACL,
   DROP_ACL_IN,
   DROP_ACL_OUT,
   DROP_NO_ROUTE,
   DROP_NULL_ROUTE,
   FORWARD;

   public static ForwardingAction fromString(String text) {
      switch (text) {
      case "accept":
         return ACCEPT;

      case "drop":
         return DROP;

      default:
         throw new BatfishException("invalid forwarding action: \"" + text
               + "\"");
      }
   }

}
