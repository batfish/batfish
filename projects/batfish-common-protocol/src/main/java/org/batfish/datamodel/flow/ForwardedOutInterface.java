package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A flow being forwarded out an interface. */
public final class ForwardedOutInterface implements ForwardingDetail {

  public static @Nonnull ForwardedOutInterface of(Ip arpIp, String outputInterface) {
    return new ForwardedOutInterface(arpIp, outputInterface);
  }

  public ForwardedOutInterface(Ip arpIp, String outputInterface) {
    _arpIp = arpIp;
    _outputInterface = outputInterface;
  }

  @JsonCreator
  private static @Nonnull ForwardedOutInterface create(
      @JsonProperty(PROP_ARP_IP) @Nullable Ip arpIp,
      @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable String outputInterface) {
    checkArgument(arpIp != null, "Missing %s", PROP_ARP_IP);
    checkArgument(outputInterface != null, "Missing %s", PROP_OUTPUT_INTERFACE);
    return of(arpIp, outputInterface);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ForwardedOutInterface)) {
      return false;
    }
    ForwardedOutInterface that = (ForwardedOutInterface) o;
    return _arpIp.equals(that._arpIp) && _outputInterface.equals(that._outputInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_arpIp, _outputInterface);
  }

  @JsonProperty(PROP_ARP_IP)
  @Nonnull
  public Ip getArpIp() {
    return _arpIp;
  }

  @JsonProperty(PROP_OUTPUT_INTERFACE)
  @Nonnull
  public String getOutputInterface() {
    return _outputInterface;
  }

  private static final String PROP_ARP_IP = "arpIp";
  private static final String PROP_OUTPUT_INTERFACE = "outputInterface";

  private final @Nonnull Ip _arpIp;
  private final @Nonnull String _outputInterface;
}
