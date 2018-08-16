package org.batfish.symbolic.bdd;

import com.google.common.collect.Sets;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists;

/**
 * Adapts {@link BDDSourceManager} for differential analysis between two versions of an ACL, where
 * interfaces could have been added/removed, enabled/disabled, and references to interfaces could
 * also have been added/removed.
 */
public final class DifferentialBDDSourceManager {
  private final BDDSourceManager _bddSourceManager;

  private final BDD _baseSane;

  private final BDD _deltaSane;

  private DifferentialBDDSourceManager(
      BDDSourceManager bddSourceManager, BDD baseSane, BDD deltaSane) {
    _bddSourceManager = bddSourceManager;
    _baseSane = baseSane;
    _deltaSane = deltaSane;
  }

  public static DifferentialBDDSourceManager forAcls(
      BDDPacket bddPacket,
      Configuration baseConfig,
      IpAccessList baseAcl,
      Configuration deltaConfig,
      IpAccessList deltaAcl) {
    BDDPacket pkt = bddPacket;
    Set<String> interfacesActiveInBase = BDDSourceManager.activeInterfaces(baseConfig);
    Set<String> interfacesActiveInDelta = BDDSourceManager.activeInterfaces(deltaConfig);
    Set<String> interfacesActiveInEither =
        Sets.union(interfacesActiveInBase, interfacesActiveInDelta);

    Set<String> interfacesInactiveInBase = BDDSourceManager.inactiveInterfaces(baseConfig);
    Set<String> interfacesInactiveInDelta = BDDSourceManager.inactiveInterfaces(deltaConfig);
    Set<String> interfacesInactiveInBoth =
        Sets.intersection(interfacesInactiveInBase, interfacesInactiveInDelta);

    Set<String> sourcesReferencedInEither =
        Sets.union(
            SourcesReferencedByIpAccessLists.referencedSources(
                baseConfig.getIpAccessLists(), baseAcl),
            SourcesReferencedByIpAccessLists.referencedSources(
                deltaConfig.getIpAccessLists(), deltaAcl));

    BDDSourceManager mgr =
        BDDSourceManager.forSources(
            pkt, interfacesActiveInEither, interfacesInactiveInBoth, sourcesReferencedInEither);
    BDD baseSane =
        Sets.difference(interfacesActiveInEither, interfacesActiveInBase)
            .stream()
            .map(mgr::getSourceInterfaceBDD)
            .map(BDD::not)
            .reduce(mgr.isSane(), BDD::and);
    BDD deltaSane =
        Sets.difference(interfacesActiveInEither, interfacesActiveInDelta)
            .stream()
            .map(mgr::getSourceInterfaceBDD)
            .map(BDD::not)
            .reduce(mgr.isSane(), BDD::and);
    return new DifferentialBDDSourceManager(mgr, baseSane, deltaSane);
  }

  public BDD getBaseSane() {
    return _baseSane;
  }

  public BDDSourceManager getBddSourceManager() {
    return _bddSourceManager;
  }

  public BDD getDeltaSane() {
    return _deltaSane;
  }
}
