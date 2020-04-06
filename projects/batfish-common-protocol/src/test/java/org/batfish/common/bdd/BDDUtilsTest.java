package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDOps.andNull;
import static org.batfish.common.bdd.BDDUtils.swap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.function.BiFunction;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class BDDUtilsTest {
  @Test
  public void testSwap() {
    BDDPacket pkt = new BDDPacket();
    BDDInteger dstIp = pkt.getDstIp();
    BDDInteger srcIp = pkt.getSrcIp();
    BDDInteger dstPort = pkt.getDstPort();

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Ip ip4 = Ip.parse("4.4.4.4");

    BiFunction<BDDInteger, BDDInteger, BDD> mkBdd =
        (v1, v2) ->
            (v1.value(ip1.asLong()).and(v2.value(ip2.asLong())).and(dstPort.value(0)))
                .or(v1.value(ip3.asLong()).and(v2.value(ip4.asLong())).and(dstIp.value(1)));

    BDD orig = mkBdd.apply(dstIp, srcIp);
    BDD swapped = mkBdd.apply(srcIp, dstIp);
    assertThat(swap(orig, dstIp, srcIp), equalTo(swapped));
  }

  @Test
  public void testSwapMultiVar() {
    BDDPacket pkt = new BDDPacket();
    BDDInteger dstIp = pkt.getDstIp();
    BDDInteger srcIp = pkt.getSrcIp();
    BDDInteger dstPort = pkt.getDstPort();
    BDDInteger srcPort = pkt.getSrcPort();

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");

    BDD orig =
        andNull(
            dstIp.value(ip1.asLong()),
            dstPort.value(5),
            srcIp.value(ip2.asLong()),
            srcPort.value(7));
    BDD swapped =
        andNull(
            srcIp.value(ip1.asLong()),
            srcPort.value(5),
            dstIp.value(ip2.asLong()),
            dstPort.value(7));
    assertThat(swap(orig, dstIp, srcIp, dstPort, srcPort), equalTo(swapped));
  }
}
