package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.routing_policy.Environment;

public class InlineCommunitySet extends CommunitySetExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private transient SortedSet<Long> _cachedCommunities;

  private List<CommunitySetElem> _communities;

  @JsonCreator
  private InlineCommunitySet() {}

  public InlineCommunitySet(Collection<Long> communities) {
    _communities =
        communities.stream().map(l -> new CommunitySetElem(l)).collect(Collectors.toList());
  }

  public InlineCommunitySet(List<CommunitySetElem> communities) {
    _communities = new ArrayList<>();
    _communities.addAll(communities);
  }

  @Override
  public SortedSet<Long> communities(Environment environment) {
    if (_cachedCommunities == null) {
      _cachedCommunities = initCommunities(environment);
    }
    return Collections.unmodifiableSortedSet(new TreeSet<>(_cachedCommunities));
  }

  @Override
  public SortedSet<Long> communities(Environment environment, SortedSet<Long> communityCandidates) {
    return CommonUtil.intersection(communities(environment), communityCandidates, TreeSet::new);
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
    } else if (!_communities.equals(other._communities)) {
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
    result = prime * result + ((_communities == null) ? 0 : _communities.hashCode());
    return result;
  }

  private synchronized SortedSet<Long> initCommunities(Environment environment) {
    SortedSet<Long> out = new TreeSet<>();
    for (CommunitySetElem elem : _communities) {
      long c = elem.community(environment);
      out.add(c);
    }
    return out;
  }

  @Override
  public boolean matchSingleCommunity(Environment environment, SortedSet<Long> communities) {
    return _communities.stream().anyMatch(c -> communities.contains(c.community(environment)));
  }

  public void setCommunities(List<CommunitySetElem> communities) {
    _communities = communities;
  }
}
