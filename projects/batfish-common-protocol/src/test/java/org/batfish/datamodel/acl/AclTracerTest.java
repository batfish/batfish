package org.batfish.datamodel.acl;

import static org.batfish.datamodel.matchers.DataModelMatchers.hasEvents;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDefaultDeniedByAclIpSpaceNamed;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDefaultDeniedByIpAccessListNamed;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDeniedByAclIpSpaceLineThat;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDeniedByIpAccessListLineThat;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDeniedByNamedIpSpaceThat;
import static org.batfish.datamodel.matchers.DataModelMatchers.isPermittedByAclIpSpaceLineThat;
import static org.batfish.datamodel.matchers.DataModelMatchers.isPermittedByIpAccessListLineThat;
import static org.batfish.datamodel.matchers.DataModelMatchers.isPermittedByNamedIpSpaceThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.matchers.DeniedByAclIpSpaceLineMatchers;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchers;
import org.batfish.datamodel.matchers.PermittedByAclIpSpaceLineMatchers;
import org.batfish.datamodel.matchers.PermittedByIpAccessListLineMatchers;
import org.junit.Test;

public class AclTracerTest {

  @Test
  public void testDefaultDeniedByIpAccessList() {
    String aclName = "acl1";
    IpAccessList acl = IpAccessList.builder().setName(aclName).build();
    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    /* The ACL has no lines, so the only event should be a default deny */
    assertThat(trace, hasEvents(contains(isDefaultDeniedByIpAccessListNamed(aclName))));
  }

