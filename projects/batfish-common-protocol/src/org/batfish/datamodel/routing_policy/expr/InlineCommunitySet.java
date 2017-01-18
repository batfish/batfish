package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class InlineCommunitySet extends CommunitySetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient CommunitySet _cachedCommunities;

   private List<CommunitySetElem> _communities;

   @JsonCreator
   private InlineCommunitySet() {
   }

   public InlineCommunitySet(Collection<Long> communities) {
      _communities = communities.stream().map(l -> new CommunitySetElem(l))
            .collect(Collectors.toList());
   }

   public InlineCommunitySet(List<CommunitySetElem> communities) {
      _communities = new ArrayList<>();
      _communities.addAll(communities);
   }

   @Override
   public CommunitySet communities(Environment environment) {
      if (_cachedCommunities == null) {
         _cachedCommunities = initCommunities(environment);
      }
      return _cachedCommunities;
   }

   @Override
   public CommunitySet communities(Environment environment,
         CommunitySet communityCandidates) {
      if (_cachedCommunities == null) {
         _cachedCommunities = initCommunities(environment);
      }
      CommunitySet matchingCommunities = new CommunitySet(_cachedCommunities);
      matchingCommunities.retainAll(communityCandidates);
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
      InlineCommunitySet other = (InlineCommunitySet) obj;
      if (_communities == null) {
         if (other._communities != null) {
            return false;
         }
      }
      else if (!_communities.equals(other._communities)) {
         return false;
      }
      return true;
   }

   public List<CommunitySetElem> getCommunities() {
      return _communities;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_communities == null) ? 0 : _communities.hashCode());
      return result;
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

   public void setCommunities(List<CommunitySetElem> communities) {
      _communities = communities;
   }

}
