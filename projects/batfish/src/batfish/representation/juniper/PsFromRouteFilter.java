package batfish.representation.juniper;

import batfish.representation.Prefix;

public abstract class PsFromRouteFilter extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Prefix _prefix;

   public PsFromRouteFilter(Prefix prefix) {
      _prefix = prefix;
   }

   public final Prefix getPrefix() {
      return _prefix;
   }

}
