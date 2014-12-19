package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;

public class FromAsPath extends From {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _asPathName;

   public FromAsPath(String asPathName) {
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
