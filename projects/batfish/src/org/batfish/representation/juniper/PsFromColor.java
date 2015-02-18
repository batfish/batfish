package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;

public final class PsFromColor extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _color;

   public PsFromColor(int color) {
      _color = color;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public int getColor() {
      return _color;
   }

}
