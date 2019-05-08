package org.batfish.datamodel.acl;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPrefix;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPrefix;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Test;

public class AclExplainerTest {

  private BDDSourceManager _mgr;

  private BDDPacket _pkt;

  @Before
  public void setup() {
    _pkt = new BDDPacket();
    _mgr = BDDSourceManager.empty(_pkt);
  }

  private AclLineMatchExpr explain(IpAccessList acl) {
    return explain(TRUE, acl);
  }

  private AclLineMatchExpr explain(AclLineMatchExpr invariantExpr, IpAccessList acl) {
    return AclExplainer.explain(
        _pkt, _mgr, invariantExpr, acl, ImmutableMap.of(), ImmutableMap.of());
  }

  private AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainWithProvenance(
      AclLineMatchExpr invariantExpr, IpAccessList acl) {
    return explainWithProvenance(invariantExpr, acl, ImmutableMap.of());
  }

  private AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainWithProvenance(
      AclLineMatchExpr invariantExpr, IpAccessList acl, Map<String, IpAccessList> namedAcls) {
    return AclExplainer.explainWithProvenance(
        _pkt, _mgr, invariantExpr, acl, namedAcls, ImmutableMap.of());
  }

  private AclLineMatchExpr explainDifferential(IpAccessList denyAcl, IpAccessList permitAcl) {
    return explainDifferential(TRUE, denyAcl, permitAcl);
  }

  private AclLineMatchExpr explainDifferential(
      AclLineMatchExpr invariantExpr, IpAccessList denyAcl, IpAccessList permitAcl) {
    return AclExplainer.explainDifferential(
        _pkt,
        _mgr,
        invariantExpr,
        denyAcl,
        ImmutableMap.of(),
        ImmutableMap.of(),
        permitAcl,
        ImmutableMap.of(),
        ImmutableMap.of());
  }

  private AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainDifferentialWithProvenance(
      AclLineMatchExpr invariantExpr, IpAccessList denyAcl, IpAccessList permitAcl) {
    return explainDifferentialWithProvenance(
        invariantExpr, denyAcl, ImmutableMap.of(), permitAcl, ImmutableMap.of());
  }

  private AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainDifferentialWithProvenance(
      AclLineMatchExpr invariantExpr,
      IpAccessList denyAcl,
      Map<String, IpAccessList> denyNamedAcls,
      IpAccessList permitAcl,
      Map<String, IpAccessList> permitNamedAcls) {
    return AclExplainer.explainDifferentialWithProvenance(
        _pkt,
        _mgr,
        invariantExpr,
        denyAcl,
        denyNamedAcls,
        ImmutableMap.of(),
        permitAcl,
        permitNamedAcls,
        ImmutableMap.of());
  }