  @Test
  public void testDefaultDeniedByNamedAclIpSpace() {
    String aclIpSpaceName = "aclIpSpace";
    AclIpSpace aclIpSpace = AclIpSpace.DENY_ALL;

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(new IpSpaceReference(aclIpSpaceName))
                            .build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of(aclIpSpaceName, aclIpSpace);
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isDefaultDeniedByAclIpSpaceNamed(aclIpSpaceName),
                    isDefaultDeniedByIpAccessListNamed(aclName)))));
  }

  @Test
  public void testDeniedByIndirectPermit() {
    String aclName = "acl";
    String aclIndirectName = "aclIndirect";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.rejecting()
                        .setMatchCondition(new PermittedByAcl(aclIndirectName))
                        .build()))
            .build();
    IpAccessList aclIndirect =
        IpAccessList.builder()
            .setName(aclIndirectName)
            .setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL))
            .build();
    Map<String, IpAccessList> availableAcls =
        ImmutableMap.of(aclName, acl, aclIndirectName, aclIndirect);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isPermittedByIpAccessListLineThat(
                        allOf(
                            PermittedByIpAccessListLineMatchers.hasName(aclIndirectName),
                            PermittedByIpAccessListLineMatchers.hasIndex(0))),
                    isDeniedByIpAccessListLineThat(
                        allOf(
                            DeniedByIpAccessListLineMatchers.hasName(aclName),
                            DeniedByIpAccessListLineMatchers.hasIndex(0)))))));
  }

  @Test
  public void testDeniedByIpAccessListLine() {
    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(ImmutableList.of(IpAccessListLine.REJECT_ALL))
            .build();
    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                isDeniedByIpAccessListLineThat(
                    allOf(
                        DeniedByIpAccessListLineMatchers.hasName(aclName),
                        DeniedByIpAccessListLineMatchers.hasIndex(0))))));
  }

  @Test
  public void testDeniedByNamedAclIpSpaceLine() {
    String aclIpSpaceName = "aclIpSpace";
    AclIpSpace aclIpSpace =
        AclIpSpace.builder().setLines(ImmutableList.of(AclIpSpaceLine.DENY_ALL)).build();

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(new IpSpaceReference(aclIpSpaceName))
                            .build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of(aclIpSpaceName, aclIpSpace);
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isDeniedByAclIpSpaceLineThat(
                        allOf(
                            DeniedByAclIpSpaceLineMatchers.hasName(aclIpSpaceName),
                            DeniedByAclIpSpaceLineMatchers.hasIndex(0))),
                    isDefaultDeniedByIpAccessListNamed(aclName)))));
  }

  @Test
  public void testDeniedByNamedSimpleIpSpace() {
    String ipSpaceName = "aclIpSpace";

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(new IpSpaceReference(ipSpaceName))
                            .build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of(ipSpaceName, Ip.MAX.toIpSpace());
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isDeniedByNamedIpSpaceThat(ipSpaceName),
                    isDefaultDeniedByIpAccessListNamed(aclName)))));
  }

  @Test
  public void testDeniedByUnnamedAclIpSpace() {
    AclIpSpace aclIpSpace = AclIpSpace.DENY_ALL;

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder().setDstIps(aclIpSpace).build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(trace, hasEvents(contains(isDefaultDeniedByIpAccessListNamed(aclName))));
  }

  @Test
  public void testDeniedByUnnamedSimpleIpSpace() {
    IpSpace ipSpace = EmptyIpSpace.INSTANCE;

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder().setDstIps(ipSpace).build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(trace, hasEvents(contains(isDefaultDeniedByIpAccessListNamed(aclName))));
  }

  @Test
  public void testPermittedByIpAccessListLine() {
    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL))
            .build();
    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                isPermittedByIpAccessListLineThat(
                    allOf(
                        PermittedByIpAccessListLineMatchers.hasName(aclName),
                        PermittedByIpAccessListLineMatchers.hasIndex(0))))));
  }

  @Test
  public void testPermittedByNamedAclIpSpaceLine() {
    String aclIpSpaceName = "aclIpSpace";
    AclIpSpace aclIpSpace = AclIpSpace.PERMIT_ALL;

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(new IpSpaceReference(aclIpSpaceName))
                            .build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of(aclIpSpaceName, aclIpSpace);
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isPermittedByAclIpSpaceLineThat(
                        allOf(
                            PermittedByAclIpSpaceLineMatchers.hasName(aclIpSpaceName),
                            PermittedByAclIpSpaceLineMatchers.hasIndex(0))),
                    isPermittedByIpAccessListLineThat(
                        allOf(
                            PermittedByIpAccessListLineMatchers.hasName(aclName),
                            PermittedByIpAccessListLineMatchers.hasIndex(0)))))));
  }

  @Test
  public void testPermittedByNamedSimpleIpSpace() {
    String ipSpaceName = "aclIpSpace";

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(new IpSpaceReference(ipSpaceName))
                            .build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of(ipSpaceName, Ip.ZERO.toIpSpace());
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isPermittedByNamedIpSpaceThat(ipSpaceName),
                    isPermittedByIpAccessListLineThat(
                        allOf(
                            PermittedByIpAccessListLineMatchers.hasName(aclName),
                            PermittedByIpAccessListLineMatchers.hasIndex(0)))))));
  }

  @Test
  public void testPermittedByUnnamedAclIpSpace() {
    AclIpSpace aclIpSpace = AclIpSpace.PERMIT_ALL;

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder().setDstIps(aclIpSpace).build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isPermittedByIpAccessListLineThat(
                        allOf(
                            PermittedByIpAccessListLineMatchers.hasName(aclName),
                            PermittedByIpAccessListLineMatchers.hasIndex(0)))))));
  }

  @Test
  public void testPermittedByUnnamedSimpleIpSpace() {
    IpSpace ipSpace = UniverseIpSpace.INSTANCE;

    String aclName = "acl1";
    IpAccessList acl =
        IpAccessList.builder()
            .setName(aclName)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder().setDstIps(ipSpace).build())))
            .build();

    Map<String, IpAccessList> availableAcls = ImmutableMap.of(aclName, acl);
    Map<String, IpSpace> namedIpSpaces = ImmutableMap.of();
    String srcInterface = null;
    Flow flow = Flow.builder().setTag("tag").setDstIp(Ip.ZERO).setIngressNode("node1").build();
    AclTrace trace = acl.trace(flow, srcInterface, availableAcls, namedIpSpaces);

    /* The ACL has no lines, so the only event should be a default deny */
    assertThat(
        trace,
        hasEvents(
            contains(
                ImmutableList.of(
                    isPermittedByIpAccessListLineThat(
                        allOf(
                            PermittedByIpAccessListLineMatchers.hasName(aclName),
                            PermittedByIpAccessListLineMatchers.hasIndex(0)))))));
  }
}
