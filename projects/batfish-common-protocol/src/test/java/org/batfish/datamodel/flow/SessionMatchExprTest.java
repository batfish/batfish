package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

public class SessionMatchExprTest {
  @Test
  public void testEquals() {
    SessionMatchExpr expr =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null);
    new EqualsTester()
        .addEqualityGroup(
            expr,
            expr,
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.TCP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.2.2"), Ip.parse("2.2.2.2"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.2.2"), Ip.parse("2.2.2.2"), 4000, 5000),
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.2.2"), Ip.parse("2.2.2.2"), 4000, 5000))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.2.2"), Ip.parse("2.2.2.2"), 4040, 5050))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    SessionMatchExpr matcher =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null);
    SessionMatchExpr clone = BatfishObjectMapper.clone(matcher, SessionMatchExpr.class);
    assertThat(matcher, equalTo(clone));

    matcher =
        new SessionMatchExpr(IpProtocol.TCP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 5000, 8000);
    clone = BatfishObjectMapper.clone(matcher, SessionMatchExpr.class);
    assertThat(matcher, equalTo(clone));
  }
}
