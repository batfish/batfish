package org.batfish.representation.juniper;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchColorLine;

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
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      PolicyMapMatchColorLine line = new PolicyMapMatchColorLine(_color);
      clause.getMatchLines().add(line);
   }

   public int getColor() {
      return _color;
   }

}
