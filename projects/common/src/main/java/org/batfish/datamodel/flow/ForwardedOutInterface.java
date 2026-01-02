package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A flow being forwarded out an interface. */
public final class ForwardedOutInterface implements ForwardingDetail {

  public static @Nonnull ForwardedOutInterface of(String outputInterface) {
    return new ForwardedOutInterface(outputInterface, null);
  }

  public static @Nonnull ForwardedOutInterface of(String outputInterface, Ip resolvedNextHopIp) {
    checkArgument(
        !resolvedNextHopIp.equals(Ip.AUTO),
        "Expected concrete resolvedNextHopIp, but got %s",
        resolvedNextHopIp);
    return new ForwardedOutInterface(outputInterface, resolvedNextHopIp);
  }

  private ForwardedOutInterface(String outputInterface, @Nullable Ip resolvedNextHopIp) {
    _resolvedNextHopIp = resolvedNextHopIp;
    _outputInterface = outputInterface;
  }

  @JsonCreator
  private static @Nonnull ForwardedOutInterface create(
      @JsonProperty(PROP_RESOLVED_NEXT_HOP_IP) @Nullable Ip resolvedNextHopIp,
      @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable String outputInterface) {
    checkArgument(outputInterface != null, "Missing %s", PROP_OUTPUT_INTERFACE);
    return resolvedNextHopIp == null ? of(outputInterface) : of(outputInterface, resolvedNextHopIp);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ForwardedOutInterface)) {
      return false;
    }
    ForwardedOutInterface that = (ForwardedOutInterface) o;
    return _outputInterface.equals(that._outputInterface)
        && Objects.equals(_resolvedNextHopIp, that._resolvedNextHopIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_resolvedNextHopIp, _outputInterface);
  }

  @JsonProperty(PROP_OUTPUT_INTERFACE)
  public @Nonnull String getOutputInterface() {
    return _outputInterface;
  }

  @JsonIgnore
  public @Nonnull Optional<Ip> getResolvedNextHopIp() {
    return Optional.ofNullable(_resolvedNextHopIp);
  }

  @JsonProperty(PROP_RESOLVED_NEXT_HOP_IP)
  private @Nullable Ip getResolvedNextHopIpJackson() {
    return _resolvedNextHopIp;
  }

  private static final String PROP_OUTPUT_INTERFACE = "outputInterface";
  private static final String PROP_RESOLVED_NEXT_HOP_IP = "resolvedNextHopIp";

  private final @Nonnull String _outputInterface;
  private final @Nullable Ip _resolvedNextHopIp;
}
