package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;

public final class FromCommunity extends From {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public FromCommunity(String name) {
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
