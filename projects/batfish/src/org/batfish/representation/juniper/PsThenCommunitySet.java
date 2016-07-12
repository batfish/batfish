package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.List;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapSetCommunityLine;
import org.batfish.datamodel.routing_policy.expr.ExplicitCommunitySet;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public final class PsThenCommunitySet extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private JuniperConfiguration _configuration;

   private final String _name;

   public PsThenCommunitySet(String name, JuniperConfiguration configuration) {
      _name = name;
      _configuration = configuration;
   }

   @Override
   public void applyTo(List<Statement> statements,
         JuniperConfiguration juniperVendorConfiguration, Configuration c,
         Warnings warnings) {
      CommunityList namedList = _configuration.getCommunityLists().get(_name);
      if (namedList == null) {
         warnings
               .redFlag("Reference to undefined community: \"" + _name + "\"");
      }
      else {
         org.batfish.datamodel.CommunityList list = c.getCommunityLists().get(
               _name);
         String regex = list.getLines().get(0).getRegex();
         // assuming this is a valid community list for setting, the regex value
         // just retrieved should just be an explicit community
         long community = CommonUtil.communityStringToLong(regex);
         statements.add(new SetCommunity(new ExplicitCommunitySet(Collections
               .singleton(community))));
      }
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
         org.batfish.datamodel.CommunityList list = c.getCommunityLists().get(
               _name);
         String regex = list.getLines().get(0).getRegex();
         // assuming this is a valid community list for setting, the regex value
         // just retrieved should just be an explicit community
         long community = CommonUtil.communityStringToLong(regex);
         PolicyMapSetCommunityLine line = new PolicyMapSetCommunityLine(
               Collections.singletonList(community));
         clause.getSetLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

}
