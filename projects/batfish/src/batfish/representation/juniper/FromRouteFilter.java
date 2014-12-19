package batfish.representation.juniper;

import batfish.representation.Prefix;

public abstract class FromRouteFilter extends From {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Prefix _prefix;

   public FromRouteFilter(Prefix prefix) {
      _prefix = prefix;
   }

   public final Prefix getPrefix() {
      return _prefix;
   }

}
