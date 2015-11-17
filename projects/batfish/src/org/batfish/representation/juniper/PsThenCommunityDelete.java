package org.batfish.representation.juniper;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapSetDeleteCommunityLine;
import org.batfish.representation.VendorConversionException;

public final class PsThenCommunityDelete extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private JuniperVendorConfiguration _configuration;

   private final String _name;

   public PsThenCommunityDelete(String name,
         JuniperVendorConfiguration configuration) {
      _name = name;
      _configuration = configuration;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      CommunityList namedList = _configuration.getCommunityLists().get(_name);
      if (namedList == null) {
         warnings
               .redFlag("Reference to undefined community: \"" + _name + "\"");
      }
      else {
         org.batfish.representation.CommunityList list = c.getCommunityLists()
               .get(_name);
         if (list == null) {
            throw new VendorConversionException("missing community list: \""
                  + _name + "\"");
         }
         PolicyMapSetDeleteCommunityLine line = new PolicyMapSetDeleteCommunityLine(
               list);
         clause.getSetLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

}
