package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpSpace;

/** Represents a Palo Alto address group */
public final class AddressGroup implements Serializable {
  private static final long serialVersionUID = 1L;

  private String _description;

  private final Set<String> _members;

  private final String _name;

  public AddressGroup(String name) {
    _name = name;
    _members = new TreeSet<>();
  }

  /** Returns the union of IpSpace of all members */
  public IpSpace getIpSpace(Map<String, AddressObject> addressObjects) {
    return AclIpSpace.union(
        _members.stream()
            .map(m -> addressObjects.containsKey(m) ? addressObjects.get(m).getIpSpace() : null)
            .collect(Collectors.toList()));
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
