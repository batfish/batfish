package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;

public final class PsFromCommunity extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsFromCommunity(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
