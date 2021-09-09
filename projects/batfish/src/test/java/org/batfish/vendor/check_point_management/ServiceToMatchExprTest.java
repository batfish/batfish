package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

/** Test of {@link ServiceToMatchExpr}. */
public final class ServiceToMatchExprTest {
  private final ServiceToMatchExpr _serviceToMatchExpr = new ServiceToMatchExpr(ImmutableMap.of());

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testCpmiAnyObject() {
    Service service = new CpmiAnyObject(Uid.of("1"));
    assertBddsEqual(service.accept(_serviceToMatchExpr), TrueExpr.INSTANCE);
  }

  @Test
  public void testIcmp() {
    Service service = new ServiceIcmp("icmp", 1, 2, Uid.of("1"));
    Service serviceNoCode = new ServiceIcmp("icmp", 1, null, Uid.of("1"));
    assertBddsEqual(
        service.accept(_serviceToMatchExpr),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpTypes(1)
                .setIcmpCodes(2)
                .build()));
    assertBddsEqual(
        serviceNoCode.accept(_serviceToMatchExpr),
        new MatchHeaderSpace(
            HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).setIcmpTypes(1).build()));
  }

  @Test
  public void testTcp() {
    Service service = new ServiceTcp("tcp", "100-105,300", Uid.of("1"));
    assertBddsEqual(
        service.accept(_serviceToMatchExpr),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(ImmutableList.of(new SubRange(100, 105), new SubRange(300)))
                .build()));
  }

  @Test
  public void testUdp() {
    Service service = new ServiceUdp("udp", "222", Uid.of("1"));
    assertBddsEqual(
        service.accept(_serviceToMatchExpr),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(new SubRange(222))
                .build()));
  }

  private void assertBddsEqual(AclLineMatchExpr left, AclLineMatchExpr right) {
    assertThat(_tb.toBDD(left), equalTo(_tb.toBDD(right)));
  }
}
