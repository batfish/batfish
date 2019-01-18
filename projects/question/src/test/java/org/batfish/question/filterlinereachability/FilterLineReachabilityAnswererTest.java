package org.batfish.question.filterlinereachability;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.IpAccessListLine.rejectingHeaderSpace;
import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityAnswerer.findBlockingLinesForLine;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityAnswerer.getSpecifiedFilters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclSpecs;
import org.batfish.datamodel.answers.FilterLineReachabilityRows;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link FilterLineReachabilityAnswerer}. */
public class FilterLineReachabilityAnswererTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration _c1;
  private Configuration _c2;

  private IpAccessList.Builder _aclb;
  private IpAccessList.Builder _aclb2;

  private static final IpAccessListLine UNMATCHABLE =
      IpAccessListLine.accepting().setMatchCondition(FalseExpr.INSTANCE).build();

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c1 = cb.setHostname("c1").build();
    _c2 = cb.setHostname("c2").build();
    _aclb = nf.aclBuilder().setOwner(_c1);
    _aclb2 = nf.aclBuilder().setOwner(_c2);
    _c1.setIpSpaces(ImmutableSortedMap.of("ipSpace", Ip.parse("1.2.3.4").toIpSpace()));
    _c1.setInterfaces(
        ImmutableSortedMap.of(
            "iface",
            Interface.builder().setName("iface").build(),
            "iface2",
            Interface.builder().setName("iface2").build()));
    _c2.setInterfaces(ImmutableSortedMap.of("iface", Interface.builder().setName("iface").build()));
  }

  @Test
  public void testIdenticalAclsCombine() {
    // acl1 and acl2 are identical; should result in a single AclSpec
    _aclb
        .setLines(
            ImmutableList.of(
                rejectingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build()),
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build())))
        .build();
    _aclb2
        .setLines(
            ImmutableList.of(
                rejectingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build()),
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build())))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1", "c2"));

    assertThat(aclSpecs, hasSize(1));
  }

  @Test
  public void testAclReferenceInAndOrNotExprIsFound() {
    _aclb.setLines(ImmutableList.of()).setName("acl0").build();

    // acl1 references acl0 in permittedbyacl concealed within and expr; should have that dependency
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new AndMatchExpr(ImmutableList.of(new PermittedByAcl("acl0"))))
                    .build()))
        .setName("acl1")
        .build();

    // acl2 references acl0 in permittedbyacl concealed within or expr; should have that dependency
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new OrMatchExpr(ImmutableList.of(new PermittedByAcl("acl0"))))
                    .build()))
        .setName("acl2")
        .build();

    // acl3 references acl0 in permittedbyacl concealed within not expr; should have that dependency
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new NotMatchExpr(new PermittedByAcl("acl0")))
                    .build()))
        .setName("acl3")
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // Everything except acl0 should have a dependency on acl0
    List<Set<String>> dependencies =
        aclSpecs.stream()
            .filter(spec -> !spec.acl.getAclName().equals("acl0"))
            .map(spec -> spec.acl.getDependencies().keySet())
            .collect(Collectors.toList());
    assertThat(
        dependencies,
        equalTo(
            ImmutableList.of(
                ImmutableSet.of("acl0"), ImmutableSet.of("acl0"), ImmutableSet.of("acl0"))));
  }

  @Test
  public void testIgnoreGeneratedFilters() {
    // generate unreachable
    IpAccessList aclGenerated =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting().setMatchCondition(FalseExpr.INSTANCE).build()))
            .setName("~aclGenerated")
            .build();

    SpecifierContext ctxt =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of("c1", _c1)).build();

    // we should get an empty set when we are ignoring generated filters
    assertThat(
        getSpecifiedFilters(new FilterLineReachabilityQuestion(null, null, true), ctxt),
        equalTo(ImmutableMap.of("c1", ImmutableSet.of())));

    // we should get the one acl we put in otherwise
    assertThat(
        getSpecifiedFilters(new FilterLineReachabilityQuestion(null, null, false), ctxt),
        equalTo(ImmutableMap.of("c1", ImmutableSet.of(aclGenerated))));
  }

  @Test
  public void testIpSpaceReferenceInAndOrNotExprIsFound() {
    MatchHeaderSpace ipSpaceReference =
        new MatchHeaderSpace(
            HeaderSpace.builder().setSrcIps(new IpSpaceReference("ipSpace")).build());

    // acl1 has IpSpace reference concealed within and expr; should remove dependency
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new AndMatchExpr(ImmutableList.of(ipSpaceReference)))
                    .build()))
        .setName("acl1")
        .build();

    // acl2 has IpSpace reference concealed within or expr; should remove dependency
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new OrMatchExpr(ImmutableList.of(ipSpaceReference)))
                    .build()))
        .setName("acl2")
        .build();

    // acl3 has IpSpace reference concealed within not expr; should remove dependency
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new NotMatchExpr(ipSpaceReference))
                    .build()))
        .setName("acl3")
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    MatchHeaderSpace dereferencedIpSpace =
        new MatchHeaderSpace(
            HeaderSpace.builder().setSrcIps(Ip.parse("1.2.3.4").toIpSpace()).build());

    Set<AclLineMatchExpr> matchExprs =
        aclSpecs.stream()
            .map(spec -> spec.acl.getSanitizedAcl().getLines().get(0).getMatchCondition())
            .collect(Collectors.toSet());
    assertThat(
        matchExprs,
        equalTo(
            ImmutableSet.of(
                new AndMatchExpr(ImmutableList.of(dereferencedIpSpace)),
                new OrMatchExpr(ImmutableList.of(dereferencedIpSpace)),
                new NotMatchExpr(dereferencedIpSpace))));
  }

  @Test
  public void testInterfaceReferenceInAndOrNotExprIsFound() {
    _c1.setInterfaces(
        ImmutableSortedMap.of(
            "iface1",
            Interface.builder().setName("iface").build(),
            "iface2",
            Interface.builder().setName("iface2").build(),
            "iface3",
            Interface.builder().setName("iface2").build()));

    // acl references iface1 within and expr, iface2 within or expr, and iface3 within not expr; all
    // three should be included in referenced interfaces
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new AndMatchExpr(
                            ImmutableList.of(new MatchSrcInterface(ImmutableList.of("iface1")))))
                    .build(),
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new OrMatchExpr(
                            ImmutableList.of(new MatchSrcInterface(ImmutableList.of("iface2")))))
                    .build(),
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new NotMatchExpr(new MatchSrcInterface(ImmutableList.of("iface3"))))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    assertThat(
        aclSpecs.get(0).acl.getInterfaces(),
        equalTo(ImmutableSet.of("iface1", "iface2", "iface3")));
  }

  @Test
  public void testCircularReferences() {
    // acl0 permits anything acl1 permits
    // acl1 permits anything acl2 permits
    // acl2 permits anything acl0 permits
    // acl3 permits anything acl1 permits (not part of cycle)
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build()))
        .setName("acl0")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("acl2")).build()))
        .setName("acl1")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("acl0")).build()))
        .setName("acl2")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build()))
        .setName("acl3")
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    assertThat(aclSpecs, hasSize(4));
    for (AclSpecs spec : aclSpecs) {
      if (spec.acl.getAclName().equals("acl3")) {
        // acl3 should still have its original line
        assertThat(
            spec.acl.getSanitizedAcl().getLines(),
            equalTo(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("acl1"))
                        .build())));
      } else {
        // acl0, acl1, acl2 should all have only an unmatchable line
        assertThat(spec.acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
        assertThat(spec.acl.inCycle(0), equalTo(true));
      }
    }
  }

  @Test
  public void testUndefinedReference() {
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("???")).build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have one unmatchable line
    assertThat(aclSpecs, hasSize(1));
    CanonicalAcl acl = aclSpecs.get(0).acl;
    assertThat(acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
    assertThat(acl.hasUndefinedRef(0), equalTo(true));
  }

  @Test
  public void testWithIpSpaceReference() {
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.rejecting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpSpaceReference("ipSpace"))
                                .build()))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should directly reject 1.2.3.4
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(
        spec.acl.getSanitizedAcl().getLines(),
        equalTo(
            ImmutableList.of(
                rejectingHeaderSpace(
                    HeaderSpace.builder().setSrcIps(Ip.parse("1.2.3.4").toIpSpace()).build()))));
  }

  @Test
  public void testWithIpSpaceReferenceChain() {
    // Make sure it correctly dereferences a whole chain of IpSpaceReferences
    _c1.setIpSpaces(
        ImmutableSortedMap.of(
            "ipSpace1",
            new IpSpaceReference("ipSpace2"),
            "ipSpace2",
            new IpSpaceReference("ipSpace3"),
            "ipSpace3",
            Ip.parse("1.2.3.4").toIpSpace()));

    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.rejecting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpSpaceReference("ipSpace1"))
                                .build()))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should directly reject 1.2.3.4
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(
        spec.acl.getSanitizedAcl().getLines(),
        equalTo(
            ImmutableList.of(
                rejectingHeaderSpace(
                    HeaderSpace.builder().setSrcIps(Ip.parse("1.2.3.4").toIpSpace()).build()))));
  }

  @Test
  public void testWithCircularIpSpaceReferenceChain() {
    // Make sure it identifies an undefined reference for a circular chain of IpSpaceReferences
    _c1.setIpSpaces(
        ImmutableSortedMap.of(
            "ipSpace1",
            new IpSpaceReference("ipSpace2"),
            "ipSpace2",
            new IpSpaceReference("ipSpace3"),
            "ipSpace3",
            new IpSpaceReference("ipSpace1")));

    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpSpaceReference("ipSpace1"))
                                .build()))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have one unmatchable line
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(spec.acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
  }

  @Test
  public void testWithUndefinedIpSpaceReference() {
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder().setSrcIps(new IpSpaceReference("???")).build()))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have one unmatchable line
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(spec.acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
  }

  @Test
  public void testWithUndefinedIpSpaceReferenceChain() {
    // Make sure it correctly interprets a chain of IpSpaceReferences ending with an undefined ref
    _c1.setIpSpaces(
        ImmutableSortedMap.of(
            "ipSpace1",
            new IpSpaceReference("ipSpace2"),
            "ipSpace2",
            new IpSpaceReference("ipSpace3")));

    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpSpaceReference("ipSpace1"))
                                .build()))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have one unmatchable line
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(spec.acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
  }

  @Test
  public void testReferencedAclUsesSrcInterface() {
    // Create ACL that references an ACL that references an interface
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("referencedAcl"))
                    .build()))
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new MatchSrcInterface(ImmutableList.of("iface")))
                    .build()))
        .setName("referencedAcl")
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // There should be two AclSpecs. Both should be aware of the interface "iface"
    assertThat(aclSpecs, hasSize(2));
    for (AclSpecs spec : aclSpecs) {
      assertThat(
          spec.acl.getInterfaces(), equalTo(ImmutableSet.of("iface", "unreferencedInterface")));
    }
  }

  @Test
  public void testWithUndefinedSrcInterfaceReference() {
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new MatchSrcInterface(ImmutableList.of("???")))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have one unmatchable line
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(spec.acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
  }

  @Test
  public void testWithAclIpSpaceWithGoodRefs() {
    // ACL contains an AclIpSpace that references the same valid named IpSpace twice
    _aclb
        .setLines(
            ImmutableList.of(
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(
                            AclIpSpace.of(
                                AclIpSpaceLine.permit(new IpSpaceReference("ipSpace")),
                                AclIpSpaceLine.permit(new IpSpaceReference("ipSpace"))))
                        .build())))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have correctly dereferenced "ipSpace"
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(
        spec.acl.getSanitizedAcl().getLines(),
        equalTo(
            ImmutableList.of(
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(
                            AclIpSpace.of(
                                AclIpSpaceLine.permit(Ip.parse("1.2.3.4").toIpSpace()),
                                AclIpSpaceLine.permit(Ip.parse("1.2.3.4").toIpSpace())))
                        .build()))));
  }

  @Test
  public void testWithAclIpSpaceWithCircularRef() {
    // Named IP spaces includes AclIpSpace "aclIpSpace".
    // "aclIpSpace" contains an IpSpaceReference to itself. Rip
    _c1.setIpSpaces(
        ImmutableSortedMap.of(
            "aclIpSpace",
            AclIpSpace.of(AclIpSpaceLine.permit(new IpSpaceReference("aclIpSpace")))));
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpSpaceReference("aclIpSpace"))
                                .build()))
                    .build()))
        .build();

    List<AclSpecs> aclSpecs = getAclSpecs(ImmutableSet.of("c1"));

    // The sanitized version of the acl should have one unmatchable line
    assertThat(aclSpecs, hasSize(1));
    AclSpecs spec = aclSpecs.get(0);
    assertThat(spec.acl.getSanitizedAcl().getLines(), equalTo(ImmutableList.of(UNMATCHABLE)));
  }

  @Test
  public void testSmallBlockersIgnored() {
    BDDPacket p = new BDDPacket();
    // deny IP <ddos src> any
    BDD ddos1 = p.getSrcIp().value(Ip.parse("1.2.3.1").asLong());
    BDD ddos2 = p.getSrcIp().value(Ip.parse("1.2.3.2").asLong());
    BDD ddos3 = p.getSrcIp().value(Ip.parse("1.2.3.3").asLong());
    // permit tcp any any eq ssh
    BDD tcp = p.getIpProtocol().value(IpProtocol.TCP.number());
    BDD ssh = tcp.and(p.getDstPort().value(22));
    // permit tcp any DST_IP eq ssh
    BDD selectiveSSH = ssh.and(p.getDstIp().value(Ip.parse("2.3.4.5").asLong()));

    /*
     * [deny|permit] ip   1.2.3.1  any
     * [deny|permit] ip   1.2.3.2  any
     * [deny|permit] ip   1.2.3.3  any
     * permit        tcp  any      any      eq ssh
     * permit        tcp  any      2.3.4.5  eq ssh
     */
    // Make the BDDS
    List<BDD> bdds = ImmutableList.of(ddos1, ddos2, ddos3, ssh, selectiveSSH);

    // last line (#4) is really blocked by (#3). Also report #0 as first line with diff action.
    List<LineAction> actions = ImmutableList.of(DENY, DENY, DENY, PERMIT, PERMIT);
    assertThat(findBlockingLinesForLine(4, actions, bdds), contains(0, 3));

    // if there are no lines with different actions, only report #3.
    List<LineAction> sameActions = ImmutableList.of(PERMIT, PERMIT, PERMIT, PERMIT, PERMIT);
    assertThat(findBlockingLinesForLine(4, sameActions, bdds), contains(3));
  }

  @Test
  public void testPartialOverlaps() {
    BDDPacket p = new BDDPacket();
    BDD first32 = p.getDstIp().value(Ip.parse("1.2.3.4").asLong());
    BDD second32 = p.getDstIp().value(Ip.parse("1.2.3.5").asLong());
    BDD slash31 = first32.or(second32);

    /*
     * permit ip   any  1.2.3.4/32
     * deny   ip   any  1.2.3.5/32
     * permit ip   any  1.2.3.4/31
     */
    List<BDD> bdds = ImmutableList.of(first32, second32, slash31);
    List<LineAction> actions = ImmutableList.of(PERMIT, DENY, PERMIT);

    // last line (#2) is blocked by both first two lines.
    assertThat(findBlockingLinesForLine(2, actions, bdds), contains(0, 1));

    //  Action should not matter.
    List<LineAction> sameActions = ImmutableList.of(PERMIT, PERMIT, PERMIT);
    assertThat(findBlockingLinesForLine(2, sameActions, bdds), contains(0, 1));
  }

  @Test
  public void testPartialOverlapsDominateFull() {
    BDDPacket p = new BDDPacket();
    BDD first32 = p.getDstIp().value(Ip.parse("1.2.3.4").asLong());
    BDD second32 = p.getDstIp().value(Ip.parse("1.2.3.5").asLong());
    BDD slash31 = first32.or(second32);

    /*
     * permit ip   any  1.2.3.4/31
     * deny   ip   any  any
     * permit ip   any  1.2.3.5/32
     */
    List<BDD> bdds = ImmutableList.of(slash31, p.getFactory().one(), second32);
    List<LineAction> actions = ImmutableList.of(PERMIT, DENY, PERMIT);

    // last line (#2) is blocked only by #0. #1 is ignored since it terminates no flows.
    assertThat(findBlockingLinesForLine(2, actions, bdds), contains(0));
  }

  @Test
  public void testDifferentActionPreservation() {
    BDDPacket p = new BDDPacket();
    BDD slash32 = p.getDstIp().value(Ip.parse("1.2.3.4").asLong());
    BDD tcp = p.getIpProtocol().value(IpProtocol.TCP.number());
    BDD not80 = tcp.and(p.getDstPort().value(80).not());

    /*
     * [deny|permit]   tcp any 1.2.3.4/32 neq 80
     * [permit|deny]   ip  any any
     * permit          ip  any 1.2.3.4/32
     */
    List<BDD> bdds = ImmutableList.of(slash32.and(not80), p.getFactory().one(), slash32);
    List<LineAction> actions = ImmutableList.of(DENY, PERMIT, PERMIT);

    // last line (#2) is blocked entirely by #1. But #0 is included since it matches with different
    // action.
    assertThat(findBlockingLinesForLine(2, actions, bdds), contains(0, 1));

    // #0 is not included when all lines have same action.
    List<LineAction> sameActions = ImmutableList.of(PERMIT, PERMIT, PERMIT);
    assertThat(findBlockingLinesForLine(2, sameActions, bdds), contains(1));

    // #0 is not included despite different action when #1 already has different action.
    List<LineAction> actionsAndCover = ImmutableList.of(DENY, DENY, PERMIT);
    assertThat(findBlockingLinesForLine(2, actionsAndCover, bdds), contains(1));
  }

  // This is really a documentation test for a case where we might want #0 to be reported, but
  // it won't be.
  @Test
  public void testSameActionNotReported() {
    BDDPacket p = new BDDPacket();
    BDD tcp = p.getIpProtocol().value(IpProtocol.TCP.number());
    BDD tcpEstablished = p.getTcpAck().or(p.getTcpRst());
    BDD slash32 = p.getDstIp().value(Ip.parse("1.2.3.4").asLong());
    BDD port80 = p.getDstPort().value(80);

    /*
     * permit tcp any any established   ! means ACK or RST is true.
     * deny tcp any 1.2.3.4/32
     * permit tcp any 1.2.3.4/32 eq 80
     */
    List<BDD> bdds =
        ImmutableList.of(tcpEstablished, tcp.and(slash32), tcp.and(slash32).and(port80));

    // last line (#2) is blocked entirely by #1. Since #1 has a different action, and even though #0
    // matches many packets, we will not include #0 independent of action of #0.
    List<LineAction> actions = ImmutableList.of(PERMIT, DENY, PERMIT);
    assertThat(findBlockingLinesForLine(2, actions, bdds), contains(1));

    List<LineAction> sameActions = ImmutableList.of(DENY, DENY, PERMIT);
    assertThat(findBlockingLinesForLine(2, sameActions, bdds), contains(1));
  }

  private List<AclSpecs> getAclSpecs(Set<String> configNames) {
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of("c1", _c1, "c2", _c2);
    Map<String, Set<IpAccessList>> acls =
        CommonUtil.toImmutableMap(
            configs,
            Entry::getKey,
            entry ->
                configNames.contains(entry.getKey())
                    ? ImmutableSet.copyOf(entry.getValue().getIpAccessLists().values())
                    : ImmutableSet.of());
    return FilterLineReachabilityAnswerer.getAclSpecs(
        configs, acls, new FilterLineReachabilityRows());
  }
}
