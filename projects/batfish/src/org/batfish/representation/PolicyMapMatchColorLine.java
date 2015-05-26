package org.batfish.representation;

public class PolicyMapMatchColorLine extends PolicyMapMatchLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _color;

   public PolicyMapMatchColorLine(int color) {
      _color = color;
   }

   public int getColor() {
      return _color;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.COLOR;
   }

}
