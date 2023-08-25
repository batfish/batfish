package org.batfish.minesweeper.question.semdiff;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff.SemDiffAnswerer;
import projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff.SemDiffQuestion;

public class SemDiffAnswererTest {
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  Batfish _batfish;

  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "RM1";
  private static final String AS_PATH_1 = "asPath1";
  private static final String AS_PATH_2 = "asPath2";
  private static final String PFX_LST_1 = "pfx1";
  private static final String PFX_LST_2 = "pfx2";
  private static final String COMM_LST_1 = "comm1";

  private RoutingPolicy.Builder _policyBuilderDelta;
  private RoutingPolicy.Builder _policyBuilderBase;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    Configuration baseConfig = cb.build();

    Configuration.Builder db =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    Configuration deltaConfig = db.build();

    // AsPathAccessList only properly initializes its state upon deserialization
    AsPathAccessList asPath1 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^40$"))));
    AsPathAccessList asPath2 =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_2, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^50$"))));

    AsPathAccessList asPath1Delta =
        SerializationUtils.clone(
            new AsPathAccessList(
                AS_PATH_1, ImmutableList.of(new AsPathAccessListLine(PERMIT, "^30$"))));
    baseConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1, AS_PATH_2, asPath2));
    deltaConfig.setAsPathAccessLists(ImmutableMap.of(AS_PATH_1, asPath1Delta, AS_PATH_2, asPath2));

    RouteFilterList f1 =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.1/32")))));
    RouteFilterList f2 =
        new RouteFilterList(
            PFX_LST_2,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/31")))));
    RouteFilterList f1Delta =
        new RouteFilterList(
            PFX_LST_1,
            ImmutableList.of(
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.0/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.1/32"))),
                new RouteFilterLine(PERMIT, PrefixRange.fromPrefix(Prefix.parse("10.0.0.2/32")))));

    baseConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1, PFX_LST_2, f2));
    deltaConfig.setRouteFilterLists(ImmutableMap.of(PFX_LST_1, f1Delta, PFX_LST_2, f2));

    baseConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^20:")));
    deltaConfig.setCommunityMatchExprs(
        ImmutableMap.of(
            COMM_LST_1, new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^10:")));

    Vrf baseVrf =
        nf.vrfBuilder().setOwner(baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    Vrf deltaVrf =
        nf.vrfBuilder().setOwner(deltaConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    BgpProcess baseBgp =
        nf.bgpProcessBuilder()
            .setRouterId(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setEbgpAdminCost(0)
            .setIbgpAdminCost(0)
            .setLocalAdminCost(0)
            .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setVrf(baseVrf)
            .build();

    BgpActivePeerConfig baseBgpPeer =
        BgpActivePeerConfig.builder()
            .setGroup("testGroup")
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder().setExportPolicy(POLICY_NAME).build())
            .build();
    baseBgp.setNeighbors(ImmutableSortedMap.of(Ip.FIRST_CLASS_A_PRIVATE_IP, baseBgpPeer));

    baseVrf.setBgpProcess(baseBgp);

    BgpProcess deltaBgp =
        nf.bgpProcessBuilder()
            .setRouterId(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setEbgpAdminCost(0)
            .setIbgpAdminCost(0)
            .setLocalAdminCost(0)
            .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setVrf(deltaVrf)
            .build();

    BgpActivePeerConfig deltaBgpPeer =
        BgpActivePeerConfig.builder()
            .setGroup("testGroup")
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder().setExportPolicy(POLICY_NAME).build())
            .build();
    deltaBgp.setNeighbors(ImmutableSortedMap.of(Ip.FIRST_CLASS_A_PRIVATE_IP, deltaBgpPeer));

    deltaVrf.setBgpProcess(deltaBgp);

    _policyBuilderBase = nf.routingPolicyBuilder().setOwner(baseConfig).setName(POLICY_NAME);
    _policyBuilderDelta = nf.routingPolicyBuilder().setOwner(deltaConfig).setName(POLICY_NAME);

    _batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(HOSTNAME, baseConfig),
            ImmutableSortedMap.of(HOSTNAME, deltaConfig),
            _tempFolder);
  }

  @Test
  public void testNoDifference() {
    _policyBuilderDelta.addStatement(new Statements.StaticStatement(Statements.ExitAccept)).build();
    _policyBuilderBase.addStatement(new Statements.StaticStatement(Statements.ExitAccept)).build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());

    assertEquals("", answer.getAnswer());
  }

  /** Tests route-map diff when the action differs. */
  @Test
  public void testActionDifference() {
    _policyBuilderDelta.addStatement(new Statements.StaticStatement(Statements.ExitAccept)).build();
    _policyBuilderBase.addStatement(new Statements.StaticStatement(Statements.ExitReject)).build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());

    assertEquals(
        "#### Found difference on device _hostname_:\n\n"
            + "| Example Input Route | Proposed Policy Action | Current Policy Action | Proposed"
            + " Policy Output | Current Policy Output | Proposed Policy Trace | Current Policy"
            + " Trace | # Similar Differences |\n"
            + "| ------------------- |---------------------- |---------------------"
            + " |---------------------- |--------------------- |---------------------"
            + " |-------------------- |--------------------- |\n"
            + "| _network_ = 10.0.0.0/8, _metric_ = 0, _asPath_ = \"\", _communities_ = [], _tag_ ="
            + " 0, _weight_ = 0 | **DENY** | **PERMIT** | -- | -- | [] | [] | 1 |\n"
            + "\n",
        answer.getAnswer());
  }

  private MatchPrefixSet matchPrefixSet(List<PrefixRange> prList) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new ExplicitPrefixSet(new PrefixSpace(prList)));
  }

  /** Tests route-map diff when the output routes differ. */
  @Test
  public void testRouteMapDiff() {
    _policyBuilderDelta
        .addStatement(
            new If(
                matchPrefixSet(
                    ImmutableList.of(
                        new PrefixRange(Prefix.parse("10.0.0.0/24"), new SubRange(32, 32)))),
                ImmutableList.of(
                    new SetLocalPreference(new LiteralLong(150)),
                    new Statements.StaticStatement(Statements.ExitAccept))))
        .build();
    _policyBuilderDelta.addStatement(new Statements.StaticStatement(Statements.ExitReject)).build();
    _policyBuilderBase.addStatement(new Statements.StaticStatement(Statements.ExitAccept)).build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());

    assertEquals(
        "#### Found difference on device _hostname_:\n\n"
            + "| Example Input Route | Proposed Policy Action | Current Policy Action | Proposed"
            + " Policy Output | Current Policy Output | Proposed Policy Trace | Current Policy"
            + " Trace | # Similar Differences |\n"
            + "| ------------------- |---------------------- |---------------------"
            + " |---------------------- |--------------------- |---------------------"
            + " |-------------------- |--------------------- |\n"
            + "| _network_ = 10.0.0.0/8, _metric_ = 0, _asPath_ = \"\", _communities_ = [], _tag_ ="
            + " 0, _weight_ = 0 | **PERMIT** | **DENY** | -- | -- | [] | [] | 1 |\n"
            + "| _network_ = 10.0.0.0/32, _metric_ = 0, _asPath_ = \"\", _communities_ = [], _tag_"
            + " = 0, _weight_ = 0 | PERMIT | PERMIT | _localPreference_ = 100 | _localPreference_ ="
            + " 150 | [] | [] | 1 |\n"
            + "\n",
        answer.getAnswer());
  }

  /** Tests route-map diff when an AS-Path list definition differs. */
  @Test
  public void testMatchNamedAsPathDiff() {
    _policyBuilderBase
        .addStatement(
            new If(
                new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();
    _policyBuilderDelta
        .addStatement(
            new If(
                new LegacyMatchAsPath(new NamedAsPathSet(AS_PATH_1)),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertEquals(
        "#### Found difference on device _hostname_:\n\n"
            + "| Example Input Route | Proposed Policy Action | Current Policy Action | Proposed"
            + " Policy Output | Current Policy Output | Proposed Policy Trace | Current Policy"
            + " Trace | # Similar Differences |\n"
            + "| ------------------- |---------------------- |---------------------"
            + " |---------------------- |--------------------- |---------------------"
            + " |-------------------- |--------------------- |\n"
            + "| _network_ = 10.0.0.0/8, _metric_ = 0, _asPath_ = 30, _communities_ = [], _tag_ ="
            + " 0, _weight_ = 0 | **DENY** | **PERMIT** | -- | -- | [] | [] | 1 |\n"
            + "| _network_ = 10.0.0.0/8, _metric_ = 0, _asPath_ = 40, _communities_ = [], _tag_ ="
            + " 0, _weight_ = 0 | **PERMIT** | **DENY** | -- | -- | [] | [] | 1 |\n"
            + "\n",
        answer.getAnswer());
  }

  /** Tests route-map diff when the policies are the same but a named prefix-list differs. */
  @Test
  public void testMatchNamedPrefix() {
    _policyBuilderBase
        .addStatement(
            new If(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_1)),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();
    _policyBuilderDelta
        .addStatement(
            new If(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_1)),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertEquals(
        "#### Found difference on device _hostname_:\n\n"
            + "| Example Input Route | Proposed Policy Action | Current Policy Action | Proposed"
            + " Policy Output | Current Policy Output | Proposed Policy Trace | Current Policy"
            + " Trace | # Similar Differences |\n"
            + "| ------------------- |---------------------- |---------------------"
            + " |---------------------- |--------------------- |---------------------"
            + " |-------------------- |--------------------- |\n"
            + "| _network_ = 10.0.0.2/32, _metric_ = 0, _asPath_ = \"\", _communities_ = [], _tag_"
            + " = 0, _weight_ = 0 | **DENY** | **PERMIT** | -- | -- | [] | [] | 1 |\n"
            + "\n",
        answer.getAnswer());
  }

  /** Tests route-map diffs when a named-prefix list is the same. Should be no difference. */
  @Test
  public void testMatchNamedPrefixSame() {
    _policyBuilderBase
        .addStatement(
            new If(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_2)),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();
    _policyBuilderDelta
        .addStatement(
            new If(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(PFX_LST_2)),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertEquals("", answer.getAnswer());
  }

  /** Tests route-maps when their community-lists differ. */
  @Test
  public void testMatchCommunityReference() {
    _policyBuilderBase
        .addStatement(
            new If(
                new MatchCommunities(
                    InputCommunities.instance(),
                    new HasCommunity(new CommunityMatchExprReference(COMM_LST_1))),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();
    _policyBuilderDelta
        .addStatement(
            new If(
                new MatchCommunities(
                    InputCommunities.instance(),
                    new HasCommunity(new CommunityMatchExprReference(COMM_LST_1))),
                ImmutableList.of(new Statements.StaticStatement(Statements.ExitAccept))))
        .build();

    SemDiffQuestion question = new SemDiffQuestion();
    SemDiffAnswerer answerer = new SemDiffAnswerer(question, _batfish);

    StringAnswerElement answer =
        (StringAnswerElement)
            answerer.answerDiff(_batfish.getSnapshot(), _batfish.getReferenceSnapshot());
    assertEquals(
        "#### Found difference on device _hostname_:\n\n"
            + "| Example Input Route | Proposed Policy Action | Current Policy Action | Proposed"
            + " Policy Output | Current Policy Output | Proposed Policy Trace | Current Policy"
            + " Trace | # Similar Differences |\n"
            + "| ------------------- |---------------------- |---------------------"
            + " |---------------------- |--------------------- |---------------------"
            + " |-------------------- |--------------------- |\n"
            + "| _network_ = 10.0.0.0/8, _metric_ = 0, _asPath_ = \"\", _communities_ = [10:0],"
            + " _tag_ = 0, _weight_ = 0 | **DENY** | **PERMIT** | -- | -- | [] | [] | 1 |\n"
            + "| _network_ = 10.0.0.0/8, _metric_ = 0, _asPath_ = \"\", _communities_ = [20:0],"
            + " _tag_ = 0, _weight_ = 0 | **PERMIT** | **DENY** | -- | -- | [] | [] | 1 |\n"
            + "\n",
        answer.getAnswer());
  }
}
