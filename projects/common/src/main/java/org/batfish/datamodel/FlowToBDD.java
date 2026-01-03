package org.batfish.datamodel;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;

/** Class for converting a {@link Flow} to a {@link BDD}. */
public final class FlowToBDD {
  /**
   * Converts the given {@link Flow} to a {@link BDD} of the packet headers. This function ignores
   * any ingress location configured in the flow.
   *
   * @see #flowToBdd(Flow, BDDPacket, BDDSourceManager)
   */
  public static BDD flowHeadersToBdd(Flow f, BDDPacket p) {
    BDDFactory factory = p.getFactory();
    BDD one = factory.one();
    BDDOps ops = new BDDOps(factory);
    BDD tcpFlags =
        f.getIpProtocol() == IpProtocol.TCP
            ? ops.and(
                f.getTcpFlags().getAck() ? p.getTcpAck() : p.getTcpAck().not(),
                f.getTcpFlags().getCwr() ? p.getTcpCwr() : p.getTcpCwr().not(),
                f.getTcpFlags().getEce() ? p.getTcpEce() : p.getTcpEce().not(),
                f.getTcpFlags().getFin() ? p.getTcpFin() : p.getTcpFin().not(),
                f.getTcpFlags().getPsh() ? p.getTcpPsh() : p.getTcpPsh().not(),
                f.getTcpFlags().getRst() ? p.getTcpRst() : p.getTcpRst().not(),
                f.getTcpFlags().getSyn() ? p.getTcpSyn() : p.getTcpSyn().not(),
                f.getTcpFlags().getUrg() ? p.getTcpUrg() : p.getTcpUrg().not())
            : one;
    return ops.and(
        p.getDscp().value(f.getDscp()),
        p.getDstIp().value(f.getDstIp().asLong()),
        f.getDstPort() == null ? one : p.getDstPort().value(f.getDstPort()),
        p.getEcn().value(f.getEcn()),
        p.getFragmentOffset().value(f.getFragmentOffset()),
        f.getIcmpCode() == null ? one : p.getIcmpCode().value(f.getIcmpCode()),
        f.getIcmpType() == null ? one : p.getIcmpType().value(f.getIcmpType()),
        p.getIpProtocol().value(f.getIpProtocol()),
        p.getPacketLength().value(f.getPacketLength()),
        p.getSrcIp().value(f.getSrcIp().asLong()),
        f.getSrcPort() == null ? one : p.getSrcPort().value(f.getSrcPort()),
        tcpFlags);
  }

  private FlowToBDD() {} // prevent instantiation of utility class
}
