package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.List;

public final class AddressGroup implements Serializable {
  private static final long serialVersionUID = 1L;

  private String _description;

  private List<String> _members;

  private final String _name;

  public AddressGroup(String name) {
    _name = name;
  }

  public String getDescription() {
    return _description;
  }

  public List<String> getMembers() {
    return _members;
  }

  public String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setMembers(List<String> members) {
    _members = members;
  }
}
