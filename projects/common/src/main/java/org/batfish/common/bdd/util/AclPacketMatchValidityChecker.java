package org.batfish.common.bdd.util;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;

/** Checks ACLs for validity with respect to which IPv4 variables they test. */
public final class AclPacketMatchValidityChecker {
  public static AclPacketMatchValidityChecker checkerFor(Configuration c) {
    BDDPacket pkt = new BDDPacket();
    IpAccessListToBdd toBdd =
        new IpAccessListToBddImpl(
            pkt,
            BDDSourceManager.forInterfaces(pkt, c.getAllInterfaces().keySet()),
            c.getIpAccessLists(),
            c.getIpSpaces());
    return new AclPacketMatchValidityChecker(pkt, toBdd);
  }

  /**
   * Checks that the given BDD obeys packet invariants about which fields can be tested
   * independently vs must be tested together (e.g., ICMP Code should only be tested if this is an
   * ICMP packet).
   */
  public boolean check(IpAccessList acl) {
    BDD aclBdd = _toBdd.toBdd(acl);
    return aclMeetsPacketMatchInvariants(aclBdd);
  }

  @VisibleForTesting
  boolean aclMeetsPacketMatchInvariants(BDD aclBdd) {
    // TCP Flags should only be set for TCP packets
    BDD notTcp = aclBdd.and(_notTcp);
    boolean tcpOk = !notTcp.testsVars(_pkt.getTcpFlagsVars());
    notTcp.free();
    if (!tcpOk) {
      return false;
    }

    // ICMP type/code should only be set for ICMP packets
    BDD notIcmp = _notIcmp.and(aclBdd);
    boolean icmpOk = !notIcmp.testsVars(_icmpVars);
    notIcmp.free();
    if (!icmpOk) {
      return false;
    }

    // Ports should only be set for protocols with ports
    BDD notPorts = aclBdd.and(_notPorts);
    boolean portsOk = !notPorts.testsVars(_portVars);
    notPorts.free();
    if (!portsOk) {
      return false;
    }

    return true;
  }

  @VisibleForTesting
  AclPacketMatchValidityChecker(@Nonnull BDDPacket pkt, @Nonnull IpAccessListToBdd toBdd) {
    _pkt = pkt;
    _toBdd = toBdd;
    _notTcp = _pkt.getIpProtocol().value(IpProtocol.TCP).notEq();
    _notIcmp = _pkt.getIpProtocol().value(IpProtocol.ICMP).notEq();
    BDD[] portProtocols =
        IpProtocol.IP_PROTOCOLS_WITH_PORTS.stream()
            .map(pkt.getIpProtocol()::value)
            .toArray(BDD[]::new);
    _notPorts = pkt.getFactory().orAllAndFree(portProtocols).notEq();
    _icmpVars =
        _pkt.getIcmpType()
            .getBDDInteger()
            .getVars()
            .and(_pkt.getIcmpCode().getBDDInteger().getVars());
    _portVars = _pkt.getDstPort().getVars().and(_pkt.getSrcPort().getVars());
  }

  private final @Nonnull BDDPacket _pkt;
  private final @Nonnull IpAccessListToBdd _toBdd;
  private final @Nonnull BDD _notTcp;
  private final @Nonnull BDD _notIcmp;
  private final @Nonnull BDD _notPorts;
  private final @Nonnull BDD _icmpVars;
  private final @Nonnull BDD _portVars;
}
