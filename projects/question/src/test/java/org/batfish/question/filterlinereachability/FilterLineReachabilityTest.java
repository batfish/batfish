package org.batfish.question.filterlinereachability;

import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.ExprAclLine.rejectingHeaderSpace;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COLUMN_METADATA;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_ADDITIONAL_INFO;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_BLOCKING_LINES;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_DIFF_ACTION;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_REASON;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_SOURCES;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_UNREACHABLE_LINE;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.COL_UNREACHABLE_LINE_ACTION;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.Reason.BLOCKING_LINES;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.Reason.CYCLICAL_REFERENCE;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.Reason.INDEPENDENTLY_UNMATCHABLE;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.Reason.UNDEFINED_REFERENCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link FilterLineReachabilityQuestion}. */
public class FilterLineReachabilityTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration _c1;
  private Configuration _c2;

  private IpAccessList.Builder _aclb;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c1 = cb.setHostname("c1").build();
    _c2 = cb.setHostname("c2").build();
    _aclb = nf.aclBuilder().setOwner(_c1);
    _c1.setIpSpaces(ImmutableSortedMap.of("ipSpace", Ip.parse("1.2.3.4").toIpSpace()));
    _c1.setInterfaces(
        ImmutableSortedMap.of(
            "iface",
            TestInterface.builder().setName("iface").build(),
            "iface2",
            TestInterface.builder().setName("iface2").build()));
    _c2.setInterfaces(
        ImmutableSortedMap.of("iface", TestInterface.builder().setName("iface").build()));
  }

  @Test
  public void testWithIcmpType() {
    // First line accepts IP 1.2.3.4
    // Second line accepts same but only ICMP of type 8
    List<AclLine> lines =
        ImmutableList.of(
            ExprAclLine.acceptingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Ip.parse("1.2.3.4").toIpSpace()).build()),
            ExprAclLine.acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(Ip.parse("1.2.3.4").toIpSpace())
                    .setIpProtocols(ImmutableSet.of(IpProtocol.ICMP))
                    .setIcmpTypes(ImmutableList.of(new SubRange(8)))
                    .build()));
    _aclb.setLines(lines).setName("acl").build();
    List<String> lineNames = lines.stream().map(Object::toString).collect(Collectors.toList());

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    // Construct the expected result. First line should block second.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": acl"))
                .put(COL_UNREACHABLE_LINE, lineNames.get(1))
                .put(COL_UNREACHABLE_LINE_ACTION, LineAction.PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0)))
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, BLOCKING_LINES)
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testIpWildcards() {
    // First line accepts src IPs 1.2.3.4/30
    // Second line accepts src IPs 1.2.3.4/32
    List<AclLine> lines =
        ImmutableList.of(
            ExprAclLine.acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(
                        IpWildcard.create(Prefix.create(Ip.parse("1.2.3.4"), 30)).toIpSpace())
                    .build()),
            ExprAclLine.acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(IpWildcard.create(Ip.parse("1.2.3.4").toPrefix()).toIpSpace())
                    .build()),
            ExprAclLine.acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(
                        IpWildcard.create(Prefix.create(Ip.parse("1.2.3.4"), 28)).toIpSpace())
                    .build()));
    _aclb.setLines(lines).setName("acl").build();
    List<String> lineNames = lines.stream().map(Object::toString).collect(Collectors.toList());

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    // Construct the expected result. First line should block second.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": acl"))
                .put(COL_UNREACHABLE_LINE, lineNames.get(1))
                .put(COL_UNREACHABLE_LINE_ACTION, LineAction.PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0)))
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, BLOCKING_LINES)
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testCycleAppearsOnce() {
    // acl1 permits anything acl2 permits... twice
    // acl2 permits anything acl1 permits... twice
    _aclb
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl2")).build(),
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl2")).build()))
        .setName("acl1")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build(),
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build()))
        .setName("acl2")
        .build();

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    // Construct the expected result. Should find only one cycle result.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": acl1, acl2"))
                .put(COL_UNREACHABLE_LINE, null)
                .put(COL_UNREACHABLE_LINE_ACTION, null)
                .put(COL_BLOCKING_LINES, null)
                .put(COL_DIFF_ACTION, null)
                .put(COL_REASON, CYCLICAL_REFERENCE)
                .put(COL_ADDITIONAL_INFO, "Cyclic references in node 'c1': acl1 -> acl2 -> acl1")
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testCircularReferences() {
    // acl0 permits anything acl1 permits
    // acl1 permits anything acl2 permits, plus 1 other line to avoid acl3's line being unmatchable
    // acl2 permits anything acl0 permits
    // acl3 permits anything acl1 permits (not part of cycle)
    _aclb
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build()))
        .setName("acl0")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl2")).build(),
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build())))
        .setName("acl1")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl0")).build()))
        .setName("acl2")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build()))
        .setName("acl3")
        .build();

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    // Construct the expected result. Should find a single cycle result.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": acl0, acl1, acl2"))
                .put(COL_UNREACHABLE_LINE, null)
                .put(COL_UNREACHABLE_LINE_ACTION, null)
                .put(COL_BLOCKING_LINES, null)
                .put(COL_DIFF_ACTION, null)
                .put(COL_REASON, CYCLICAL_REFERENCE)
                .put(
                    COL_ADDITIONAL_INFO,
                    "Cyclic references in node 'c1': acl0 -> acl1 -> acl2 -> acl0")
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testUndefinedReference() {

    ExprAclLine aclLine =
        ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("???")).build();
    _aclb.setLines(ImmutableList.of(aclLine)).setName("acl").build();

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    // Construct the expected result. Should find an undefined ACL result.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": acl"))
                .put(COL_UNREACHABLE_LINE, aclLine.toString())
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of())
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, UNDEFINED_REFERENCE)
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testIndirection() {
    /*
     Referenced ACL contains 1 line: Permit 1.0.0.0/24
     Main ACL contains 2 lines:
     0. Permit anything that referenced ACL permits
     1. Permit 1.0.0.0/24
    */
    List<AclLine> referencedAclLines =
        ImmutableList.of(
            acceptingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace()).build()));
    IpAccessList referencedAcl = _aclb.setLines(referencedAclLines).setName("acl1").build();

    List<AclLine> aclLines =
        ImmutableList.of(
            ExprAclLine.accepting()
                .setMatchCondition(new PermittedByAcl(referencedAcl.getName()))
                .build(),
            acceptingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace()).build()));
    IpAccessList acl = _aclb.setLines(aclLines).setName("acl2").build();
    List<String> lineNames = aclLines.stream().map(Object::toString).collect(Collectors.toList());

    /*
     Runs two questions:
     1. General ACL reachability (referenced ACL won't be encoded after first NoD step)
     2. Reachability specifically for main ACL (referenced ACL won't be encoded at all)
     Will test that both give the same result.
    */
    TableAnswerElement generalAnswer = answer(new FilterLineReachabilityQuestion());
    TableAnswerElement specificAnswer = answer(new FilterLineReachabilityQuestion(acl.getName()));

    // Construct the expected result. Should find line 1 to be blocked by line 0 in main ACL.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": " + acl.getName()))
                .put(COL_UNREACHABLE_LINE, lineNames.get(1))
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0)))
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, BLOCKING_LINES)
                .build());

    assertThat(generalAnswer.getRows().getData(), equalTo(expected));
    assertThat(specificAnswer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testMultipleCoveringLines() {
    List<AclLine> aclLines =
        ImmutableList.of(
            acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(IpWildcard.parse("1.0.0.0:0.0.0.0").toIpSpace())
                    .build()),
            acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(IpWildcard.parse("1.0.0.1:0.0.0.0").toIpSpace())
                    .build()),
            acceptingHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(IpWildcard.parse("1.0.0.0:0.0.0.1").toIpSpace())
                    .build()));
    IpAccessList acl = _aclb.setLines(aclLines).setName("acl").build();
    List<String> lineNames = aclLines.stream().map(Object::toString).collect(Collectors.toList());

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    /*
     Construct the expected result. Line 2 should be blocked by both previous lines.
    */
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": " + acl.getName()))
                .put(COL_UNREACHABLE_LINE, lineNames.get(2))
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0), lineNames.get(1)))
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, BLOCKING_LINES)
                .build());

    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testIndependentlyUnmatchableLines() {
    /*
    Construct ACL with lines:
    0. Reject 1.0.0.0/24 (unblocked)
    1. Accept 1.0.0.0/24 (blocked by line 0)
    2. Accept [empty set] (unmatchable)
    3. Accept 1.0.0.0/32 (blocked by line 0)
    4. Accept 1.2.3.4/32 (unblocked)
     */
    List<AclLine> aclLines =
        ImmutableList.of(
            rejectingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace()).build()),
            acceptingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace()).build()),
            ExprAclLine.accepting().setMatchCondition(FalseExpr.INSTANCE).build(),
            acceptingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Prefix.parse("1.0.0.0/32").toIpSpace()).build()),
            acceptingHeaderSpace(
                HeaderSpace.builder().setSrcIps(Prefix.parse("1.2.3.4/32").toIpSpace()).build()));
    IpAccessList acl = _aclb.setLines(aclLines).setName("acl").build();
    List<String> lineNames = aclLines.stream().map(Object::toString).collect(Collectors.toList());

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    // Construct the expected result
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": " + acl.getName()))
                .put(COL_UNREACHABLE_LINE, lineNames.get(1))
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0)))
                .put(COL_DIFF_ACTION, true)
                .put(COL_REASON, BLOCKING_LINES)
                .build(),
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": " + acl.getName()))
                .put(COL_UNREACHABLE_LINE, lineNames.get(2))
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of())
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, INDEPENDENTLY_UNMATCHABLE)
                .build(),
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": " + acl.getName()))
                .put(COL_UNREACHABLE_LINE, lineNames.get(3))
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0)))
                .put(COL_DIFF_ACTION, true)
                .put(COL_REASON, BLOCKING_LINES)
                .build());

    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testOriginalAclNotMutated() {
    // ACL that references an undefined ACL and an IpSpace; check line unchanged in original version
    IpAccessList acl =
        _aclb
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("???")).build(),
                    ExprAclLine.rejecting()
                        .setMatchCondition(
                            new MatchHeaderSpace(
                                HeaderSpace.builder()
                                    .setSrcIps(new IpSpaceReference("ipSpace"))
                                    .build()))
                        .build()))
            .setName("acl")
            .build();

    answer(new FilterLineReachabilityQuestion());

    // ACL's lines should be the same as before
    assertThat(
        acl.getLines(),
        equalTo(
            ImmutableList.of(
                ExprAclLine.accepting().setMatchCondition(new PermittedByAcl("???")).build(),
                ExprAclLine.rejecting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpSpaceReference("ipSpace"))
                                .build()))
                    .build())));

    // Config's ACL should be the same as the original version
    assertThat(_c1.getIpAccessLists().get(acl.getName()), equalTo(acl));
  }

  @Test
  public void testWithSrcInterfaceReference() {
    List<AclLine> aclLines =
        ImmutableList.of(
            ExprAclLine.accepting()
                .setMatchCondition(new MatchSrcInterface(ImmutableList.of("iface", "iface2")))
                .build(),
            ExprAclLine.accepting()
                .setMatchCondition(new MatchSrcInterface(ImmutableList.of("iface")))
                .build());
    IpAccessList acl = _aclb.setLines(aclLines).setName("acl").build();
    List<String> lineNames = aclLines.stream().map(Object::toString).collect(Collectors.toList());

    TableAnswerElement answer = answer(new FilterLineReachabilityQuestion());

    /* Construct the expected result. Line 1 should be blocked by line 0. */
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder(COLUMN_METADATA)
                .put(COL_SOURCES, ImmutableList.of(_c1.getHostname() + ": " + acl.getName()))
                .put(COL_UNREACHABLE_LINE, lineNames.get(1))
                .put(COL_UNREACHABLE_LINE_ACTION, PERMIT)
                .put(COL_BLOCKING_LINES, ImmutableList.of(lineNames.get(0)))
                .put(COL_DIFF_ACTION, false)
                .put(COL_REASON, BLOCKING_LINES)
                .build());

    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  private TableAnswerElement answer(FilterLineReachabilityQuestion q) {
    IBatfish batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return ImmutableSortedMap.of(_c1.getHostname(), _c1, _c2.getHostname(), _c2);
          }

          @Override
          public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
            return ImmutableMap.of();
          }
        };
    FilterLineReachabilityAnswerer answerer = new FilterLineReachabilityAnswerer(q, batfish);
    return answerer.answer(batfish.getSnapshot());
  }
}
