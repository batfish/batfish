package org.batfish.datamodel.phc_to_flow;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.Location;

/** Extract Ip procotol from {@link PacketHeaderConstraints}; if failed use a default value. */
@ParametersAreNonnullByDefault
public enum IpProtocolExtractorDefault implements FieldExtractor<IpProtocol> {
  /** default TCP */
  TCP(IpProtocol.TCP),
  /** default UDP */
  UDP(IpProtocol.UDP),
  /** default ICMP */
  ICMP(IpProtocol.ICMP);

  @Override
  public IpProtocol getValue(PacketHeaderConstraints phc, Location srcLoction) {
    Set<IpProtocol> ipProtocols =
        Optional.ofNullable(phc.resolveIpProtocols()).orElse(ImmutableSet.of(_defaultIpProtocol));

    return ipProtocols.iterator().next();
  }

  private IpProtocol _defaultIpProtocol;

  IpProtocolExtractorDefault(IpProtocol defaultIpProtocol) {
    _defaultIpProtocol = defaultIpProtocol;
  }
}
