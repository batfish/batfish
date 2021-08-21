package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model for the response to the {@code show-gateways-and-servers} command. */
public final class GatewaysAndServers implements Serializable {

  @VisibleForTesting
  GatewaysAndServers(Map<Uid, GatewayOrServer> gatewaysAndServers) {
    _gatewaysAndServers = gatewaysAndServers;
  }

  @JsonCreator
  private static @Nonnull GatewaysAndServers create(
      @JsonProperty(PROP_OBJECTS) @Nullable List<GatewayOrServer> objects) {
    checkArgument(objects != null, "Missing %s", PROP_OBJECTS);
    Map<Uid, GatewayOrServer> gatewaysAndServers =
        objects.stream()
            .collect(ImmutableMap.toImmutableMap(GatewayOrServer::getUid, Function.identity()));
    return new GatewaysAndServers(gatewaysAndServers);
  }

  public @Nonnull Map<Uid, GatewayOrServer> getGatewaysAndServers() {
    return _gatewaysAndServers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof GatewaysAndServers)) {
      return false;
    }
    GatewaysAndServers that = (GatewaysAndServers) o;
    return _gatewaysAndServers.equals(that._gatewaysAndServers);
  }

  @Override
  public int hashCode() {
    return _gatewaysAndServers.hashCode();
  }

  private static final String PROP_OBJECTS = "objects";

  private final @Nonnull Map<Uid, GatewayOrServer> _gatewaysAndServers;

  @Override
  public String toString() {
    return toStringHelper(this).add("_gatewaysAndServers", _gatewaysAndServers).toString();
  }
}
