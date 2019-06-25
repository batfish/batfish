package org.batfish.representation.palo_alto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;

/** Represents a Palo Alto address group */
public final class AddressGroup implements Serializable {

  private String _description;

  private final Set<String> _members;

  private final String _name;

  public AddressGroup(String name) {
    _name = name;
    _members = new TreeSet<>();
  }

  /**
   * Returns all address objects that are directly or indirectly containted in this group. Accounts
   * for circular group references.
   */
  @VisibleForTesting
  Set<String> getDescendantObjects(
      Map<String, AddressObject> addressObjects,
      Map<String, AddressGroup> addressGroups,
      Set<String> alreadyTraversedGroups) {
    if (alreadyTraversedGroups.contains(_name)) {
      return ImmutableSet.of();
    }
    alreadyTraversedGroups.add(_name);
    Set<String> descendantObjects = new HashSet<>();
    for (String member : _members) {
      if (addressObjects.containsKey(member)) {
        descendantObjects.add(member);
      } else if (addressGroups.containsKey(member)) {
        descendantObjects.addAll(
            addressGroups
                .get(member)
                .getDescendantObjects(addressObjects, addressGroups, alreadyTraversedGroups));
      }
    }
    return descendantObjects;
  }

  /**
   * Returns the union of IpSpace of all members. Returns {@link EmptyIpSpace} if there are no
   * members
   */
  public IpSpace getIpSpace(
      Map<String, AddressObject> addressObjects, Map<String, AddressGroup> addressGroups) {
    Set<String> descendantObjects =
        getDescendantObjects(addressObjects, addressGroups, new HashSet<>());
    IpSpace space =
        AclIpSpace.union(
            descendantObjects.stream()
                .map(m -> addressObjects.get(m).getIpSpace())
                .collect(Collectors.toSet()));
    return space == null ? EmptyIpSpace.INSTANCE : space;
  }

  public String getDescription() {
    return _description;
  }

  public Set<String> getMembers() {
    return _members;
  }

  public String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
