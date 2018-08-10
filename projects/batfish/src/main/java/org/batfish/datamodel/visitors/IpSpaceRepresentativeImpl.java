package org.batfish.datamodel.visitors;

import com.google.auto.service.AutoService;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.symbolic.bdd.IpSpaceToBDD;

/**
 * Finds a representative {@link Ip} contained in an input {@link IpSpace}, if any exists. In
 * particular, it finds the numerically smallest representative.
 */
@AutoService(IpSpaceRepresentative.class)
public final class IpSpaceRepresentativeImpl implements IpSpaceRepresentative {
  /** Returns some representative element of an {@link IpSpace ip space}, if any exists. */
  @Override
  public Optional<Ip> getRepresentative(IpSpace ipSpace) {
    BDDPacket pkt = new BDDPacket();
    BDDInteger ipAddrBdd = pkt.getDstIp();
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(pkt.getFactory(), ipAddrBdd);
    BDD ipSpaceBDD = ipSpace.accept(ipSpaceToBDD);
    return ipSpaceToBDD.getBDDInteger().getValueSatisfying(ipSpaceBDD).map(Ip::new);
  }
}
