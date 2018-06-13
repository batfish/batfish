package org.batfish.datamodel.visitors;

import com.google.auto.service.AutoService;
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
@AutoService(IpSpaceRepresentative.class)
public final class IpSpaceRepresentativeImpl implements IpSpaceRepresentative {

  /** Returns some representative element of an {@link IpSpace ip space}, if any exists. */
  @Override
  public Optional<Ip> getRepresentative(IpSpace ipSpace) {
    BDDFactory factory = BDDUtils.bddFactory(Prefix.MAX_PREFIX_LENGTH);
    BDDInteger ipAddrBdd = BDDInteger.makeFromIndex(factory, Prefix.MAX_PREFIX_LENGTH, 0, false);

    BDD bdd = ipSpace.accept(new IpSpaceToBDD(factory, ipAddrBdd));

    if (bdd.isZero()) {
      // unsatisfiable
      return Optional.empty();
    }

    BDD satAssignment = bdd.fullSatOne();
    long ip = 0;
    for (int i = 0; i < 32; i++) {
      BDD bitBDD = ipAddrBdd.getBitvec()[31 - i];
      if (!satAssignment.and(bitBDD).isZero()) {
        ip += 1 << i;
      }
    }
    return Optional.of(new Ip(ip));
  }
}
