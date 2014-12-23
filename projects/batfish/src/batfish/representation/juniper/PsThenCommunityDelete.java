package batfish.representation.juniper;

import batfish.representation.CommunityList;
import batfish.representation.Configuration;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapSetDeleteCommunityLine;
import batfish.representation.VendorConversionException;

public final class PsThenCommunityDelete extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsThenCommunityDelete(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      CommunityList list = c.getCommunityLists().get(_name);
      if (list == null) {
         throw new VendorConversionException("missing community list: \""
               + _name + "\"");
      }
      PolicyMapSetDeleteCommunityLine line = new PolicyMapSetDeleteCommunityLine(
            list);
      clause.getSetLines().add(line);
   }

   public String getName() {
      return _name;
   }

}
