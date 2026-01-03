package org.batfish.datamodel.visitors;

import java.util.Optional;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

/** Simple class for finding representative {@link Ip IP Addresses} in an {@link IpSpace}. */
public final class IpSpaceRepresentative {
  private final IpSpaceToBDD _ipSpaceToBDD;

  public IpSpaceRepresentative() {
    BDDPacket bddPacket = new BDDPacket();
    _ipSpaceToBDD = bddPacket.getDstIpSpaceToBDD();
  }

  /** Returns some representative element of an {@link IpSpace ip space}, if any exists. */
  public Optional<Ip> getRepresentative(IpSpace ipSpace) {
    return _ipSpaceToBDD
        .getBDDInteger()
        .getValueSatisfying(_ipSpaceToBDD.visit(ipSpace))
        .map(Ip::create);
  }
}
