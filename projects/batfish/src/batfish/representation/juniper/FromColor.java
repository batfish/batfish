package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;

public final class FromColor extends From {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _color;

   public FromColor(int color) {
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
