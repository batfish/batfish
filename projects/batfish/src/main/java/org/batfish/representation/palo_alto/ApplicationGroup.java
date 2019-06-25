package org.batfish.representation.palo_alto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Represents a Palo Alto application-group */
public final class ApplicationGroup implements Serializable {

  private final Set<String> _members;

  private final String _name;

  public ApplicationGroup(String name) {
    _name = name;
    _members = new TreeSet<>();
  }

  /**
   * Returns all {@link Application applications} that are directly or indirectly contained in this
   * group. Accounts for circular group references.
   */
  @VisibleForTesting
  @SuppressWarnings("unused") // todo
  Set<String> getDescendantObjects(
      Map<String, Application> applications,
      Map<String, ApplicationGroup> applicationGroups,
      Set<String> alreadyTraversedGroups) {
    if (alreadyTraversedGroups.contains(_name)) {
      return ImmutableSet.of();
    }
    alreadyTraversedGroups.add(_name);
    Set<String> descendantObjects = new HashSet<>();
    for (String member : _members) {
      if (applications.containsKey(member)) {
        descendantObjects.add(member);
      } else if (applicationGroups.containsKey(member)) {
        descendantObjects.addAll(
            applicationGroups
                .get(member)
                .getDescendantObjects(applications, applicationGroups, alreadyTraversedGroups));
      }
    }
    return descendantObjects;
  }

  public Set<String> getMembers() {
    return _members;
  }

  public String getName() {
    return _name;
  }
}
