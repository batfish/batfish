package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Set;

public final class AddressGroup implements Serializable {
  private static final long serialVersionUID = 1L;

  private String _description;

  private Set<String> _members;

  private final String _name;

  public AddressGroup(String name) {
    _name = name;
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
