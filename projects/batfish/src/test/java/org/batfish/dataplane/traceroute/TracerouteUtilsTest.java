package org.batfish.dataplane.traceroute;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
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
}
