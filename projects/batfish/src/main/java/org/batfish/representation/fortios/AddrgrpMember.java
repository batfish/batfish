package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * FortiOS datamodel interface representing objects which can be a member of firewall addrgrps
 * (address groups)
 */
public abstract class AddrgrpMember implements FortiosRenameableObject, Serializable {

  public static final boolean DEFAULT_FABRIC_OBJECT = false;

  public @Nullable String getComment() {
    return _comment;
  }

  public @Nullable Boolean getFabricObject() {
    return _fabricObject;
  }

  public boolean getFabricObjectEffective() {
    return firstNonNull(_fabricObject, DEFAULT_FABRIC_OBJECT);
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  public void setFabricObject(boolean fabricObject) {
    _fabricObject = fabricObject;
  }

  @Nullable private String _comment;
  @Nullable private Boolean _fabricObject;
}
