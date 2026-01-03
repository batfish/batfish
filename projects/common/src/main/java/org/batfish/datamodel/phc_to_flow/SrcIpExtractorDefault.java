package org.batfish.datamodel.phc_to_flow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Extract source Ip from {@link PacketHeaderConstraints} if possible; otherwise infer an Ip from
 * the source {@link Location}. If all inference fails, use a default Ip as the source Ip.
 */
// Used in testFilter
@ParametersAreNonnullByDefault
public final class SrcIpExtractorDefault implements FieldExtractor<Ip> {
  public SrcIpExtractorDefault(IpFieldExtractorContext helper) {
    _helper = helper;
  }

  @Override
  public Ip getValue(PacketHeaderConstraints phc, @Nullable Location srcLocation) {
    // Extract source IP from header constraints,
    String headerSrcIp = phc.getSrcIps();

    return headerSrcIp != null
        ? _helper.inferSrcIpFromHeaderSrcIp(headerSrcIp)
        : srcLocation != null
            ? _helper.inferSrcIpFromSourceLocation(srcLocation)
            : DEFAULT_IP_ADDRESS;
  }

  private static final Ip DEFAULT_IP_ADDRESS = Ip.parse("8.8.8.8");
  private @Nonnull IpFieldExtractorContext _helper;
}
