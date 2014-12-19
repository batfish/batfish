package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;

public final class PsFromColor extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _color;

   public PsFromColor(int color) {
      _color = color;
   }

   public int getColor() {
      return _color;
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
