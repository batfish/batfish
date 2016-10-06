package org.batfish.datamodel.routing_policy.expr;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class InlineCommunitySet implements CommunitySetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient CommunitySet _cachedCommunities;

   private Set<CommunitySetElem> _communities;

   @JsonCreator
   public InlineCommunitySet() {
   }

   public InlineCommunitySet(Collection<Long> communities) {
      _communities = communities.stream().map(l -> new CommunitySetElem(l))
            .collect(Collectors.toSet());
   }

   public InlineCommunitySet(Set<CommunitySetElem> communities) {
      _communities.addAll(communities);
   }

   @Override
   public CommunitySet communities(Environment environment) {
      if (_cachedCommunities == null) {
         _cachedCommunities = initCommunities(environment);
      }
      return _cachedCommunities;
   }

   public Set<CommunitySetElem> getCommunities() {
      return _communities;
   }

   private synchronized CommunitySet initCommunities(Environment environment) {
      CommunitySet out = new CommunitySet();
      for (CommunitySetElem elem : _communities) {
         long c = elem.community(environment);
         out.add(c);
      }
      return out;
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

   public void setCommunities(Set<CommunitySetElem> communities) {
      _communities = communities;
   }

}
