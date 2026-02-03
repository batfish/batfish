package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Cisco FTD network object-group. */
public class FtdNetworkObjectGroup implements Serializable {

  private final @Nonnull String _name;
  private @Nullable String _description;
  private final @Nonnull List<FtdNetworkObjectGroupMember> _members;

  public FtdNetworkObjectGroup(@Nonnull String name) {
    _name = name;
    _members = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nonnull List<FtdNetworkObjectGroupMember> getMembers() {
    return _members;
  }

  public void addMember(@Nonnull FtdNetworkObjectGroupMember member) {
    _members.add(member);
  }

  @Override
  public String toString() {
    return String.format("NetworkObjectGroup: %s (%d members)", _name, _members.size());
  }
}
