package org.batfish.datamodel.phc_to_flow;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Converter that picks a representative flow from {@link PacketHeaderConstraints} and a source
 * {@link Location} using {@link FieldExtractor} for each field.
 */
@ParametersAreNonnullByDefault
public final class PacketHeaderConstraintToFlowConverter {
  public static class Builder {

    public PacketHeaderConstraintToFlowConverter build() {
      checkArgument(_srcIpExtractor != null, "srcIp extractor cannot be null");
      checkArgument(_dstIpExtractor != null, "dstIp extractor cannot be null");
      return new PacketHeaderConstraintToFlowConverter(
          _srcIpExtractor,
          _dstIpExtractor,
          _ipProtocolExtractor,
          _srcPortExtractor,
          _dstPortExtractor,
          _icmpTypeExtractor,
          _icmpCodeExtractor);
    }

    public Builder setSrcIpExtractor(FieldExtractor<Ip> srcIpExtractor) {
      _srcIpExtractor = srcIpExtractor;
      return this;
    }

    public Builder setDstIpExtractor(FieldExtractor<Ip> dstIpExtractor) {
      _dstIpExtractor = dstIpExtractor;
      return this;
    }

    public Builder setDstPortExtractor(FieldExtractor<Integer> dstPortExtractor) {
      _dstPortExtractor = dstPortExtractor;
      return this;
    }

    public Builder setIpProtocolExtractor(FieldExtractor<IpProtocol> ipProtocolExtractor) {
      _ipProtocolExtractor = ipProtocolExtractor;
      return this;
    }

    public Builder setSrcPortExtractor(FieldExtractor<Integer> srcPortExtractor) {
      _srcPortExtractor = srcPortExtractor;
      return this;
    }

    public Builder setIcmpTypeExtractor(FieldExtractor<Integer> icmpTypeExtractor) {
      _icmpTypeExtractor = icmpTypeExtractor;
      return this;
    }

    public Builder setIcmpCodeExtractor(FieldExtractor<Integer> icmpCodeExtractor) {
      _icmpCodeExtractor = icmpCodeExtractor;
      return this;
    }

    private FieldExtractor<Ip> _srcIpExtractor;
    private FieldExtractor<Ip> _dstIpExtractor;
    private @Nonnull FieldExtractor<IpProtocol> _ipProtocolExtractor;
    private @Nonnull FieldExtractor<Integer> _srcPortExtractor;
    private @Nonnull FieldExtractor<Integer> _dstPortExtractor;
    private @Nonnull FieldExtractor<Integer> _icmpTypeExtractor;
    private @Nonnull FieldExtractor<Integer> _icmpCodeExtractor;

    private Builder() {
      _ipProtocolExtractor = IpProtocolExtractorDefault.TCP;
      _srcPortExtractor = SrcPortExtractorDefault.INSTANCE;
      _dstPortExtractor = DstPortExtractorDefault.HTTP;
      _icmpTypeExtractor = IcmpTypeExtractorDefault.REQUEST;
      _icmpCodeExtractor = IcmpCodeExtractorDefault.ZERO;
    }
  }

  public Flow.Builder toFlow(PacketHeaderConstraints phc, Location srcLocation) {
    Flow.Builder flowBuilder = Flow.builder();
    flowBuilder.setSrcIp(_srcIpExtractor.getValue(phc, srcLocation));
    flowBuilder.setDstIp(_dstIpExtractor.getValue(phc, srcLocation));
    flowBuilder.setIpProtocol(_ipProtocolExtractor.getValue(phc, srcLocation));
    flowBuilder.setSrcPort(_srcPortExtractor.getValue(phc, srcLocation));
    flowBuilder.setDstPort(_dstPortExtractor.getValue(phc, srcLocation));
    if (flowBuilder.getIpProtocol() == IpProtocol.ICMP) {
      flowBuilder.setIcmpType(_icmpTypeExtractor.getValue(phc, srcLocation));
      flowBuilder.setIcmpCode(_icmpCodeExtractor.getValue(phc, srcLocation));
    }
    return flowBuilder;
  }

  public static Builder builder() {
    return new Builder();
  }

  // FieldExtractor for essential fields
  private @Nonnull FieldExtractor<Ip> _srcIpExtractor;
  private @Nonnull FieldExtractor<Ip> _dstIpExtractor;
  private @Nonnull FieldExtractor<IpProtocol> _ipProtocolExtractor;
  private @Nonnull FieldExtractor<Integer> _srcPortExtractor;
  private @Nonnull FieldExtractor<Integer> _dstPortExtractor;
  private @Nonnull FieldExtractor<Integer> _icmpTypeExtractor;
  private @Nonnull FieldExtractor<Integer> _icmpCodeExtractor;

  private PacketHeaderConstraintToFlowConverter(
      FieldExtractor<Ip> srcIpExtractor,
      FieldExtractor<Ip> dstIpExtractor,
      FieldExtractor<IpProtocol> ipProtocolExtractor,
      FieldExtractor<Integer> srcPortExtractor,
      FieldExtractor<Integer> dstPortExtractor,
      FieldExtractor<Integer> icmpTypeExtractor,
      FieldExtractor<Integer> icmpCodeExtractor) {
    _srcIpExtractor = srcIpExtractor;
    _dstIpExtractor = dstIpExtractor;
    _ipProtocolExtractor = ipProtocolExtractor;
    _srcPortExtractor = srcPortExtractor;
    _dstPortExtractor = dstPortExtractor;
    _icmpTypeExtractor = icmpTypeExtractor;
    _icmpCodeExtractor = icmpCodeExtractor;
  }
}
