package org.batfish.datamodel.applications;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

public class UdpApplicationTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            UdpApplication.ALL,
            // synonym of ALL
            new UdpApplication(ImmutableList.of(new SubRange(0, PortsApplication.MAX_PORT_NUMBER))))
        .addEqualityGroup(
            new UdpApplication(80), new UdpApplication(ImmutableList.of(SubRange.singleton(80))))
        .addEqualityGroup(new UdpApplication(ImmutableList.of(new SubRange(0, 80))))
        .addEqualityGroup(new UdpApplication(ImmutableList.of(new SubRange(80, 0))))
        .addEqualityGroup(
            new UdpApplication(ImmutableList.of(SubRange.singleton(0), SubRange.singleton(80))))
        // shouldn't equal TCP
        .addEqualityGroup(new TcpApplication(ImmutableList.of(new SubRange(0, 80))))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(UdpApplication.ALL.toString(), equalTo("udp"));
    assertThat(new UdpApplication(80).toString(), equalTo("udp/80"));
    assertThat(
        new UdpApplication(ImmutableList.of(new SubRange(0, 80))).toString(), equalTo("udp/0-80"));
    assertThat(
        new UdpApplication(ImmutableList.of(SubRange.singleton(443), new SubRange(0, 80)))
            .toString(),
        equalTo("udp/443,0-80"));
  }
}
