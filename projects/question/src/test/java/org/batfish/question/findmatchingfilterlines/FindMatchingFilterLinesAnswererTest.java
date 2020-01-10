package org.batfish.question.findmatchingfilterlines;

import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.ExprAclLine.rejectingHeaderSpace;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.COL_ACTION;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.COL_FILTER;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.COL_LINE;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.COL_LINE_INDEX;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.COL_NODE;
import static org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.getBehaviorToReport;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.ActionGetter.LineBehavior;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.Row;
import org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesAnswerer.LineBehaviorFinder;
import org.batfish.question.findmatchingfilterlines.FindMatchingFilterLinesQuestion.Action;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link FindMatchingFilterLinesAnswerer} */
public class FindMatchingFilterLinesAnswererTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static FindMatchingFilterLinesQuestion UNCONSTRAINED_QUESTION =
      new FindMatchingFilterLinesQuestion(null, null, null, null, null);

  @Test
  public void testThrowsIfNoFiltersMatch() {
    // Config with no ACLs; should see exception because no filters match question specifications
    Configuration c =
        new NetworkFactory()
            .configurationBuilder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    MockBatfish batfish = new MockBatfish(ImmutableSortedMap.of("c", c));
    FindMatchingFilterLinesAnswerer answerer =
        new FindMatchingFilterLinesAnswerer(UNCONSTRAINED_QUESTION, batfish);
    _thrown.expect(IllegalArgumentException.class);
    answerer.answer(batfish.getSnapshot());
  }

  @Test
  public void testGetActionsToReport() {
    // Set up ACL containing two lines, make sure header constraints filter to the right line

    // First line accepts TCP to 1.1.1.0/24
    Ip ip1 = Ip.parse("1.1.1.1");
    Prefix prefix1 = Prefix.create(ip1, 24);
    Set<IpProtocol> protocols1 = ImmutableSet.of(IpProtocol.TCP);
    HeaderSpace headerSpace1 =
        HeaderSpace.builder().setDstIps(prefix1.toIpSpace()).setIpProtocols(protocols1).build();
    // Second line accepts UDP to 2.2.2.0/24
    Ip ip2 = Ip.parse("2.2.2.2");
    Prefix prefix2 = Prefix.create(ip2, 24);
    Set<IpProtocol> protocols2 = ImmutableSet.of(IpProtocol.UDP);
    HeaderSpace headerSpace2 =
        HeaderSpace.builder().setDstIps(prefix2.toIpSpace()).setIpProtocols(protocols2).build();

    List<AclLine> aclLines =
        ImmutableList.of(
            acceptingHeaderSpace(headerSpace1), ExprAclLine.rejectingHeaderSpace(headerSpace2));

    BDDPacket pkt = new BDDPacket();
    IpAccessListToBdd bddConverter =
        new IpAccessListToBddImpl(
            pkt, BDDSourceManager.empty(pkt), ImmutableMap.of(), ImmutableMap.of());
    HeaderSpaceToBDD hsConverter = new HeaderSpaceToBDD(pkt, ImmutableMap.of());

    {
      // Constrain dstIp to a superset of first line's dstIps (that still doesn't intersect prefix2)
      HeaderSpace headerSpace =
          HeaderSpace.builder()
              .setDstIps(Prefix.create(ip1, prefix1.getPrefixLength() - 8).toIpSpace())
              .build();
      Map<Integer, LineBehavior> reportedActions =
          getBehaviorToReport(aclLines, hsConverter.toBDD(headerSpace), bddConverter, null);
      assertThat(reportedActions, equalTo(ImmutableMap.of(0, LineBehavior.PERMIT)));
    }
    {
      // Constrain dstIp to a subset of first line's dstIps
      HeaderSpace headerSpace =
          HeaderSpace.builder()
              .setDstIps(Prefix.create(ip1, prefix1.getPrefixLength() + 4).toIpSpace())
              .build();
      Map<Integer, LineBehavior> reportedActions =
          getBehaviorToReport(aclLines, hsConverter.toBDD(headerSpace), bddConverter, null);
      assertThat(reportedActions, equalTo(ImmutableMap.of(0, LineBehavior.PERMIT)));
    }
    {
      // Constrain protocol to TCP
      HeaderSpace headerSpace =
          HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.TCP)).build();
      Map<Integer, LineBehavior> reportedActions =
          getBehaviorToReport(aclLines, hsConverter.toBDD(headerSpace), bddConverter, null);
      assertThat(reportedActions, equalTo(ImmutableMap.of(0, LineBehavior.PERMIT)));
    }
    {
      // Constrain action to DENY
      HeaderSpace headerSpace = HeaderSpace.builder().build();
      Map<Integer, LineBehavior> reportedActions =
          getBehaviorToReport(aclLines, hsConverter.toBDD(headerSpace), bddConverter, Action.DENY);
      assertThat(reportedActions, equalTo(ImmutableMap.of(1, LineBehavior.DENY)));
    }
    {
      // Constrain dstIp to 3.3.3.0/24 (shouldn't match either line)
      HeaderSpace headerSpace =
          HeaderSpace.builder().setDstIps(Prefix.parse("3.3.3.0/24").toIpSpace()).build();
      Map<Integer, LineBehavior> reportedActions =
          getBehaviorToReport(aclLines, hsConverter.toBDD(headerSpace), bddConverter, null);
      assertThat(reportedActions, anEmptyMap());
    }
    {
      // Constrain srcIp to 3.3.3.0/24 (should match both lines)
      HeaderSpace headerSpace =
          HeaderSpace.builder().setSrcIps(Prefix.parse("3.3.3.0/24").toIpSpace()).build();
      Map<Integer, LineBehavior> reportedActions =
          getBehaviorToReport(aclLines, hsConverter.toBDD(headerSpace), bddConverter, null);
      assertThat(
          reportedActions, equalTo(ImmutableMap.of(0, LineBehavior.PERMIT, 1, LineBehavior.DENY)));
    }
  }

  @Test
  public void testRecordedActionFinder_AclAclLine() {
    HeaderSpace permitSpace =
        HeaderSpace.builder().setDstIps(Ip.parse("1.1.1.1").toIpSpace()).build();
    HeaderSpace denySpace =
        HeaderSpace.builder().setDstIps(Ip.parse("2.2.2.2").toIpSpace()).build();

    IpAccessList permitAcl =
        IpAccessList.builder()
            .setName("permitAcl")
            .setLines(acceptingHeaderSpace(permitSpace))
            .build();
    IpAccessList denyAcl =
        IpAccessList.builder().setName("denyAcl").setLines(rejectingHeaderSpace(denySpace)).build();
    IpAccessList mixedAcl =
        IpAccessList.builder()
            .setName("mixedAcl")
            .setLines(acceptingHeaderSpace(permitSpace), rejectingHeaderSpace(denySpace))
            .build();
    Map<String, IpAccessList> acls =
        ImmutableMap.of(
            permitAcl.getName(),
            permitAcl,
            denyAcl.getName(),
            denyAcl,
            mixedAcl.getName(),
            mixedAcl);

    BDDPacket pkt = new BDDPacket();
    IpAccessListToBdd bddConverter =
        new IpAccessListToBddImpl(pkt, BDDSourceManager.empty(pkt), acls, ImmutableMap.of());
    HeaderSpaceToBDD hsConverter = new HeaderSpaceToBDD(pkt, ImmutableMap.of());
    BDD permitSpaceBdd = hsConverter.toBDD(permitSpace);
    BDD denySpaceBdd = hsConverter.toBDD(denySpace);

    AclAclLine permitAclAclLine = new AclAclLine("line name", permitAcl.getName());
    AclAclLine denyAclAclLine = new AclAclLine("line name", denyAcl.getName());
    AclAclLine mixedAclAclLine = new AclAclLine("line name", mixedAcl.getName());

    {
      // No action specified: Reported action should match all possible actions of referenced ACL
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, null, pkt.getFactory().one());
      assertThat(behaviorFinder.visit(permitAclAclLine), equalTo(LineBehavior.PERMIT));
      assertThat(behaviorFinder.visit(denyAclAclLine), equalTo(LineBehavior.DENY));
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.VARIABLE));
    }
    {
      // Action PERMIT: Only lines that can PERMIT should be reported
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, Action.PERMIT, pkt.getFactory().one());
      assertThat(behaviorFinder.visit(permitAclAclLine), equalTo(LineBehavior.PERMIT));
      assertThat(behaviorFinder.visit(denyAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.VARIABLE));
    }
    {
      // Action DENY: Only lines that can DENY should be reported
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, Action.DENY, pkt.getFactory().one());
      assertThat(behaviorFinder.visit(permitAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(denyAclAclLine), equalTo(LineBehavior.DENY));
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.VARIABLE));
    }
    {
      // No action specified, only permitHeaderSpace counts
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, null, permitSpaceBdd);
      assertThat(behaviorFinder.visit(permitAclAclLine), equalTo(LineBehavior.PERMIT));
      assertThat(behaviorFinder.visit(denyAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.PERMIT));
    }
    {
      // Action PERMIT and only permitHeaderSpace counts
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, Action.PERMIT, permitSpaceBdd);
      assertThat(behaviorFinder.visit(permitAclAclLine), equalTo(LineBehavior.PERMIT));
      assertThat(behaviorFinder.visit(denyAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.PERMIT));
    }
    {
      // Action DENY, but only permitHeaderSpace counts
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, Action.DENY, permitSpaceBdd);
      assertThat(behaviorFinder.visit(permitAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(denyAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(mixedAclAclLine), nullValue());
    }
    {
      // No action specified, only denyHeaderSpace counts
      LineBehaviorFinder behaviorFinder = new LineBehaviorFinder(bddConverter, null, denySpaceBdd);
      assertThat(behaviorFinder.visit(permitAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(denyAclAclLine), equalTo(LineBehavior.DENY));
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.DENY));
    }
    {
      // Action PERMIT, but only denyHeaderSpace counts
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, Action.PERMIT, denySpaceBdd);
      assertThat(behaviorFinder.visit(permitAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(denyAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(mixedAclAclLine), nullValue());
    }
    {
      // Action DENY and only denyHeaderSpace counts
      LineBehaviorFinder behaviorFinder =
          new LineBehaviorFinder(bddConverter, Action.DENY, denySpaceBdd);
      assertThat(behaviorFinder.visit(permitAclAclLine), nullValue());
      assertThat(behaviorFinder.visit(denyAclAclLine), equalTo(LineBehavior.DENY));
      assertThat(behaviorFinder.visit(mixedAclAclLine), equalTo(LineBehavior.DENY));
    }
  }

  @Test
  public void testFullAnswer() {
    /*
    Configuration c1 has:
      - ACL acl1, with a permit-all line
      - ACL acl2, with a deny-all line
    Configuration c2 has:
      - ACL acl1, with a permit-all line
     */

    // Construct matchers for the rows corresponding to each ACL line
    Matcher<Row> c1Acl1Matcher =
        allOf(
            hasColumn(COL_NODE, "c1", Schema.STRING),
            hasColumn(COL_FILTER, "acl1", Schema.STRING),
            hasColumn(COL_LINE, ExprAclLine.ACCEPT_ALL.getName(), Schema.STRING),
            hasColumn(COL_LINE_INDEX, 0, Schema.INTEGER),
            hasColumn(COL_ACTION, LineAction.PERMIT.toString(), Schema.STRING));
    Matcher<Row> c1Acl2Matcher =
        allOf(
            hasColumn(COL_NODE, "c1", Schema.STRING),
            hasColumn(COL_FILTER, "acl2", Schema.STRING),
            hasColumn(COL_LINE, ExprAclLine.REJECT_ALL.getName(), Schema.STRING),
            hasColumn(COL_LINE_INDEX, 0, Schema.INTEGER),
            hasColumn(COL_ACTION, LineAction.DENY.toString(), Schema.STRING));
    Matcher<Row> c2Acl1Matcher =
        allOf(
            hasColumn(COL_NODE, "c2", Schema.STRING),
            hasColumn(COL_FILTER, "acl1", Schema.STRING),
            hasColumn(COL_LINE, ExprAclLine.ACCEPT_ALL.getName(), Schema.STRING),
            hasColumn(COL_LINE_INDEX, 0, Schema.INTEGER),
            hasColumn(COL_ACTION, LineAction.PERMIT.toString(), Schema.STRING));

    // Build configs
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    IpAccessList.Builder aclBuilder = nf.aclBuilder();
    Configuration c1 = cb.setHostname("c1").build();
    Configuration c2 = cb.setHostname("c2").build();

    // Add acl1 in both configs
    aclBuilder.setName("acl1").setLines(ExprAclLine.ACCEPT_ALL);
    aclBuilder.setOwner(c1).build();
    aclBuilder.setOwner(c2).build();

    // Add acl2 in c1
    aclBuilder.setName("acl2").setLines(ExprAclLine.REJECT_ALL);
    aclBuilder.setOwner(c1).build();

    // Create batfish with configs
    MockBatfish mockBatfish = new MockBatfish(ImmutableSortedMap.of("c1", c1, "c2", c2));

    {
      // Unfiltered getRows should have all three lines
      FindMatchingFilterLinesAnswerer answerer =
          new FindMatchingFilterLinesAnswerer(UNCONSTRAINED_QUESTION, mockBatfish);
      assertThat(
          answerer.answer(mockBatfish.getSnapshot()).getRows().getData(),
          containsInAnyOrder(c1Acl1Matcher, c1Acl2Matcher, c2Acl1Matcher));
    }
    {
      // Answerer with nodes "c1" should not give row for c2 acl1
      FindMatchingFilterLinesAnswerer answerer =
          new FindMatchingFilterLinesAnswerer(
              new FindMatchingFilterLinesQuestion("c1", null, null, null, null), mockBatfish);
      assertThat(
          answerer.answer(mockBatfish.getSnapshot()).getRows().getData(),
          containsInAnyOrder(c1Acl1Matcher, c1Acl2Matcher));
    }
    {
      // Answerer with filters "acl1" should not give row for c1 acl2
      FindMatchingFilterLinesAnswerer answerer =
          new FindMatchingFilterLinesAnswerer(
              new FindMatchingFilterLinesQuestion(null, "acl1", null, null, null), mockBatfish);
      assertThat(
          answerer.answer(mockBatfish.getSnapshot()).getRows().getData(),
          containsInAnyOrder(c1Acl1Matcher, c2Acl1Matcher));
    }
  }

  private static class MockBatfish extends IBatfishTestAdapter {
    private final SortedMap<String, Configuration> _configs;
    private final SpecifierContext _specifierContext;

    MockBatfish(SortedMap<String, Configuration> configs) {
      _configs = configs;
      _specifierContext = MockSpecifierContext.builder().setConfigs(configs).build();
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
      assertThat(snapshot, equalTo(getSnapshot()));
      return _configs;
    }

    @Override
    public SpecifierContext specifierContext(NetworkSnapshot snapshot) {
      assertThat(snapshot, equalTo(getSnapshot()));
      return _specifierContext;
    }
  }
}
