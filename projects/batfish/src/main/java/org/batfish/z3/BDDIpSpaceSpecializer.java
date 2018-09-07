package org.batfish.z3;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.IpSpaceToBDD;

/**
 * An {@link IpSpaceSpecializer} that uses a {@link BDD} to represent the headerspace to which we
 * want to specialize.
 */
public final class BDDIpSpaceSpecializer extends IpSpaceSpecializer {
  private final BDD _bdd;
  private final IpSpaceToBDD _ipSpaceToBDD;
  private final boolean _simplifyToUniverse;

  public BDDIpSpaceSpecializer(IpSpace ipSpace, Map<String, IpSpace> namedIpSpaces) {
    super(namedIpSpaces);

    BDDFactory factory = BDDUtils.bddFactory(32);
    BDDInteger ipAddrBdd = BDDInteger.makeFromIndex(factory, 32, 0, true);
    _ipSpaceToBDD = new IpSpaceToBDD(factory, ipAddrBdd, namedIpSpaces);
    _bdd = ipSpace.accept(_ipSpaceToBDD);
    _simplifyToUniverse = true;
  }

  /**
   * If you provide the specialization IpSpace as a BDD, you must also provide the IpSpaceToBDD
   * object (which contains a BDDFactory, because we have to keep using the same BDDFactory that
   * created the input BDD.
   *
   * @param bdd The IpSpace to specialize to.
   * @param namedIpSpaces The named IpSpaces currently in scope.
   * @param ipSpaceToBDD Converts IpSpaces to BDDs
   */
  public BDDIpSpaceSpecializer(
      BDD bdd, Map<String, IpSpace> namedIpSpaces, IpSpaceToBDD ipSpaceToBDD) {
    this(bdd, namedIpSpaces, ipSpaceToBDD, true);
  }

  public BDDIpSpaceSpecializer(
      BDD bdd,
      Map<String, IpSpace> namedIpSpaces,
      IpSpaceToBDD ipSpaceToBDD,
      boolean simplifyToUniverse) {
    super(namedIpSpaces);
    _bdd = bdd;
    _ipSpaceToBDD = ipSpaceToBDD;
    _simplifyToUniverse = simplifyToUniverse;
  }

  @Override
  protected Optional<IpSpaceSpecializer> restrictSpecializerToBlacklist(Set<IpWildcard> blacklist) {
    BDD refinedBDD =
        blacklist.stream().map(_ipSpaceToBDD::toBDD).map(BDD::not).reduce(_bdd, BDD::and);
    return refinedBDD.isZero()
        ? Optional.empty()
        : Optional.of(new BDDIpSpaceSpecializer(refinedBDD, _namedIpSpaces, _ipSpaceToBDD));
  }

  @Override
  protected IpSpace specialize(Ip ip) {
    return emptyIfNoIntersection(ip.toIpSpace());
  }

  @Override
  protected IpSpace specialize(IpWildcard ipWildcard) {
    return emptyIfNoIntersection(ipWildcard.toIpSpace());
  }

  /**
   * This method does the interesting work. Replace the input ipSpace with an equivalent one for
   * _bdd. If ipSpace and _bdd are disjoint, return EmptyIpSpace. If ipSpace is a superset of _bdd,
   * return UniverseIpSpace. Otherwise, return ipSpace.
   */
  private IpSpace emptyIfNoIntersection(IpSpace ipSpace) {
    BDD ipSpaceBDD = ipSpace.accept(_ipSpaceToBDD);

    if (ipSpaceBDD.and(_bdd).isZero()) {
      // disjoint ip spaces
      return EmptyIpSpace.INSTANCE;
    }

    if (_simplifyToUniverse && ipSpaceBDD.not().and(_bdd).isZero()) {
      // _bdd's ip space is a subset of ipSpace.
      return UniverseIpSpace.INSTANCE;
    }

    return ipSpace;
  }
}
