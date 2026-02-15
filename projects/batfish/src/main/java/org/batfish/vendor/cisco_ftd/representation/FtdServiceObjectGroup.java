package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Cisco FTD service object-group. */
public class FtdServiceObjectGroup implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull List<FtdServiceObjectGroupMember> _members;
  private @Nullable String _protocol;

  public FtdServiceObjectGroup(@Nonnull String name) {
    _name = name;
    _members = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getProtocol() {
    return _protocol;
  }

  public void setProtocol(@Nullable String protocol) {
    _protocol = protocol;
  }

  public @Nonnull List<FtdServiceObjectGroupMember> getMembers() {
    return _members;
  }

  public void addMember(@Nonnull FtdServiceObjectGroupMember member) {
    _members.add(member);
  }

  @Override
  public String toString() {
    return String.format("ServiceObjectGroup: %s (%d members)", _name, _members.size());
  }
}
