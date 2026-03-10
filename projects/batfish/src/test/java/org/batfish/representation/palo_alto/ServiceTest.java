package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Tests for {@link Service} */
public class ServiceTest {
  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testToMatchExpr() {
    Warnings w = new Warnings();
    // Protocol, no ports
    {
      Service service = Service.builder("foo").setIpProtocol(IpProtocol.TCP).build();
      MatchHeaderSpace expr =
          AclLineMatchExprs.match(HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build());

      assertThat(_tb.toBDD(service.toMatchExpr(w)), equalTo(_tb.toBDD(expr)));
    }
    // Protocol w/ ports
    {
      Service service =
          Service.builder("foo")
              .setIpProtocol(IpProtocol.UDP)
              .addPorts(443, 445, 446, 447)
              .addSourcePort(1024)
              .build();
      MatchHeaderSpace expr =
          AclLineMatchExprs.match(
              HeaderSpace.builder()
                  .setIpProtocols(IpProtocol.UDP)
                  .setDstPorts(ImmutableList.of(new SubRange(443, 443), new SubRange(445, 447)))
                  .setSrcPorts(new SubRange(1024, 1024))
                  .build());

      assertThat(_tb.toBDD(service.toMatchExpr(w)), equalTo(_tb.toBDD(expr)));
    }
    // ICMP with type
    {
      Service service =
          Service.builder("foo").setIpProtocol(IpProtocol.ICMP).setIcmpType(8).build();
      MatchHeaderSpace expr =
          AclLineMatchExprs.match(
              HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).setIcmpTypes(8).build());

      assertThat(_tb.toBDD(service.toMatchExpr(w)), equalTo(_tb.toBDD(expr)));
    }
  }
}
