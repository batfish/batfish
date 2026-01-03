package org.batfish.datamodel.phc_to_flow;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/** Extract destination Ip only from {@link PacketHeaderConstraints}. */
@ParametersAreNonnullByDefault
public final class DstIpExtractorPhcOnly implements FieldExtractor<Ip> {
  public DstIpExtractorPhcOnly(IpFieldExtractorContext helper) {
    _helper = helper;
  }

  @Override
  public Ip getValue(PacketHeaderConstraints constraints, Location srcLocation) {
    String headerDstIp = constraints.getDstIps();
    checkArgument(headerDstIp != null, "destination must be specified");
    return _helper.inferDstIpFromHeaderDstIp(headerDstIp);
  }

  private final @Nonnull IpFieldExtractorContext _helper;
}
