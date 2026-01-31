package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

/** Options available to all {@link ActionIpAccessListLine}s. */
public final class Layer3Options implements Serializable {

  public static final class Builder {

    private @Nullable Integer _dscp;
    private @Nullable IntegerSpace _packetLength;
    private @Nullable Integer _precedence;
    private @Nullable Integer _ttl;

    private Builder() {}

    public @Nonnull Layer3Options build() {
      return new Layer3Options(_dscp, _packetLength, _precedence, _ttl);
    }

    public @Nonnull Builder setDscp(@Nullable Integer dscp) {
      _dscp = dscp;
      return this;
    }

    public @Nonnull Builder setPacketLength(@Nullable IntegerSpace packetLength) {
      _packetLength = packetLength;
      return this;
    }

    public @Nonnull Builder setPrecedence(@Nullable Integer precedence) {
      _precedence = precedence;
      return this;
    }

    public @Nonnull Builder setTtl(@Nullable Integer ttl) {
      _ttl = ttl;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nullable Integer _dscp;
  private final @Nullable IntegerSpace _packetLength;
  private final @Nullable Integer _precedence;
  private final @Nullable Integer _ttl;

  private Layer3Options(Integer dscp, IntegerSpace packetLength, Integer precedence, Integer ttl) {
    _dscp = dscp;
    _packetLength = packetLength;
    _precedence = precedence;
    _ttl = ttl;
  }

  public Integer getDscp() {
    return _dscp;
  }

  public IntegerSpace getPacketLength() {
    return _packetLength;
  }

  public Integer getPrecedence() {
    return _precedence;
  }

  public Integer getTtl() {
    return _ttl;
  }
}
