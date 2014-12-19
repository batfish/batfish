package batfish.representation.juniper;

import batfish.representation.PolicyMapSetLine;

public final class PsThenCommunitySet extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsThenCommunitySet(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public PolicyMapSetLine toPolicyStatmentSetLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
