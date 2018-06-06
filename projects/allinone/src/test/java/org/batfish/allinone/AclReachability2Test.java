package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.IpAccessListLine.rejectingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclLines2Rows;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.aclreachability2.AclReachability2Answerer;
import org.batfish.question.aclreachability2.AclReachability2Question;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AclReachability2Test {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration _c1;
  private Configuration _c2;

  private IpAccessList.Builder _aclb;
  private IpAccessList.Builder _aclb2;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c1 = cb.setHostname("c1").build();
    _c2 = cb.setHostname("c2").build();
    _aclb = nf.aclBuilder().setOwner(_c1);
    _aclb2 = nf.aclBuilder().setOwner(_c2);
  }

  @Test
  public void testIdenticalAclsCombine() throws IOException {
    // acl1 and acl2 are identical; blocked line should only appear as one result
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
        .setName("acl1")
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
        .setName("acl2")
        .build();

    TableAnswerElement answer = answer(new AclReachability2Question());

    // Construct the expected result. Should find only one result with both ACLs.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": acl1", _c2.getName() + ": acl2"))
                .put(AclLines2Rows.COL_LINES, new String[2])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 1)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2Rows.COL_DIFF_ACTION, true)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl1; c2: acl2 } contain(s) an unreachable line: '1: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}'."
                        + " Blocking line(s):\n  [index 0] IpAccessListLine{action=REJECT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}")
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void test2CircularReferences() throws IOException {
    // acl1 permits anything acl2 permits
    // acl2 permits anything acl1 permits
    IpAccessList acl1 =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("acl2"))
                        .build()))
            .setName("acl1")
            .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("acl1")).build()))
        .setName("acl2")
        .build();

    TableAnswerElement answer = answer(new AclReachability2Question());

    // Construct the expected result. Should find a cycle result.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(AclLines2Rows.COL_SOURCES, ImmutableList.of(_c1.getName() + ": acl1, acl2"))
                .put(AclLines2Rows.COL_LINES, null)
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, null)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, null)
                .put(AclLines2Rows.COL_DIFF_ACTION, null)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "Cyclic ACL references in node 'c1': acl1 -> acl2 -> acl1")
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void test3CircularReferences() throws IOException {
    // acl0 permits anything acl1 permits
    // acl1 permits anything acl2 permits, plus 1 other line to avoid acl3's line being unmatchable
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
                IpAccessListLine.accepting().setMatchCondition(new PermittedByAcl("acl2")).build(),
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build())))
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

    TableAnswerElement answer = answer(new AclReachability2Question());

    // Construct the expected result. Should find a single cycle result.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": acl0, acl1, acl2"))
                .put(AclLines2Rows.COL_LINES, null)
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, null)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, null)
                .put(AclLines2Rows.COL_DIFF_ACTION, null)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "Cyclic ACL references in node 'c1': acl0 -> acl1 -> acl2 -> acl0")
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testUndefinedReference() throws IOException {
    IpAccessList acl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("pretend acl"))
                        .build()))
            .setName("acl")
            .build();

    TableAnswerElement answer = answer(new AclReachability2Question());

    // Construct the expected result. Should find an undefined ACL result.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(AclLines2Rows.COL_SOURCES, ImmutableList.of(_c1.getName() + ": acl"))
                .put(AclLines2Rows.COL_LINES, new String[1])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 0)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of())
                .put(AclLines2Rows.COL_DIFF_ACTION, false)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl } contain(s) an unreachable line: '0: "
                        + "IpAccessListLine{action=ACCEPT, matchCondition=FalseExpr{}}'. This line "
                        + "references a structure that is not defined.")
                .build());
    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testIndirection() throws IOException {
    /*
     Referenced ACL contains 1 line: Permit 1.0.0.0/24
     Main ACL contains 2 lines:
     0. Permit anything that referenced ACL permits
     1. Permit 1.0.0.0/24
    */
    IpAccessList referencedAcl =
        _aclb
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .setName("acl1")
            .build();
    IpAccessList acl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl(referencedAcl.getName()))
                        .build(),
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .setName("acl2")
            .build();

    /*
     Runs two questions:
     1. General ACL reachability (referenced ACL won't be encoded after first NoD step)
     2. Reachability specifically for main ACL (referenced ACL won't be encoded at all)
     Will test that both give the same result.
    */
    TableAnswerElement generalAnswer = answer(new AclReachability2Question());
    TableAnswerElement specificAnswer = answer(new AclReachability2Question(acl.getName(), null));

    // Construct the expected result. Should find line 1 to be blocked by line 0 in main ACL.
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": " + acl.getName()))
                .put(AclLines2Rows.COL_LINES, new String[2])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 1)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2Rows.COL_DIFF_ACTION, false)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl2 } contain(s) an unreachable line: '1: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}'. "
                        + "Blocking line(s):\n  [index 0] IpAccessListLine{action=ACCEPT, matchCondition=PermittedByAcl{aclName=acl1, defaultAccept=false}}")
                .build());

    assertThat(generalAnswer.getRows().getData(), equalTo(expected));
    assertThat(specificAnswer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testMultipleCoveringLines() throws IOException {
    IpAccessList acl =
        _aclb
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(new IpWildcard("1.0.0.0:0.0.0.0").toIpSpace())
                            .build()),
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(new IpWildcard("1.0.0.1:0.0.0.0").toIpSpace())
                            .build()),
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(new IpWildcard("1.0.0.0:0.0.0.1").toIpSpace())
                            .build())))
            .setName("acl")
            .build();

    TableAnswerElement answer = answer(new AclReachability2Question());

    /*
     Construct the expected result. Line 2 should be blocked by both previous lines.
     Currently we are not finding the line numbers of multiple blocking lines, so list of blocking
     line numbers should be empty.
    */
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": " + acl.getName()))
                .put(AclLines2Rows.COL_LINES, new String[3])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 2)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of())
                .put(AclLines2Rows.COL_DIFF_ACTION, false)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl } contain(s) an unreachable line: '2: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=IpWildcardIpSpace{ipWildcard=1.0.0.0/31}}}}'."
                        + " Multiple earlier lines partially block this line, making it unreachable.")
                .build());

    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testIndependentlyUnmatchableLines() throws IOException {
    /*
    Construct ACL with lines:
    0. Reject 1.0.0.0/24 (unblocked)
    1. Accept 1.0.0.0/24 (blocked by line 0)
    2. Accept [empty set] (unmatchable)
    3. Accept 1.0.0.0/32 (blocked by line 0)
    4. Accept 1.2.3.4/32 (unblocked)
     */
    IpAccessList acl =
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
                            .build()),
                    IpAccessListLine.accepting().setMatchCondition(FalseExpr.INSTANCE).build(),
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/32").toIpSpace())
                            .build()),
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.2.3.4/32").toIpSpace())
                            .build())))
            .setName("acl")
            .build();

    TableAnswerElement answer = answer(new AclReachability2Question());

    // Construct the expected result
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": " + acl.getName()))
                .put(AclLines2Rows.COL_LINES, new String[5])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 1)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2Rows.COL_DIFF_ACTION, true)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl } contain(s) an unreachable line: '1: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}'. "
                        + "Blocking line(s):\n  [index 0] IpAccessListLine{action=REJECT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}")
                .build(),
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": " + acl.getName()))
                .put(AclLines2Rows.COL_LINES, new String[5])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 2)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of())
                .put(AclLines2Rows.COL_DIFF_ACTION, false)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl } contain(s) an unreachable line: '2: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=FalseExpr{}}'. This line will never match any packet, independent of preceding lines.")
                .build(),
            Row.builder()
                .put(
                    AclLines2Rows.COL_SOURCES,
                    ImmutableList.of(_c1.getName() + ": " + acl.getName()))
                .put(AclLines2Rows.COL_LINES, new String[5])
                .put(AclLines2Rows.COL_BLOCKED_LINE_NUM, 3)
                .put(AclLines2Rows.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2Rows.COL_DIFF_ACTION, true)
                .put(
                    AclLines2Rows.COL_MESSAGE,
                    "ACL(s) { c1: acl } contain(s) an unreachable line: '3: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/32}}}}'. "
                        + "Blocking line(s):\n  [index 0] IpAccessListLine{action=REJECT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}")
                .build());

    assertThat(answer.getRows().getData(), equalTo(expected));
  }

  @Test
  public void testOriginalAclNotMutated() throws IOException {
    // ACL that references an undefined ACL; that line should not change in original version
    IpAccessList acl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("???"))
                        .build()))
            .setName("acl")
            .build();

    TableAnswerElement answer = answer(new AclReachability2Question());

    // ACL's line should be the same as before
    assertThat(
        acl.getLines(),
        equalTo(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("???"))
                    .build())));

    // Config's ACL should be the same as the original version
    assertThat(_c1.getIpAccessLists().get(acl.getName()), equalTo(acl));
  }

  private TableAnswerElement answer(AclReachability2Question q) throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(_c1.getName(), _c1, _c2.getName(), _c2), _folder);
    AclReachability2Answerer answerer = new AclReachability2Answerer(q, batfish);
    return answerer.answer();
  }
}
