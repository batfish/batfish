package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/**
 * Represents the settings of a ACL_RULE:
 * https://github.com/Azure/SONiC/wiki/Configuration#acl-and-mirroring
 */
public class AclRule implements Serializable {

  public enum PacketAction {
    DROP,
    ACCEPT,
    FORWARD
  }

  private static final String PROP_IP_PROTOCOL = "IP_PROTOCOL";
  private static final String PROP_DST_IP = "DST_IP";
  private static final String PROP_SRC_IP = "SRC_IP";
  private static final String PROP_L4_DST_PORT = "L4_DST_PORT";
  private static final String PROP_L4_SRC_PORT = "L4_SRC_PORT";
  private static final String PROP_PACKET_ACTION = "PACKET_ACTION";
  private static final String PROP_PRIORITY = "PRIORITY";

  private @Nullable final Integer _ipProtocol;
  private @Nullable final Prefix _dstIp;
  private @Nullable final Prefix _srcIp;
  private @Nullable final Integer _l4DstPort;
  private @Nullable final Integer _l4SrcPort;
  private @Nullable final Integer _priority;
  private @Nullable final PacketAction _packetAction;

  @JsonCreator
  private @Nonnull static AclRule create(
      @Nullable @JsonProperty(PROP_IP_PROTOCOL) Integer ipProtocol,
      @Nullable @JsonProperty(PROP_DST_IP) Prefix dstIp,
      @Nullable @JsonProperty(PROP_SRC_IP) Prefix srcIp,
      @Nullable @JsonProperty(PROP_L4_DST_PORT) Integer l4DstPort,
      @Nullable @JsonProperty(PROP_L4_SRC_PORT) Integer l4SrcPort,
      @Nullable @JsonProperty(PROP_PRIORITY) Integer priority,
      @Nullable @JsonProperty(PROP_PACKET_ACTION) PacketAction packetAction) {
    return AclRule.builder()
        .setIpProtocol(ipProtocol)
        .setDstIp(dstIp)
        .setSrcIp(srcIp)
        .setL4DstPort(l4DstPort)
        .setL4SrcPort(l4SrcPort)
        .setPriority(priority)
        .setPacketAction(packetAction)
        .build();
  }

  private AclRule(
      @Nullable Integer ipProtocol,
      @Nullable Prefix dstIp,
      @Nullable Prefix srcIp,
      @Nullable Integer l4DstPort,
      @Nullable Integer l4SrcPort,
      @Nullable Integer priority,
      @Nullable PacketAction packetAction) {
    _ipProtocol = ipProtocol;
    _dstIp = dstIp;
    _srcIp = srcIp;
    _l4DstPort = l4DstPort;
    _l4SrcPort = l4SrcPort;
    _priority = priority;
    _packetAction = packetAction;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AclRule)) {
      return false;
    }
    AclRule aclRule = (AclRule) o;
    return Objects.equals(_ipProtocol, aclRule._ipProtocol)
        && Objects.equals(_dstIp, aclRule._dstIp)
        && Objects.equals(_srcIp, aclRule._srcIp)
        && Objects.equals(_l4DstPort, aclRule._l4DstPort)
        && Objects.equals(_l4SrcPort, aclRule._l4SrcPort)
        && Objects.equals(_priority, aclRule._priority)
        && _packetAction == aclRule._packetAction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _ipProtocol, _dstIp, _srcIp, _l4DstPort, _l4SrcPort, _priority, _packetAction);
  }

  public @Nonnull static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Integer _ipProtocol;
    private Prefix _dstIp;
    private Prefix _srcIp;
    private Integer _l4DstPort;
    private Integer _l4SrcPort;
    private Integer _priority;
    private PacketAction _packetAction;

    public @Nonnull Builder setIpProtocol(@Nullable Integer ipProtocol) {
      this._ipProtocol = ipProtocol;
      return this;
    }

    public @Nonnull Builder setDstIp(@Nullable Prefix dstIp) {
      this._dstIp = dstIp;
      return this;
    }

    public @Nonnull Builder setSrcIp(@Nullable Prefix srcIp) {
      this._srcIp = srcIp;
      return this;
    }

    public @Nonnull Builder setL4DstPort(@Nullable Integer l4DstPort) {
      this._l4DstPort = l4DstPort;
      return this;
    }

    public @Nonnull Builder setL4SrcPort(@Nullable Integer l4SrcPort) {
      this._l4SrcPort = l4SrcPort;
      return this;
    }

    public @Nonnull Builder setPriority(@Nullable Integer priority) {
      this._priority = priority;
      return this;
    }

    public @Nonnull Builder setPacketAction(@Nullable PacketAction packetAction) {
      this._packetAction = packetAction;
      return this;
    }

    public @Nonnull AclRule build() {
      return new AclRule(
          _ipProtocol, _dstIp, _srcIp, _l4DstPort, _l4SrcPort, _priority, _packetAction);
    }
  }
}
