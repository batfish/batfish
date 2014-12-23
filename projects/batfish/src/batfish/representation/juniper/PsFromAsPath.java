package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;

public class PsFromAsPath extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _asPathName;

   public PsFromAsPath(String asPathName) {
      _asPathName = asPathName;
   }

   public String getAsPathName() {
      return _asPathName;
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
