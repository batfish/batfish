package org.batfish.datamodel.phc_to_flow;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Extract destination Ip from from {@link PacketHeaderConstraints}; if failed then use a default
 * Ip.
 */
@ParametersAreNonnullByDefault
public final class DstIpExtractorDefault implements FieldExtractor<Ip> {
  public DstIpExtractorDefault(IpFieldExtractorContext helper) {
    _helper = helper;
  }

  @Override
  public Ip getValue(PacketHeaderConstraints phc, Location srcLocation) {
    String headerDstIp = phc.getDstIps();
    return Optional.ofNullable(headerDstIp)
        .map(_helper::inferDstIpFromHeaderDstIp)
        .orElse(DEFAULT_IP_ADDRESS);
  }

  private static final Ip DEFAULT_IP_ADDRESS = Ip.parse("8.8.8.8");
  private final @Nonnull IpFieldExtractorContext _helper;
}
