package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.TcpFlags;

/** TCP options for an {@link ActionIpAccessListLine}. */
public final class TcpOptions implements Layer4Options {

  public static final class Builder {

    private @Nullable PortSpec _dstPortSpec;
    private boolean _established;
    private @Nullable HttpMethod _httpMethod;
    private @Nullable PortSpec _srcPortSpec;
    private @Nullable TcpFlags _tcpFlags;
    private @Nullable Integer _tcpFlagsMask;
    private @Nullable Integer _tcpOptionLength;

    private Builder() {}

    public @Nonnull TcpOptions build() {
      return new TcpOptions(
          _dstPortSpec,
          _established,
          _httpMethod,
          _srcPortSpec,
          _tcpFlags,
          _tcpFlagsMask,
          _tcpOptionLength);
    }

    public @Nonnull Builder setDstPortSpec(PortSpec dstPortSpec) {
      _dstPortSpec = dstPortSpec;
      return this;
    }

    public @Nonnull Builder setEstablished(boolean established) {
      _established = established;
      return this;
    }

    public @Nonnull Builder setHttpMethod(HttpMethod httpMethod) {
      _httpMethod = httpMethod;
      return this;
    }

    public @Nonnull Builder setSrcPortSpec(PortSpec srcPortSpec) {
      _srcPortSpec = srcPortSpec;
      return this;
    }

    public @Nonnull Builder setTcpFlags(TcpFlags tcpFlags) {
      _tcpFlags = tcpFlags;
      return this;
    }

    public @Nonnull Builder setTcpFlagsMask(@Nullable Integer tcpFlagsMask) {
      _tcpFlagsMask = tcpFlagsMask;
      return this;
    }

    public @Nonnull Builder setTcpOptionLength(Integer tcpOptionLength) {
      _tcpOptionLength = tcpOptionLength;
      return this;
    }
  }

  public static final @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nullable PortSpec _dstPortSpec;
  private final boolean _established;
  private final @Nullable HttpMethod _httpMethod;
  private final @Nullable PortSpec _srcPortSpec;
  private final @Nullable TcpFlags _tcpFlags;
  private final @Nullable Integer _tcpFlagsMask;
  private final @Nullable Integer _tcpOptionLength;

  private TcpOptions(
      @Nullable PortSpec dstPortSpec,
      boolean established,
      @Nullable HttpMethod httpMethod,
      @Nullable PortSpec srcPortSpec,
      @Nullable TcpFlags tcpFlags,
      Integer tcpFlagsMask,
      @Nullable Integer tcpOptionLength) {
    _dstPortSpec = dstPortSpec;
    _established = established;
    _httpMethod = httpMethod;
    _srcPortSpec = srcPortSpec;
    _tcpFlags = tcpFlags;
    _tcpFlagsMask = tcpFlagsMask;
    _tcpOptionLength = tcpOptionLength;
  }

  @Override
  public <T> T accept(Layer4OptionsVisitor<T> visitor) {
    return visitor.visitTcpOptions(this);
  }

  public @Nullable PortSpec getDstPortSpec() {
    return _dstPortSpec;
  }

  /** Match packets with either ACK or RST bits set. */
  // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus5000/sw/command/reference/security/n5k-sec-cr/n5k-sec_cmds_d.html#wp2654002
  public boolean getEstablished() {
    return _established;
  }

  public @Nullable HttpMethod getHttpMethod() {
    return _httpMethod;
  }

  public @Nullable PortSpec getSrcPortSpec() {
    return _srcPortSpec;
  }

  public @Nullable TcpFlags getTcpFlags() {
    return _tcpFlags;
  }

  public @Nullable Integer getTcpFlagsMask() {
    return _tcpFlagsMask;
  }

  public @Nullable Integer getTcpOptionLength() {
    return _tcpOptionLength;
  }
}
