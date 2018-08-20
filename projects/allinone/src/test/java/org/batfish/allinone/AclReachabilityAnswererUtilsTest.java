package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.IpAccessListLine.rejectingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
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
import org.batfish.datamodel.IpSpaceReference;
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
import org.batfish.datamodel.answers.AclLines2Rows;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface.AclSpecs;
import org.batfish.question.aclreachability2.AclReachabilityAnswererUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AclReachabilityAnswererUtilsTest {

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
    _c1.setIpSpaces(ImmutableSortedMap.of("ipSpace", new Ip("1.2.3.4").toIpSpace()));
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
        aclSpecs
            .stream()
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
            HeaderSpace.builder().setSrcIps(new Ip("1.2.3.4").toIpSpace()).build());

    Set<AclLineMatchExpr> matchExprs =
        aclSpecs
            .stream()
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
                    HeaderSpace.builder().setSrcIps(new Ip("1.2.3.4").toIpSpace()).build()))));
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
            new Ip("1.2.3.4").toIpSpace()));

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
                    HeaderSpace.builder().setSrcIps(new Ip("1.2.3.4").toIpSpace()).build()))));
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
                            AclIpSpace.builder()
                                .setLines(
                                    ImmutableList.of(
                                        AclIpSpaceLine.permit(new IpSpaceReference("ipSpace")),
                                        AclIpSpaceLine.permit(new IpSpaceReference("ipSpace"))))
                                .build())
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
                            AclIpSpace.builder()
                                .setLines(
                                    ImmutableList.of(
                                        AclIpSpaceLine.permit(new Ip("1.2.3.4").toIpSpace()),
                                        AclIpSpaceLine.permit(new Ip("1.2.3.4").toIpSpace())))
                                .build())
                        .build()))));
  }

  @Test
  public void testWithAclIpSpaceWithCircularRef() {
    // Named IP spaces includes AclIpSpace "aclIpSpace".
    // "aclIpSpace" contains an IpSpaceReference to itself. Rip
    _c1.setIpSpaces(
        ImmutableSortedMap.of(
            "aclIpSpace",
            AclIpSpace.builder()
                .setLines(
                    ImmutableList.of(
                        AclIpSpaceLine.builder()
                            .setIpSpace(new IpSpaceReference("aclIpSpace"))
                            .build()))
                .build()));
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
    return AclReachabilityAnswererUtils.getAclSpecs(configs, acls, new AclLines2Rows());
  }
}
