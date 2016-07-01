package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.List;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapSetAddCommunityLine;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public final class PsThenCommunityAdd extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private JuniperConfiguration _configuration;

   private final String _name;

   public PsThenCommunityAdd(String name, JuniperConfiguration configuration) {
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
         return;
      }
      else {
         for (CommunityListLine clLine : namedList.getLines()) {
            // assuming that regex here is actually a literal community
            String communityStr = clLine.getRegex();
            Long communityLong = CommonUtil.communityStringToLong(communityStr);
            statements.add(new AddCommunity(communityLong));
         }
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
         for (CommunityListLine clLine : namedList.getLines()) {
            // assuming that regex here is actually a literal community
            String communityStr = clLine.getRegex();
            Long communityLong = CommonUtil.communityStringToLong(communityStr);
            PolicyMapSetAddCommunityLine line = new PolicyMapSetAddCommunityLine(
                  Collections.singletonList(communityLong));
            clause.getSetLines().add(line);
         }
      }
   }

   public String getName() {
      return _name;
   }
}
