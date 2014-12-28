package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.Prefix;
import batfish.representation.RouteFilterList;

public abstract class RouteFilterLine implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Prefix _prefix;

   public RouteFilterLine(Prefix prefix) {
      _prefix = prefix;
   }

   public abstract void applyTo(RouteFilterList rfl);

   public final Prefix getPrefix() {
      return _prefix;
   }

}
