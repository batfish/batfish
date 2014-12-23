package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;

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
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
