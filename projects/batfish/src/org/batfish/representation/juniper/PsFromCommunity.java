package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchCommunityListLine;

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
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      org.batfish.representation.CommunityList list = c.getCommunityLists()
            .get(_name);
      if (list == null) {
         warnings.redFlag("missing community list: \"" + _name + "\"");
      }
      else {
         PolicyMapMatchCommunityListLine line = new PolicyMapMatchCommunityListLine(
               Collections.singleton(list));
         clause.getMatchLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

}
