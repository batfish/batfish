package org.batfish.common.bdd;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.ipspace.IpSpaceSpecializer;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.UniverseIpSpace;

/**
 * An {@link IpSpaceSpecializer} that uses a {@link BDD} to represent the headerspace to which we
 * want to specialize.
 */
public final class BDDIpSpaceSpecializer extends IpSpaceSpecializer {
  private final BDD _bdd;
  private final IpSpaceToBDD _ipSpaceToBDD;
  private final boolean _simplifyToUniverse;

  /**
   * @param headerSpaceBdd The header space to specialize to.
   * @param namedIpSpaces The named {@link IpSpace IpSpaces} currently in scope.
   * @param ipSpaceToBDD Converts {@link IpSpace IpSpaces} to {@link BDD BDDs}.
   */
  public BDDIpSpaceSpecializer(
      BDD headerSpaceBdd, Map<String, IpSpace> namedIpSpaces, IpSpaceToBDD ipSpaceToBDD) {
    this(headerSpaceBdd, namedIpSpaces, ipSpaceToBDD, true);
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

  /**
   * Create a {@link BDDIpSpaceSpecializer} that specializes {@link IpSpace IpSpaces} to the destIp
   * of a headerspace {@link BDD}.
   *
   * @param pkt The {@link BDDPacket} used to create the headerSpaceBdd.
   * @param headerSpaceBdd The headerspace {@link BDD}.
   * @param namedIpSpaces The named {@link IpSpace IpSpaces} currently in scope.
   * @param simplifyToUniverse Whether to simplify {@link IpSpace IpSpaces} that contain
   *     headerSpaceBdd to {@link UniverseIpSpace#INSTANCE}.
   */
  public static BDDIpSpaceSpecializer specializeByDstIp(
      BDDPacket pkt,
      BDD headerSpaceBdd,
      Map<String, IpSpace> namedIpSpaces,
      boolean simplifyToUniverse) {
    return new BDDIpSpaceSpecializer(
        headerSpaceBdd, namedIpSpaces, new IpSpaceToBDD(pkt.getDstIp()), simplifyToUniverse);
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
    BDD ipSpaceBDD = _ipSpaceToBDD.visit(ipSpace);

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
