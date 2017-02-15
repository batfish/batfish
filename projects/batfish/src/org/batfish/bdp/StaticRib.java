package org.batfish.bdp;

import org.batfish.datamodel.StaticRoute;

public class StaticRib extends AbstractRib<StaticRoute> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public StaticRib(VirtualRouter owner) {
      super(owner);
   }

   @Override
   public int comparePreference(StaticRoute lhs, StaticRoute rhs) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
