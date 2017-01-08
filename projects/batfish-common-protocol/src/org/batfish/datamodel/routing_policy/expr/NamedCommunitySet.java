package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedCommunitySet implements CommunitySetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   public NamedCommunitySet() {
   }

   public NamedCommunitySet(String name) {
      _name = name;
   }

   @Override
   public CommunitySet communities(Environment environment) {
      CommunitySet out = new CommunitySet();
      CommunityList cl = environment.getConfiguration().getCommunityLists()
            .get(_name);
      for (CommunityListLine line : cl.getLines()) {
         Long community = line.toLiteralCommunity();
         out.add(community);
      }
      return out;
   }

   @Override
   public CommunitySet communities(Environment environment,
         CommunitySet communityCandidates) {
      CommunitySet matchingCommunities = new CommunitySet();
      for (Long community : communityCandidates) {
         CommunityList cl = environment.getConfiguration().getCommunityLists()
               .get(_name);
         if (cl.permits(community)) {
            matchingCommunities.add(community);
         }
      }
      return matchingCommunities;
   }

   public String getName() {
      return _name;
   }

   @Override
   public boolean matchSingleCommunity(Environment environment,
         CommunitySet communities) {
      CommunityList cl = environment.getConfiguration().getCommunityLists()
            .get(_name);
      for (Long community : communities) {
         if (cl.permits(community)) {
            return true;
         }
      }
      return false;
   }

   public void setName(String name) {
      _name = name;
   }

}
