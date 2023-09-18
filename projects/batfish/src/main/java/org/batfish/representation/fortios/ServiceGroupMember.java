package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * FortiOS datamodel interface representing objects which can be a member of firewall service groups
 */
public abstract class ServiceGroupMember implements FortiosRenameableObject, Serializable {
  public @Nullable String getComment() {
    return _comment;
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  private @Nullable String _comment;
}
