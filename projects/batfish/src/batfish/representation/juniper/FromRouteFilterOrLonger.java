package batfish.representation.juniper;

import batfish.representation.PolicyMapMatchLine;
import batfish.representation.Prefix;

public class FromRouteFilterOrLonger extends FromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public FromRouteFilterOrLonger(Prefix prefix) {
      super(prefix);
   }

   @Override
   public PolicyMapMatchLine toPolicyMapMatchLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
