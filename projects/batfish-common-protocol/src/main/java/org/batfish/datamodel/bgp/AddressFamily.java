package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Base class for all BGP address family config */
@ParametersAreNonnullByDefault
public abstract class AddressFamily implements Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @JsonIgnore
  public abstract Type getType();

  /** BGP address family type */
  public enum Type {
    IPV4_UNICAST,
    EVPN
  }
}
