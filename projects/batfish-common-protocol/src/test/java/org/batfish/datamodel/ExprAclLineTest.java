package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

/** Tests of {@link ExprAclLine}. */
public class ExprAclLineTest {
  @Test
  public void testExplicitActions() {
    Ip ip1234 = Ip.parse("1.2.3.4");
    Ip ip2345 = Ip.parse("2.3.4.5");
    ExprAclLine block1234 =
        ExprAclLine.rejectingHeaderSpace(
            HeaderSpace.builder().setSrcIps(ip1234.toIpSpace()).build());
    ExprAclLine allow2345 =
        ExprAclLine.acceptingHeaderSpace(
            HeaderSpace.builder().setSrcIps(ip2345.toIpSpace()).build());
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(block1234, allow2345))
            .build();

    IpAccessList testAcl =
        IpAccessList.builder()
            .setName("aclThenDeny")
            .setLines(
                ExprAclLine.takingExplicitActionsOf(acl.getName()).collect(Collectors.toList()))
            .build();
    Map<String, IpAccessList> acls =
        ImmutableMap.of(acl.getName(), acl, testAcl.getName(), testAcl);

    Flow.Builder fb =
        Flow.builder().setIpProtocol(IpProtocol.OSPF).setDstIp(Ip.ZERO).setIngressNode("node");

    {
      // The testACL should explicitly permit the flow on some line, since it was permitted by acl.
      FilterResult r2345 =
          testAcl.filter(fb.setSrcIp(ip2345).build(), "eth", acls, Collections.emptyMap());
      assertThat(r2345.getAction(), equalTo(LineAction.PERMIT));
      assertThat(r2345.getMatchLine(), notNullValue()); // did not fall off end
    }
    {
      // The testACL should explicitly reject the flow on some line, since it was rejected by acl.
      FilterResult r1234 =
          testAcl.filter(fb.setSrcIp(ip1234).build(), "eth", acls, Collections.emptyMap());
      assertThat(r1234.getAction(), equalTo(LineAction.DENY));
      assertThat(r1234.getMatchLine(), notNullValue()); // did not fall off end
    }
    {
      // The testACL should reject the flow by falling off the end, since it was not explicitly
      // handled by acl.
      Ip ip3456 = Ip.parse("3.4.5.6");
      FilterResult r3456 =
          testAcl.filter(fb.setSrcIp(ip3456).build(), "eth", acls, Collections.emptyMap());
      assertThat(r3456.getAction(), equalTo(LineAction.DENY));
      assertThat(r3456.getMatchLine(), nullValue()); // signifies fell off the end
    }
  }

  @Test
  public void testEquals() {
    ExprAclLine.Builder lineBuilder =
        ExprAclLine.builder()
            .setAction(LineAction.PERMIT)
            .setMatchCondition(FalseExpr.INSTANCE)
            .setName("name");
    new EqualsTester()
        .addEqualityGroup(
            lineBuilder.build(),
            lineBuilder.build(),
            new ExprAclLine(LineAction.PERMIT, FalseExpr.INSTANCE, "name"))
        .addEqualityGroup(lineBuilder.setAction(LineAction.DENY).build())
        .addEqualityGroup(lineBuilder.setMatchCondition(TrueExpr.INSTANCE).build())
        .addEqualityGroup(lineBuilder.setName("another name").build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ExprAclLine l = new ExprAclLine(LineAction.PERMIT, OriginatingFromDevice.INSTANCE, "name");
    assertThat(BatfishObjectMapper.clone(l, ExprAclLine.class), equalTo(l));
  }
}
