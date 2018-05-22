package org.batfish.symbolic.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDNetFactory.BDDPacket;
import org.batfish.symbolic.bdd.BDDNetFactory.BDDRoute;
import org.batfish.symbolic.bdd.BDDNetFactory.SatAssigment;
import org.junit.Before;
import org.junit.Test;

public class BDDNetFactoryTest {

  private BDDFactory _factory;
  private BDDNetFactory _netFactory;

  @Before
  public void init() {
    BDDNetConfig config = new BDDNetConfig(false);
    List<String> routers = new ArrayList<>();
    routers.add("A");
    routers.add("B");
    List<CommunityVar> comms = new ArrayList<>();
    List<Integer> localPrefs = new ArrayList<>();
    localPrefs.add(99);
    _netFactory = new BDDNetFactory(routers, comms, localPrefs, config);
    _factory = _netFactory.getFactory();
  }

  @Test
  public void testRoute() {
    BDDRoute r = _netFactory.routeVariables();

    Ip dstIp = new Ip("1.2.3.0");
    Prefix p = new Prefix(dstIp, 24);
    BDD pfx = BDDUtils.prefixToBdd(_factory, r, p);
    BDD metric = r.getMetric().value(100);
    BDD ospfMetric = r.getOspfMetric().value(OspfType.E1);
    BDD localPref = r.getLocalPref().value(99);
    BDD adminDist = r.getAdminDist().value(20);
    BDD med = r.getMed().value(80);
    BDD proto = r.getProtocolHistory().value(Protocol.BGP);
    BDD dst = r.getDstRouter().value("A");
    BDD src = r.getSrcRouter().value("B");
    BDD all =
        pfx.and(metric)
            .and(ospfMetric)
            .and(localPref)
            .and(adminDist)
            .and(med)
            .and(proto)
            .and(dst)
            .and(src);

    SatAssigment assignment = _netFactory.satOne(all);
    assertThat(assignment.getDstIp(), equalTo(dstIp));
    assertThat(assignment.getPrefixLen(), equalTo(24));
    assertThat(assignment.getMetric(), equalTo(100));
    assertThat(assignment.getOspfMetric(), equalTo(OspfType.E1));
    assertThat(assignment.getLocalPref(), equalTo(99));
    assertThat(assignment.getAdminDist(), equalTo(20));
    assertThat(assignment.getMed(), equalTo(80));
    assertThat(assignment.getRoutingProtocol(), equalTo(RoutingProtocol.BGP));
    assertThat(assignment.getDstRouter(), equalTo("A"));
    assertThat(assignment.getSrcRouter(), equalTo("B"));
  }

