package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.routing_policy.Environment;

public class InlineCommunitySet extends CommunitySetExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private final Supplier<SortedSet<Long>> _cachedCommunities;

  private List<CommunitySetElem> _communities;

  @JsonCreator
  private InlineCommunitySet() {
    _cachedCommunities = Suppliers.memoize(this::initCommunities);
  }

  public InlineCommunitySet(Collection<Long> communities) {
    this();
    _communities = communities.stream().map(CommunitySetElem::new).collect(Collectors.toList());
  }

  public InlineCommunitySet(List<CommunitySetElem> communities) {
    this();
    _communities = ImmutableList.copyOf(communities);
  }

  @Override
  public SortedSet<Long> allCommunities(Environment environment) {
    return _cachedCommunities.get();
  }

  @Override
  public SortedSet<Long> communities(
      Environment environment, Collection<Long> communityCandidates) {
    return CommonUtil.intersection(allCommunities(environment), communityCandidates, TreeSet::new);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InlineCommunitySet)) {
      return false;
    }
    InlineCommunitySet other = (InlineCommunitySet) obj;
    return Objects.equals(_communities, other._communities);
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

  private SortedSet<Long> initCommunities() {
    SortedSet<Long> out = new TreeSet<>();
    for (CommunitySetElem elem : _communities) {
      long c = elem.community();
      out.add(c);
    }
    return out;
  }

  public void setCommunities(List<CommunitySetElem> communities) {
    _communities = communities;
  }
}
