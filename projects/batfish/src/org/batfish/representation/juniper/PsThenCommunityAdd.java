package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapSetAddCommunityLine;
import org.batfish.util.Util;

public final class PsThenCommunityAdd extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private JuniperVendorConfiguration _configuration;

   private final String _name;

   public PsThenCommunityAdd(String name,
         JuniperVendorConfiguration configuration) {
      _name = name;
      _configuration = configuration;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      CommunityList namedList = _configuration.getCommunityLists().get(_name);
      for (CommunityListLine clLine : namedList.getLines()) {
         // assuming that regex here is actually a literal community
         String communityStr = clLine.getRegex();
         Long communityLong = Util.communityStringToLong(communityStr);
         PolicyMapSetAddCommunityLine line = new PolicyMapSetAddCommunityLine(
               Collections.singletonList(communityLong));
         clause.getSetLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

}
