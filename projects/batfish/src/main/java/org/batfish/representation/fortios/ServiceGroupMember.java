package org.batfish.representation.fortios;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;

/**
 * FortiOS datamodel interface representing objects which can be a member of firewall service groups
 */
public abstract class ServiceGroupMember implements FortiosRenameableObject, Serializable {
  @Nullable
  public String getComment() {
    return _comment;
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  public abstract Stream<HeaderSpace> toHeaderSpaces(
      Map<String, ServiceGroupMember> serviceGroupMembers);

  @Nullable private String _comment;
}
