package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedCommunitySet extends CommunitySetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   private NamedCommunitySet() {
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

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      NamedCommunitySet other = (NamedCommunitySet) obj;
      if (_name == null) {
         if (other._name != null) {
            return false;
         }
      }
      else if (!_name.equals(other._name)) {
         return false;
      }
      return true;
   }

   public String getName() {
      return _name;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_name == null) ? 0 : _name.hashCode());
      return result;
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
