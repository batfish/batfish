package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

/** Tests of {@link AclAclLine} */
public class AclAclLineTest {
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
            .setLines(ImmutableList.of(new AclAclLine("aclAclLine", acl.getName())))
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
  public void testDefaultTraceElement() {
    assertNull(new AclAclLine("n", "a").getTraceElement());
  }

  @Test
  public void testEquals() {
    TraceElement traceElement1 = TraceElement.builder().add("a").build();
    TraceElement traceElement2 = TraceElement.builder().add("b").build();

    VendorStructureId vsId1 = new VendorStructureId("a", "b", "c");
    VendorStructureId vsId2 = null;

    new EqualsTester()
        .addEqualityGroup(
            new AclAclLine("name1", "acl1", traceElement1, vsId1),
            new AclAclLine("name1", "acl1", traceElement1, vsId1))
        .addEqualityGroup(new AclAclLine("name2", "acl1", traceElement1, vsId1))
        .addEqualityGroup(new AclAclLine("name1", "acl2", traceElement1, vsId1))
        .addEqualityGroup(new AclAclLine("name1", "acl1", traceElement2, vsId1))
        .addEqualityGroup(new AclAclLine("name1", "acl1", traceElement1, vsId2))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    {
      AclAclLine aclAclLine = new AclAclLine("lineName", "aclName");
      AclAclLine clone = (AclAclLine) BatfishObjectMapper.clone(aclAclLine, AclLine.class);
      assertEquals(aclAclLine, clone);
    }

    {
      AclAclLine aclAclLine =
          new AclAclLine(
              "lineName",
              "aclName",
              TraceElement.builder().add("a").build(),
              new VendorStructureId("a", "b", "c"));
      AclAclLine clone = (AclAclLine) BatfishObjectMapper.clone(aclAclLine, AclLine.class);
      assertEquals(aclAclLine, clone);
    }
  }
}
