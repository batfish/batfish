package org.batfish.datamodel.phc_to_flow;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/**
 * Extract source Ip from {@link PacketHeaderConstraints} if possible; otherwise infer an Ip from
 * the source {@link Location}. Throws if those inference fails.
 */
@ParametersAreNonnullByDefault
public final class SrcIpExtractorLocation implements FieldExtractor<Ip> {
  public SrcIpExtractorLocation(IpFieldExtractorContext helper) {
    _helper = helper;
  }

  @Override
  public Ip getValue(PacketHeaderConstraints phc, Location srcLocation) {
    String headerSrcIp = phc.getSrcIps();

    if (headerSrcIp != null) {
      return _helper.inferSrcIpFromHeaderSrcIp(headerSrcIp);
    }

    checkArgument(srcLocation != null, "source location is missing");
    return _helper.inferSrcIpFromSourceLocation(srcLocation);
  }

  private @Nonnull IpFieldExtractorContext _helper;
}
