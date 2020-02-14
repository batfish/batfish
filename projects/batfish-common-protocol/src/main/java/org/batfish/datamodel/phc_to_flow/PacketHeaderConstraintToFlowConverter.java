package org.batfish.datamodel.phc_to_flow;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ip.Ip;
import org.batfish.datamodel.Flow;
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
      return new PacketHeaderConstraintToFlowConverter(_srcIpExtractor, _dstIpExtractor);
    }

    public Builder setSrcIpExtractor(FieldExtractor<Ip> srcIpExtractor) {
      _srcIpExtractor = srcIpExtractor;
      return this;
    }

    public Builder setDstIpExtractor(FieldExtractor<Ip> dstIpExtractor) {
      _dstIpExtractor = dstIpExtractor;
      return this;
    }

    private FieldExtractor<Ip> _srcIpExtractor;
    private FieldExtractor<Ip> _dstIpExtractor;

    private Builder() {}
  }

  public Flow.Builder toFlow(PacketHeaderConstraints phc, Location srcLocation) {
    Flow.Builder flowBuilder = Flow.builder();
    flowBuilder.setSrcIp(_srcIpExtractor.getValue(phc, srcLocation));
    flowBuilder.setDstIp(_dstIpExtractor.getValue(phc, srcLocation));
    return flowBuilder;
  }

  public static Builder builder() {
    return new Builder();
  }

  // FieldExtractor for essential fields
  private @Nonnull FieldExtractor<Ip> _srcIpExtractor;
  private @Nonnull FieldExtractor<Ip> _dstIpExtractor;

  private PacketHeaderConstraintToFlowConverter(
      FieldExtractor<Ip> srcIpExtractor, FieldExtractor<Ip> dstIpExtractor) {
    _srcIpExtractor = srcIpExtractor;
    _dstIpExtractor = dstIpExtractor;
  }
}
