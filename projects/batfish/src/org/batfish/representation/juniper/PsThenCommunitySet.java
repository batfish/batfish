package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapSetCommunityLine;
import org.batfish.util.Util;

public final class PsThenCommunitySet extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private JuniperVendorConfiguration _configuration;

   private final String _name;

   public PsThenCommunitySet(String name,
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
         String regex = list.getLines().get(0).getRegex();
         // assuming this is a valid community list for setting, the regex value
         // just retrieved should just be an explicit community
         long community = Util.communityStringToLong(regex);
         PolicyMapSetCommunityLine line = new PolicyMapSetCommunityLine(
               Collections.singletonList(community));
         clause.getSetLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

}
