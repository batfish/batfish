package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;

public final class AddressObject implements Serializable {
  private static final long serialVersionUID = 1L;

  @Nullable private String _description;

  private IpSpace _ipSpace;

  private final String _name;

  public AddressObject(String name) {
    _name = name;
    _ipSpace = EmptyIpSpace.INSTANCE;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  @Nullable
  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  public String getName() {
    return _name;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setIpSpace(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }
}
