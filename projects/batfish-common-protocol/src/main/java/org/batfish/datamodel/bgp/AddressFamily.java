package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Base class for all BGP address family config */
@ParametersAreNonnullByDefault
public abstract class AddressFamily implements Serializable {

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @JsonIgnore
  public abstract Type getType();

  /** Builder for an {@link AddressFamily} */
  public abstract static class Builder<B extends Builder<B, F>, F extends AddressFamily> {
    @Nonnull
    public abstract B getThis();

    @Nonnull
    public abstract F build();
  }

  /** BGP address family type */
  public enum Type {
    IPV4_UNICAST,
    EVPN
  }
}
