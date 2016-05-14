package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;

public final class RouteFilterLineExact extends RouteFilterLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public RouteFilterLineExact(Prefix prefix) {
      super(prefix);
   }

   @Override
   public void applyTo(RouteFilterList rfl) {
      int prefixLength = _prefix.getPrefixLength();
      org.batfish.datamodel.RouteFilterLine line = new org.batfish.datamodel.RouteFilterLine(
            LineAction.ACCEPT, _prefix,
            new SubRange(prefixLength, prefixLength));
      rfl.addLine(line);
   }

   @Override
   public boolean equals(Object o) {
      if (!this.getClass().equals(o.getClass())) {
         return false;
      }
      else {
         RouteFilterLineExact rhs = (RouteFilterLineExact) o;
         return _prefix.equals(rhs._prefix);
      }
   }

   @Override
   public int hashCode() {
      return _prefix.hashCode();
   }

}
