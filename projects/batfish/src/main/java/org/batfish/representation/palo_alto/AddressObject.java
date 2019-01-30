package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;

/** Represents a Palo Alto address object */
public final class AddressObject implements Serializable {
  private static final long serialVersionUID = 1L;

  private String _description;

  private IpSpace _ipSpace;

  private final String _name;

  public AddressObject(String name) {
    this(name, EmptyIpSpace.INSTANCE);
  }

  public AddressObject(String name, IpSpace ipSpace) {
    _name = name;
    _ipSpace = ipSpace;
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

  public void setDescription(String description) {
    _description = description;
  }

  public void setIpSpace(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }
}
