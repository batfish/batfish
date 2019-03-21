package org.batfish.dataplane.traceroute;

import static org.batfish.dataplane.traceroute.TracerouteUtils.getTcpFlagsForReverse;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TcpFlags;
import org.junit.Test;

/** Tests for {@link TracerouteUtils}. */
public final class TracerouteUtilsTest {
  @Test
  public void returnFlow() {
    Ip ip1 = Ip.parse("1.1.1.1");
    int port1 = 1111;
    Ip ip2 = Ip.parse("2.2.2.2");
    int port2 = 2222;
    String node1 = "node1";
    String vrf1 = "vrf1";
    String iface1 = "iface1";
    String tag = "TAG";
    Flow forwardFlow =
        Flow.builder()
            .setSrcIp(ip1)
            .setSrcPort(port1)
            .setDstIp(ip2)
            .setDstPort(port2)
            .setIngressNode(node1)
            .setIngressVrf(vrf1)
            .setIngressInterface(iface1)
            .setTag(tag)
            .build();
    String node2 = "node2";
    String vrf2 = "vrf2";
    String iface2 = "iface2";
    assertThat(
        TracerouteUtils.returnFlow(forwardFlow, node2, vrf2, null),
        equalTo(
            Flow.builder()
                .setSrcIp(ip2)
                .setSrcPort(port2)
                .setDstIp(ip1)
                .setDstPort(port1)
                .setIngressNode(node2)
                .setIngressVrf(vrf2)
                .setTag(tag)
                .build()));
    assertThat(
        TracerouteUtils.returnFlow(forwardFlow, node2, null, iface2),
        equalTo(
            Flow.builder()
                .setSrcIp(ip2)
                .setSrcPort(port2)
                .setDstIp(ip1)
                .setDstPort(port1)
                .setIngressNode(node2)
                .setIngressInterface(iface2)
                .setTag(tag)
                .build()));
  }

  @Test
  public void testGetTcpFlagsForReverseNoSynNoAck() {
    // !SYN, !ACK -> !SYN, !ACK
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().build());
    assertFalse(reverseTcpFlags.getSyn());
    assertFalse(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseOnlySyn() {
    // SYN, !ACK -> SYN, ACK
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().setSyn(true).build());
    assertTrue(reverseTcpFlags.getSyn());
    assertTrue(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseSynAck() {
    // SYN, ACK -> !SYN, ACK
    TcpFlags reverseTcpFlags =
        getTcpFlagsForReverse(TcpFlags.builder().setSyn(true).setAck(true).build());
    assertFalse(reverseTcpFlags.getSyn());
    assertTrue(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseOnlyAck() {
    // !SYN, ACK -> !SYN, ACK
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().setAck(true).build());
    assertFalse(reverseTcpFlags.getSyn());
    assertTrue(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseAllTrue() {
    TcpFlags reverseTcpFlags =
        getTcpFlagsForReverse(
            TcpFlags.builder()
                .setRst(true)
                .setFin(true)
                .setUrg(true)
                .setEce(true)
                .setPsh(true)
                .setCwr(true)
                .build());

    assertTrue(reverseTcpFlags.getRst());
    assertTrue(reverseTcpFlags.getFin());
    assertTrue(reverseTcpFlags.getUrg());
    assertTrue(reverseTcpFlags.getEce());
    assertTrue(reverseTcpFlags.getPsh());
    assertTrue(reverseTcpFlags.getCwr());
  }

  @Test
  public void testGetTcpFlagsForReverseAllFalse() {
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().build());

    assertFalse(reverseTcpFlags.getRst());
    assertFalse(reverseTcpFlags.getFin());
    assertFalse(reverseTcpFlags.getUrg());
    assertFalse(reverseTcpFlags.getEce());
    assertFalse(reverseTcpFlags.getPsh());
    assertFalse(reverseTcpFlags.getCwr());
  }
}
