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

  @VisibleForTesting
  Set<Application> getDescendantObjects(
      Map<String, Application> applications,
      Map<String, ApplicationGroup> applicationGroups,
      Set<String> alreadyTraversedGroups) {
    if (alreadyTraversedGroups.contains(_name)) {
      return ImmutableSet.of();
    }
    alreadyTraversedGroups.add(_name);
    Set<Application> descendantObjects = new HashSet<>();
    for (String member : _members) {
      if (applications.containsKey(member)) {
        descendantObjects.add(applications.get(member));
      } else if (applicationGroups.containsKey(member)) {
        descendantObjects.addAll(
            applicationGroups
                .get(member)
                .getDescendantObjects(applications, applicationGroups, alreadyTraversedGroups));
      } else if (ApplicationBuiltIn.getBuiltInApplication(member).isPresent()) {
        descendantObjects.add(ApplicationBuiltIn.getBuiltInApplication(member).get());
      }
    }
    return descendantObjects;
  }

  /**
   * Returns all {@link Application applications} that are directly or indirectly contained in this
   * group. Accounts for circular group references.
   */
  public Set<Application> getDescendantObjects(
      Map<String, Application> applications, Map<String, ApplicationGroup> applicationGroups) {
    return getDescendantObjects(applications, applicationGroups, new HashSet<>());
  }

  public Set<String> getMembers() {
    return _members;
  }

  public String getName() {
    return _name;
  }
}
