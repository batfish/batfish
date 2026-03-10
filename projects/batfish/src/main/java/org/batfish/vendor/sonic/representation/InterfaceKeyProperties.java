package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the value of the key in SONiC's multi-level interface|address (e.g.,
 * Ethernet1|1.1.1.1) encoding.
 */
public class InterfaceKeyProperties implements Serializable {

  /** Returns null if the property was not set */
  public @Nullable List<String> getForcedMgmtRoutes() {
    return _forcedMgmtRoutes;
  }

  public @Nullable String getGwAddr() {
    return _gwAddr;
  }

  public @Nullable Boolean getSecondary() {
    return _secondary;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceKeyProperties)) {
      return false;
    }
    InterfaceKeyProperties that = (InterfaceKeyProperties) o;
    return Objects.equals(_forcedMgmtRoutes, that._forcedMgmtRoutes)
        && Objects.equals(_gwAddr, that._gwAddr)
        && Objects.equals(_secondary, that._secondary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_forcedMgmtRoutes, _gwAddr, _secondary);
  }

  @JsonCreator
  private static InterfaceKeyProperties create(
      @JsonProperty(PROP_FORCED_MGMT_ROUTES) @Nullable List<String> forcedMgmtRoutes,
      @JsonProperty(PROP_GWADDR) @Nullable String gwaddr,
      @JsonProperty(PROP_SECONDARY) @Nullable String secondary) {
    // we do not parse strings inside forcedMgmtRoutes and gwAddr (which are v4 or v6 address)
    // they are not convert these properties
    return InterfaceKeyProperties.builder()
        .setForcedMgmtRoutes(forcedMgmtRoutes)
        .setGwAddr(gwaddr)
        .setSecondary(Optional.ofNullable(secondary).map("true"::equals).orElse(null))
        .build();
  }

  private InterfaceKeyProperties(
      @Nullable List<String> forcedMgmtRoutes,
      @Nullable String gwAddr,
      @Nullable Boolean secondary) {
    _forcedMgmtRoutes = forcedMgmtRoutes;
    _gwAddr = gwAddr;
    _secondary = secondary;
  }

  private static final String PROP_FORCED_MGMT_ROUTES = "forced_mgmt_routes";
  private static final String PROP_GWADDR = "gwaddr";
  private static final String PROP_SECONDARY = "secondary";

  private final @Nullable List<String> _forcedMgmtRoutes;
  private final @Nullable String _gwAddr;
  private final @Nullable Boolean _secondary;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("forcedMgmtRoutes", _forcedMgmtRoutes)
        .add("gwAddr", _gwAddr)
        .add("secondary", _secondary)
        .toString();
  }

  public static final class Builder {
    private @Nullable List<String> _forcedMgmtRoutes;
    private @Nullable String _gwAddr;
    private @Nullable Boolean _secondary;

    public @Nonnull Builder setForcedMgmtRoutes(@Nullable List<String> forcedMgmtRoutes) {
      _forcedMgmtRoutes = forcedMgmtRoutes;
      return this;
    }

    public @Nonnull Builder setGwAddr(@Nullable String gwAddr) {
      _gwAddr = gwAddr;
      return this;
    }

    public @Nonnull Builder setSecondary(@Nullable Boolean secondary) {
      this._secondary = secondary;
      return this;
    }

    public @Nonnull InterfaceKeyProperties build() {
      return new InterfaceKeyProperties(_forcedMgmtRoutes, _gwAddr, _secondary);
    }
  }
}
