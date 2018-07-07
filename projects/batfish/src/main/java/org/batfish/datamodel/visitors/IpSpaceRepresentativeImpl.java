package org.batfish.datamodel.visitors;

import static org.batfish.symbolic.bdd.BDDPacket.factory;

import com.google.auto.service.AutoService;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.bdd.BDDInteger;
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
    synchronized (factory) {
      if (factory.varNum() < Prefix.MAX_PREFIX_LENGTH) {
        factory.setVarNum(Prefix.MAX_PREFIX_LENGTH);
      }

      BDDInteger ipAddrBdd = BDDInteger.makeFromIndex(factory, Prefix.MAX_PREFIX_LENGTH, 0, true);
      IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(factory, ipAddrBdd);
      BDD ipSpaceBDD = ipSpace.accept(ipSpaceToBDD);
      return ipSpaceToBDD.getBDDInteger().getValueSatisfying(ipSpaceBDD).map(Ip::new);
    }
  }
}
