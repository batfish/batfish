package org.batfish.representation;

public enum PolicyMapAction {
   DENY,
   PERMIT;

   public static PolicyMapAction fromLineAction(LineAction la) {
      switch (la) {
      case ACCEPT:
         return PERMIT;
      case REJECT:
         return DENY;
      default:
         return null;
      }
   }
}
