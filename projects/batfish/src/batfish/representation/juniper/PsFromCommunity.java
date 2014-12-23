package batfish.representation.juniper;

import java.util.Collections;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapMatchCommunityListLine;
import batfish.representation.VendorConversionException;

public final class PsFromCommunity extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsFromCommunity(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      batfish.representation.CommunityList list = c.getCommunityLists().get(
            _name);
      if (list == null) {
         throw new VendorConversionException("missing community list: \""
               + _name + "\"");
      }
      PolicyMapMatchCommunityListLine line = new PolicyMapMatchCommunityListLine(
            Collections.singleton(list));
      clause.getMatchLines().add(line);
   }

   public String getName() {
      return _name;
   }

}