  @Test
  public void testPacket() {
    BDDPacket pkt = _netFactory.packetVariables();

    Ip dstIp1 = new Ip("1.2.3.4");
    Ip srcIp1 = new Ip("5.6.7.8");
    BDD dip1 = BDDUtils.firstBitsEqual(_factory, pkt.getDstIp().getBitvec(), dstIp1, 32);
    BDD sip1 = BDDUtils.firstBitsEqual(_factory, pkt.getSrcIp().getBitvec(), srcIp1, 32);
    BDD dport1 = pkt.getDstPort().value(100);
    BDD sport1 = pkt.getSrcPort().value(200);
    BDD ipProto1 = pkt.getIpProtocol().value(IpProtocol.TCP.number());
    BDD icmpCode1 = pkt.getIcmpCode().value(11);
    BDD icmpType1 = pkt.getIcmpType().value(12);
    BDD tcpAck1 = pkt.getTcpAck();
    BDD tcpCwr1 = pkt.getTcpCwr().not();
    BDD tcpEce1 = pkt.getTcpEce();
    BDD tcpPsh1 = pkt.getTcpPsh().not();
    BDD tcpRst1 = pkt.getTcpRst();
    BDD tcpFin1 = pkt.getTcpFin().not();
    BDD tcpUrg1 = pkt.getTcpUrg();
    BDD tcpSyn1 = pkt.getTcpSyn().not();
    BDD all1 =
        dip1.and(sip1)
            .and(dport1)
            .and(sport1)
            .and(ipProto1)
            .and(icmpCode1)
            .and(icmpType1)
            .and(tcpAck1)
            .and(tcpCwr1)
            .and(tcpEce1)
            .and(tcpPsh1)
            .and(tcpRst1)
            .and(tcpFin1)
            .and(tcpUrg1)
            .and(tcpSyn1);

    Ip dstIp2 = new Ip("99.98.97.96");
    Ip srcIp2 = new Ip("81.82.83.84");
    BDD dip2 = BDDUtils.firstBitsEqual(_factory, pkt.getDstIp().getBitvec(), dstIp2, 32);
    BDD sip2 = BDDUtils.firstBitsEqual(_factory, pkt.getSrcIp().getBitvec(), srcIp2, 32);
    BDD dport2 = pkt.getDstPort().value(1);
    BDD sport2 = pkt.getSrcPort().value(2);
    BDD ipProto2 = pkt.getIpProtocol().value(IpProtocol.UDP.number());
    BDD icmpCode2 = pkt.getIcmpCode().value(3);
    BDD icmpType2 = pkt.getIcmpType().value(4);
    BDD tcpAck2 = pkt.getTcpAck().not();
    BDD tcpCwr2 = pkt.getTcpCwr();
    BDD tcpEce2 = pkt.getTcpEce().not();
    BDD tcpPsh2 = pkt.getTcpPsh();
    BDD tcpRst2 = pkt.getTcpRst().not();
    BDD tcpFin2 = pkt.getTcpFin();
    BDD tcpUrg2 = pkt.getTcpUrg().not();
    BDD tcpSyn2 = pkt.getTcpSyn();
    BDD all2 =
        dip2.and(sip2)
            .and(dport2)
            .and(sport2)
            .and(ipProto2)
            .and(icmpCode2)
            .and(icmpType2)
            .and(tcpAck2)
            .and(tcpCwr2)
            .and(tcpEce2)
            .and(tcpPsh2)
            .and(tcpRst2)
            .and(tcpFin2)
            .and(tcpUrg2)
            .and(tcpSyn2);

    BDD either = all1.or(all2);
    List<SatAssigment> assignments = _netFactory.allSat(either);
    SatAssigment a1 = assignments.get(0);
    SatAssigment a2 = assignments.get(1);

    // ensure the correct order
    if (a1.getIcmpCode() < a2.getIcmpCode()) {
      SatAssigment tmp = a1;
      a1 = a2;
      a2 = tmp;
    }

    assertThat(a1.getDstIp(), equalTo(dstIp1));
    assertThat(a1.getSrcIp(), equalTo(srcIp1));
    assertThat(a1.getDstPort(), equalTo(100));
    assertThat(a1.getSrcPort(), equalTo(200));
    assertThat(a1.getIcmpCode(), equalTo(11));
    assertThat(a1.getIcmpType(), equalTo(12));
    assertThat(a1.getIpProtocol(), equalTo(IpProtocol.TCP));
    assertThat(a1.getTcpFlags().getAck(), equalTo(true));
    assertThat(a1.getTcpFlags().getCwr(), equalTo(false));
    assertThat(a1.getTcpFlags().getEce(), equalTo(true));
    assertThat(a1.getTcpFlags().getPsh(), equalTo(false));
    assertThat(a1.getTcpFlags().getRst(), equalTo(true));
    assertThat(a1.getTcpFlags().getFin(), equalTo(false));
    assertThat(a1.getTcpFlags().getUrg(), equalTo(true));
    assertThat(a1.getTcpFlags().getSyn(), equalTo(false));

    assertThat(a2.getDstIp(), equalTo(dstIp2));
    assertThat(a2.getSrcIp(), equalTo(srcIp2));
    assertThat(a2.getDstPort(), equalTo(1));
    assertThat(a2.getSrcPort(), equalTo(2));
    assertThat(a2.getIcmpCode(), equalTo(3));
    assertThat(a2.getIcmpType(), equalTo(4));
    assertThat(a2.getIpProtocol(), equalTo(IpProtocol.UDP));
    assertThat(a2.getTcpFlags().getAck(), equalTo(false));
    assertThat(a2.getTcpFlags().getCwr(), equalTo(true));
    assertThat(a2.getTcpFlags().getEce(), equalTo(false));
    assertThat(a2.getTcpFlags().getPsh(), equalTo(true));
    assertThat(a2.getTcpFlags().getRst(), equalTo(false));
    assertThat(a2.getTcpFlags().getFin(), equalTo(true));
    assertThat(a2.getTcpFlags().getUrg(), equalTo(false));
    assertThat(a2.getTcpFlags().getSyn(), equalTo(true));
  }

}
