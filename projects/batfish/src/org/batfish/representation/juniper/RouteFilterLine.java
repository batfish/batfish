package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterList;

public abstract class RouteFilterLine implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Prefix _prefix;

   private final Set<PsThen> _thens;

   public RouteFilterLine(Prefix prefix) {
      _prefix = prefix;
      _thens = new HashSet<PsThen>();
   }

   public abstract void applyTo(RouteFilterList rfl);

   @Override
   public abstract boolean equals(Object o);

   public final Prefix getPrefix() {
      return _prefix;
   }

   public Set<PsThen> getThens() {
      return _thens;
   }

   @Override
   public abstract int hashCode();

}
