package org.batfish.datamodel.applications;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

public class TcpApplicationTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            TcpApplication.ALL,
            // synonym of ALL
            new TcpApplication(ImmutableList.of(new SubRange(0, PortsApplication.MAX_PORT_NUMBER))))
        .addEqualityGroup(
            new TcpApplication(80), new TcpApplication(ImmutableList.of(SubRange.singleton(80))))
        .addEqualityGroup(new TcpApplication(ImmutableList.of(new SubRange(0, 80))))
        .addEqualityGroup(new TcpApplication(ImmutableList.of(new SubRange(80, 0))))
        .addEqualityGroup(
            new TcpApplication(ImmutableList.of(SubRange.singleton(0), SubRange.singleton(80))))
        // shouldn't equal UDP
        .addEqualityGroup(new UdpApplication(ImmutableList.of(new SubRange(0, 80))))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(TcpApplication.ALL.toString(), equalTo("tcp"));
    assertThat(new TcpApplication(80).toString(), equalTo("tcp/80"));
    assertThat(
        new TcpApplication(ImmutableList.of(new SubRange(0, 80))).toString(), equalTo("tcp/0-80"));
    assertThat(
        new TcpApplication(ImmutableList.of(SubRange.singleton(443), new SubRange(0, 80)))
            .toString(),
        equalTo("tcp/443,0-80"));
  }

  @Test
  public void toAclLineMatchExpr() {
    assertEquals(
        new TcpApplication(ImmutableList.of(new SubRange(10, 20), new SubRange(20, 30)))
            .toAclLineMatchExpr(),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(ImmutableList.of(new SubRange(10, 20), new SubRange(20, 30)))
                .build()));
  }
}
