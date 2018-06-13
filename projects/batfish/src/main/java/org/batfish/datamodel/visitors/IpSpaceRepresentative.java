package org.batfish.datamodel.visitors;

import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.IpSpaceToBDD;

/**
 * Finds a representative {@link Ip} contained in an input {@link IpSpace}, if any exists. Prefers
 * 0s to 1s in the representative, though this is a greedy choice, starting with the high-order
 * bits, so of course we may end up with more than the minimal number of 1s.
 */
public final class IpSpaceRepresentative {

  private final BDDFactory _factory;

  private final BDDInteger _ipAddrBdd;

  private IpSpaceRepresentative() {
    _factory = BDDUtils.bddFactory(Prefix.MAX_PREFIX_LENGTH);
    _ipAddrBdd = BDDInteger.makeFromIndex(_factory, Prefix.MAX_PREFIX_LENGTH, 0, false);
  }

  /** Returns some representative element of an {@link IpSpace ip space}, if any exists. */
  public static Optional<Ip> getRepresentative(IpSpace ipSpace) {
    IpSpaceRepresentative obj = new IpSpaceRepresentative();
    BDD bdd = ipSpace.accept(new IpSpaceToBDD(obj._factory, obj._ipAddrBdd));
    return obj.getIp(bdd);
  }

  private Optional<Ip> getIp(BDD bdd) {
    if (bdd.isZero()) {
      // unsatisfiable
      return Optional.empty();
    }

    BDD satAssignment = bdd.fullSatOne();
    long ip = 0;
    for (int i = 0; i < 32; i++) {
      BDD bitBDD = _ipAddrBdd.getBitvec()[31 - i];
      if (!satAssignment.and(bitBDD).isZero()) {
        ip += 1 << i;
      }
    }
    return Optional.of(new Ip(ip));
  }
}
