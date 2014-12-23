package batfish.representation.juniper;

import java.util.Collections;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapSetCommunityLine;
import batfish.util.Util;

public final class PsThenCommunitySet extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsThenCommunitySet(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      batfish.representation.CommunityList list = c.getCommunityLists().get(
            _name);
      String regex = list.getLines().get(0).getRegex();
      // assuming this is a valid community list for setting, the regex value
      // just retrieved should just be an explicit community
      long community = Util.communityStringToLong(regex);
      PolicyMapSetCommunityLine line = new PolicyMapSetCommunityLine(
            Collections.singletonList(community));
      clause.getSetLines().add(line);
   }

   public String getName() {
      return _name;
   }

}
