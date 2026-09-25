package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Public key infrastructure certificate authority profile. */
public final class PkiCaProfile implements Serializable {

  public PkiCaProfile(String name) {
    _name = name;
  }

  public @Nullable String getCaIdentity() {
    return _caIdentity;
  }

  public String getName() {
    return _name;
  }

  public boolean getRevocationCheckDisabled() {
    return _revocationCheckDisabled;
  }

  public @Nullable String getRoutingInstance() {
    return _routingInstance;
  }

  public void setCaIdentity(String caIdentity) {
    _caIdentity = caIdentity;
  }

  public void setRevocationCheckDisabled() {
    _revocationCheckDisabled = true;
  }

  public void setRoutingInstance(String routingInstance) {
    _routingInstance = routingInstance;
  }

  private @Nullable String _caIdentity;
  private final String _name;
  private boolean _revocationCheckDisabled;
  private @Nullable String _routingInstance;
}
