package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.routing_policy.Environment;

public class NamedCommunitySet extends CommunitySetExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  @JsonCreator
  private NamedCommunitySet() {}

  public NamedCommunitySet(String name) {
    _name = name;
  }

  @Override
  public SortedSet<Long> allCommunities(Environment environment) {
    SortedSet<Long> out = new TreeSet<>();
    CommunityList cl = environment.getConfiguration().getCommunityLists().get(_name);
    for (CommunityListLine line : cl.getLines()) {
      Long community = line.toLiteralCommunity();
      out.add(community);
    }
    return Collections.unmodifiableSortedSet(out);
  }

  @Override
  public SortedSet<Long> communities(
      Environment environment, Collection<Long> communityCandidates) {
    SortedSet<Long> matchingCommunities = new TreeSet<>();
    for (Long community : communityCandidates) {
      CommunityList cl = environment.getConfiguration().getCommunityLists().get(_name);
      if (cl.permits(community)) {
        matchingCommunities.add(community);
      }
    }
    return Collections.unmodifiableSortedSet(matchingCommunities);
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
    } else if (!_name.equals(other._name)) {
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

  public void setName(String name) {
    _name = name;
  }
}
