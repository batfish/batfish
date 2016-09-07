package org.batfish.datamodel.routing_policy.expr;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitCommunitySet implements CommunitySetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedSet<Long> _communities;

   @JsonCreator
   public ExplicitCommunitySet() {
      _communities = new TreeSet<>();
   }

   public ExplicitCommunitySet(Set<Long> communities) {
      this();
      _communities.addAll(communities);
   }

   @Override
   public CommunitySet communities(Environment environment) {
      CommunitySet out = new CommunitySet();
      out.addAll(_communities);
      return out;
   }

   public SortedSet<Long> getCommunities() {
      return _communities;
   }

   @Override
   public boolean matchSingleCommunity(Environment environment,
         CommunitySet communities) {
      for (Long community : communities) {
         if (_communities.contains(community)) {
            return true;
         }
      }
      return false;
   }

   public void setCommunities(SortedSet<Long> communities) {
      _communities = communities;
   }

}