  @Test
  public void testExplainSimpleWithProvenance() {
    MatchHeaderSpace matchDstIp1 = matchDstIp("1.1.1.1");
    AclLineMatchExpr matchDstIp2 = matchDstIp("2.2.2.2");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDstIp1), accepting(matchDstIp2)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainWithProvenance(TRUE, acl);

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(or(matchDstIp1, matchDstIp2)));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(2));

    assertThat(
        provenance, hasEntry(matchDstIp1, ImmutableSet.of(new IpAccessListLineIndex(acl, 0))));
    assertThat(
        provenance, hasEntry(matchDstIp2, ImmutableSet.of(new IpAccessListLineIndex(acl, 1))));
  }

  // make a map of namedAcls including the given ACL and a newly constructed one named newAclName
  // that refers to the original one
  private Map<String, IpAccessList> namedAclsWithReference(IpAccessList acl, String newAclName) {
    String oldAclName = acl.getName();
    return ImmutableMap.of(
        oldAclName,
        acl,
        newAclName,
        IpAccessList.builder()
            .setName(newAclName)
            .setLines(ImmutableList.of(accepting(permittedByAcl(oldAclName))))
            .build());
  }

  @Test
  public void testExplainSimpleWithProvenanceAndNamedAcls() {
    MatchHeaderSpace matchDstIp1 = matchDstIp("1.1.1.1");
    AclLineMatchExpr matchDstIp2 = matchDstIp("2.2.2.2");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDstIp1), accepting(matchDstIp2)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainWithProvenance(TRUE, acl, namedAclsWithReference(acl, "acl2"));

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(or(matchDstIp1, matchDstIp2)));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(2));

    assertThat(
        provenance, hasEntry(matchDstIp1, ImmutableSet.of(new IpAccessListLineIndex(acl, 0))));
    assertThat(
        provenance, hasEntry(matchDstIp2, ImmutableSet.of(new IpAccessListLineIndex(acl, 1))));
  }

  @Test
  public void testExplainInvariantWithProvenance() {
    MatchHeaderSpace matchDstIp1 = matchDstIp("1.1.1.1");
    AclLineMatchExpr matchDstIp2 = matchDstIp("2.2.2.2");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDstIp1), accepting(matchDstIp2)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainWithProvenance(matchDstPrefix("2.0.0.0/8"), acl);

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(matchDstIp2));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(1));

    assertThat(
        provenance, hasEntry(matchDstIp2, ImmutableSet.of(new IpAccessListLineIndex(acl, 1))));
  }

  @Test
  public void testExplainInvariantWithProvenance2() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.1.1.1");
    AclLineMatchExpr matchDstPre = matchDstPrefix("2.0.0.0/8");
    AclLineMatchExpr invariant = matchDstIp("2.2.2.2");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDstIp), accepting(matchDstPre)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainWithProvenance(invariant, acl);

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(invariant));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(1));

    assertThat(
        provenance,
        hasEntry(
            invariant,
            ImmutableSet.of(new IpAccessListLineIndex(AclExplainer.scopedAcl(invariant, acl), 0))));
  }

  @Test
  public void testExplainRedundantPermit() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDstIp), accepting(matchDstPrefix)))
            .build();
    AclLineMatchExpr explanation = explain(acl);
    assertThat(explanation, equalTo(matchDstPrefix));
  }

  @Test
  public void testExplainRedundantDeny() {
    MatchHeaderSpace matchSrcIp = matchSrcIp("1.2.3.4");
    AclLineMatchExpr matchSrcPrefix = matchSrcPrefix("1.2.3.0/24");
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                ImmutableList.of(
                    rejecting(matchSrcIp),
                    accepting(matchDstIp),
                    rejecting(matchSrcPrefix),
                    accepting(matchDstPrefix)))
            .build();
    assertThat(
        explain(acl),
        equalTo(or(and(matchDstIp, not(matchSrcIp)), and(matchDstPrefix, not(matchSrcPrefix)))));

    /*
     * Reverse the order of the rejecting lines. Now we get:
     * or(and(matchDstIp, not(matchSrcPrefix)), and(matchDstPrefix, not(matchSrcPrefix)))))
     * And then the first disjunct is removed because it's subsumed by the second.
     */
    acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                ImmutableList.of(
                    rejecting(matchSrcPrefix),
                    accepting(matchDstIp),
                    rejecting(matchSrcIp),
                    accepting(matchDstPrefix)))
            .build();
    assertThat(explain(acl), equalTo(or(and(matchDstPrefix, not(matchSrcPrefix)))));
  }

  @Test
  public void testExplainRejectLines() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(accepting(matchDstIp), accepting(matchDstPrefix)))
            .build();
    AclLineMatchExpr explanation = explain(acl);
    assertThat(explanation, equalTo(matchDstPrefix));
  }

  @Test
  public void testExplainDifferentialSimpleWithProvenance() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    IpAccessList denyAcl =
        IpAccessList.builder()
            .setName("deny")
            .setLines(ImmutableList.of(accepting(matchDstIp)))
            .build();
    IpAccessList permitAcl =
        IpAccessList.builder()
            .setName("permit")
            .setLines(ImmutableList.of(accepting(matchDstPrefix)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainDifferentialWithProvenance(TRUE, denyAcl, permitAcl);

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(and(matchDstPrefix, not(matchDstIp))));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(2));

    assertThat(
        provenance,
        hasEntry(matchDstPrefix, ImmutableSet.of(new IpAccessListLineIndex(permitAcl, 0))));
    assertThat(
        provenance, hasEntry(matchDstIp, ImmutableSet.of(new IpAccessListLineIndex(denyAcl, 0))));
  }

  @Test
  public void testExplainDifferentialSimpleWithProvenanceAndNamedAcls() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    IpAccessList denyAcl =
        IpAccessList.builder()
            .setName("deny")
            .setLines(ImmutableList.of(accepting(matchDstIp)))
            .build();
    IpAccessList permitAcl =
        IpAccessList.builder()
            .setName("permit")
            .setLines(ImmutableList.of(accepting(matchDstPrefix)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainDifferentialWithProvenance(
            TRUE,
            denyAcl,
            namedAclsWithReference(denyAcl, "deny2"),
            permitAcl,
            namedAclsWithReference(permitAcl, "permit2"));

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(and(matchDstPrefix, not(matchDstIp))));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(2));

    assertThat(
        provenance,
        hasEntry(matchDstPrefix, ImmutableSet.of(new IpAccessListLineIndex(permitAcl, 0))));
    assertThat(
        provenance, hasEntry(matchDstIp, ImmutableSet.of(new IpAccessListLineIndex(denyAcl, 0))));
  }

  @Test
  public void testExplainDifferentialMultipleProvenance() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    IpAccessList denyAcl =
        IpAccessList.builder()
            .setName("deny")
            .setLines(ImmutableList.of(accepting(matchDstIp)))
            .build();
    IpAccessList permitAcl =
        IpAccessList.builder()
            .setName("permit")
            .setLines(
                ImmutableList.of(rejecting(not(matchSrcIp("1.1.1.1"))), accepting(matchDstPrefix)))
            .build();

    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainDifferentialWithProvenance(TRUE, denyAcl, permitAcl);

    MatchHeaderSpace permitHeaderSpace =
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setDstIps(Prefix.parse("1.2.3.0/24").toIpSpace())
                .setSrcIps(Ip.parse("1.1.1.1").toIpSpace())
                .build());
    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(and(permitHeaderSpace, not(matchDstIp))));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(2));

    assertThat(
        provenance,
        hasEntry(
            permitHeaderSpace,
            ImmutableSet.of(
                new IpAccessListLineIndex(permitAcl, 0), new IpAccessListLineIndex(permitAcl, 1))));
    assertThat(
        provenance, hasEntry(matchDstIp, ImmutableSet.of(new IpAccessListLineIndex(denyAcl, 0))));
  }

  @Test
  public void testExplainDifferentialInvariantWithProvenance() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix = matchDstPrefix("1.2.3.0/24");
    AclLineMatchExpr invariantExpr = matchDstPrefix("1.2.3.0/27");
    IpAccessList denyAcl =
        IpAccessList.builder()
            .setName("deny")
            .setLines(ImmutableList.of(accepting(matchDstIp)))
            .build();
    IpAccessList permitAcl =
        IpAccessList.builder()
            .setName("permit")
            .setLines(ImmutableList.of(accepting(matchDstPrefix)))
            .build();
    AclLineMatchExprWithProvenance<IpAccessListLineIndex> explanationWithProvenance =
        explainDifferentialWithProvenance(invariantExpr, denyAcl, permitAcl);

    AclLineMatchExpr explanation = explanationWithProvenance.getMatchExpr();
    assertThat(explanation, equalTo(and(invariantExpr, not(matchDstIp))));

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> provenance =
        explanationWithProvenance.getProvenance();
    assertThat(provenance.entrySet(), hasSize(2));

    assertThat(
        provenance,
        hasEntry(
            invariantExpr,
            ImmutableSet.of(
                new IpAccessListLineIndex(
                    AclExplainer.scopedAcl(
                        invariantExpr,
                        DifferentialIpAccessList.create(
                                denyAcl,
                                ImmutableMap.of(),
                                ImmutableMap.of(),
                                permitAcl,
                                ImmutableMap.of(),
                                ImmutableMap.of())
                            .getAcl()),
                    0))));
    assertThat(
        provenance, hasEntry(matchDstIp, ImmutableSet.of(new IpAccessListLineIndex(denyAcl, 0))));
  }

  @Test
  public void testExplainDifferentialReject() {
    MatchHeaderSpace matchDstIp = matchDstIp("1.2.3.4");
    AclLineMatchExpr matchDstPrefix24 = matchDstPrefix("1.2.3.0/24");
    AclLineMatchExpr matchDstPrefix16 = matchDstPrefix("1.2.0.0/16");
    IpAccessList denyAcl =
        IpAccessList.builder()
            .setName("deny")
            .setLines(ImmutableList.of(rejecting(matchDstIp), accepting(matchDstPrefix24)))
            .build();
    IpAccessList permitAcl =
        IpAccessList.builder()
            .setName("permit")
            .setLines(ImmutableList.of(accepting(matchDstPrefix16)))
            .build();
    assertThat(
        explainDifferential(denyAcl, permitAcl),
        equalTo(or(matchDstIp, and(matchDstPrefix16, not(matchDstPrefix24)))));
    /*
     * Add an invariant that contradicts the second disjunct
     */
    assertThat(explainDifferential(matchDstPrefix24, denyAcl, permitAcl), equalTo(matchDstIp));

    /*
     * Switch the prefixes. Now the second explanation becomes
     *   and(matchDstPrefix24, not(matchDstPrefix16))
     * Which is False and gets removed.
     */
    denyAcl =
        IpAccessList.builder()
            .setName("deny")
            .setLines(ImmutableList.of(rejecting(matchDstIp), accepting(matchDstPrefix16)))
            .build();
    permitAcl =
        IpAccessList.builder()
            .setName("permit")
            .setLines(ImmutableList.of(accepting(matchDstPrefix24)))
            .build();
    assertThat(explainDifferential(denyAcl, permitAcl), equalTo(matchDstIp));
  }
}
