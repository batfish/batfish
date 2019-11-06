package org.batfish.datamodel;

import static org.batfish.datamodel.PacketHeaderConstraintsUtil.DEFAULT_PACKET_LENGTH;

import java.util.Map;
import java.util.Optional;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;

/** Class for picking a representative flow from a header space. */
public final class HeaderSpaceToFlow {

  public HeaderSpaceToFlow(Map<String, IpSpace> ipSpaces, FlowPreference preference) {
    _headerSpaceToBDD = new HeaderSpaceToBDD(BDD_PACKET, ipSpaces);
    _preference = preference;
  }

  /** Get a representative flow from a header space according to a flow preference. */
  public Optional<Flow.Builder> getRepresentativeFlow(HeaderSpace hs) {
    int packetLength =
        hs.getPacketLengths().stream()
            .filter(l -> !l.isEmpty())
            .findFirst()
            .map(SubRange::getStart)
            .orElse(DEFAULT_PACKET_LENGTH);

    return BDD_PACKET
        .getFlow(_headerSpaceToBDD.toBDD(hs), _preference)
        .map(flowBuilder -> flowBuilder.setPacketLength(packetLength));
  }

  private static final BDDPacket BDD_PACKET = new BDDPacket();
  private final HeaderSpaceToBDD _headerSpaceToBDD;
  private final FlowPreference _preference;
}
