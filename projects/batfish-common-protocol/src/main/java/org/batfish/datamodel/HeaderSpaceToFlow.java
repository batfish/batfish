package org.batfish.datamodel;

import java.util.Map;
import java.util.Optional;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;

/** Class for picking a representative flow from a header space. */
public final class HeaderSpaceToFlow {

  public HeaderSpaceToFlow(Map<String, IpSpace> ipSpaces) {
    _headerSpaceToBDD = new HeaderSpaceToBDD(BDD_PACKET, ipSpaces);
  }

  /** Get a representative flow from a header space according to a flow preference. */
  public Optional<Flow.Builder> getRepresentativeFlow(HeaderSpace hs, FlowPreference preference) {
    return BDD_PACKET.getFlow(_headerSpaceToBDD.toBDD(hs), preference);
  }

  private static final BDDPacket BDD_PACKET = new BDDPacket();
  private final HeaderSpaceToBDD _headerSpaceToBDD;
}
