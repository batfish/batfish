package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.IpAccessListLine.rejectingHeaderSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
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
import org.batfish.datamodel.answers.AclLinesNewAnswerElement;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.table.Row;
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

  private NetworkFactory _nf;

  private Configuration.Builder _cb;

  private Configuration _c;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c = _cb.build();
  }

  @Test
  public void testIndirection() throws IOException {
    IpAccessList.Builder aclb = _nf.aclBuilder().setOwner(_c);

    /*
    Reference ACL contains 1 line: Permit 1.0.0.0/24
    Main ACL contains 2 lines:
    0. Permit anything that reference ACL permits
    1. Permit 1.0.0.0/24

    Runs two questions:
    1. General ACL reachability (reference ACL won't be encoded after first NoD step)
    2. Reachability specifically for main ACL (reference ACL won't be encoded at all)
    Tests that both find line 1 to be blocked by line 0 in main ACL.
     */

    IpAccessList referencedAcl =
        aclb.setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .setName("acl1")
            .build();
    IpAccessList acl =
        aclb.setLines(
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

    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_c.getName(), _c), _folder);

    assertThat(_c, hasIpAccessLists(hasEntry(referencedAcl.getName(), referencedAcl)));
    assertThat(_c, hasIpAccessLists(hasEntry(acl.getName(), acl)));

    AclReachability2Question question = new AclReachability2Question();
    AclReachability2Answerer answerer = new AclReachability2Answerer(question, batfish);
    question.setAclNameRegex(acl.getName());
    AclReachability2Answerer specificAnswerer = new AclReachability2Answerer(question, batfish);

    AnswerElement baseClassAnswer = answerer.answer();
    AnswerElement specificBaseClassAnswer = specificAnswerer.answer();
    assertThat(baseClassAnswer, instanceOf(AclLinesNewAnswerElement.class));
    assertThat(specificBaseClassAnswer, instanceOf(AclLinesNewAnswerElement.class));
    AclLinesNewAnswerElement answer = (AclLinesNewAnswerElement) baseClassAnswer;
    AclLinesNewAnswerElement specificAnswer = (AclLinesNewAnswerElement) specificBaseClassAnswer;

    // Tests for general ACL reachability answer
    Multiset<Row> rows = answer.getInitialRows().getData();
    assertThat(rows.size(), equalTo(1));
    Row row = (Row) rows.toArray()[0];
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_NODES, new TypeReference<List<String>>() {}),
        equalTo(ImmutableList.of(_c.getName())));
    assertThat(row.get(AclLinesNewAnswerElement.COL_ACL, String.class), equalTo(acl.getName()));
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_LINES, new TypeReference<List<String>>() {}),
        hasSize(2));
    assertThat(
        "Line 1 is blocked",
        row.get(AclLinesNewAnswerElement.COL_BLOCKED_LINE_NUM, Integer.class),
        equalTo(1));
    assertThat(
        "Line 0 is blocking",
        row.get(
            AclLinesNewAnswerElement.COL_BLOCKING_LINE_NUMS, new TypeReference<List<Integer>>() {}),
        equalTo(ImmutableList.of(0)));
    assertThat(row.get(AclLinesNewAnswerElement.COL_DIFF_ACTION, Boolean.class), equalTo(false));

    // Tests for ACL reachability of main ACL specifically
    rows = specificAnswer.getInitialRows().getData();
    assertThat(rows.size(), equalTo(1));
    row = (Row) rows.toArray()[0];
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_NODES, new TypeReference<List<String>>() {}),
        equalTo(ImmutableList.of(_c.getName())));
    assertThat(row.get(AclLinesNewAnswerElement.COL_ACL, String.class), equalTo(acl.getName()));
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_LINES, new TypeReference<List<String>>() {}),
        hasSize(2));
    assertThat(
        "Line 1 is blocked",
        row.get(AclLinesNewAnswerElement.COL_BLOCKED_LINE_NUM, Integer.class),
        equalTo(1));
    assertThat(
        "Line 0 is blocking",
        row.get(
            AclLinesNewAnswerElement.COL_BLOCKING_LINE_NUMS, new TypeReference<List<Integer>>() {}),
        equalTo(ImmutableList.of(0)));
    assertThat(row.get(AclLinesNewAnswerElement.COL_DIFF_ACTION, Boolean.class), equalTo(false));
  }

  @Test
  public void testMultipleCoveringLines() throws IOException {
    String aclName = "acl";
    IpAccessList acl =
        _nf.aclBuilder()
            .setOwner(_c)
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
            .setName(aclName)
            .build();
    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_c.getName(), _c), _folder);

    assertThat(_c, hasIpAccessLists(hasEntry(equalTo(aclName), equalTo(acl))));

    AclReachability2Question question = new AclReachability2Question();
    AclReachability2Answerer answerer = new AclReachability2Answerer(question, batfish);
    AnswerElement baseClassAnswer = answerer.answer();

    assertThat(baseClassAnswer, instanceOf(AclLinesNewAnswerElement.class));
    AclLinesNewAnswerElement answer = (AclLinesNewAnswerElement) baseClassAnswer;

    Multiset<Row> rows = answer.getInitialRows().getData();
    assertThat(rows.size(), equalTo(1));
    Row row = (Row) rows.toArray()[0];
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_NODES, new TypeReference<List<String>>() {}),
        equalTo(ImmutableList.of(_c.getName())));
    assertThat(row.get(AclLinesNewAnswerElement.COL_ACL, String.class), equalTo(aclName));
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_LINES, new TypeReference<List<String>>() {}),
        hasSize(3));
    assertThat(
        "Line 2 is blocked",
        row.get(AclLinesNewAnswerElement.COL_BLOCKED_LINE_NUM, Integer.class),
        equalTo(2));
    assertThat(
        row.get(
            AclLinesNewAnswerElement.COL_BLOCKING_LINE_NUMS, new TypeReference<List<Integer>>() {}),
        hasSize(0));
    assertThat(row.get(AclLinesNewAnswerElement.COL_DIFF_ACTION, Boolean.class), equalTo(false));
    assertThat(
        row.get(AclLinesNewAnswerElement.COL_MESSAGE, String.class)
            .contains("Multiple earlier lines partially block this line, making it unreachable."),
        equalTo(true));
  }

  @Test
  public void testIndependentlyUnmatchableLines() throws IOException {
    String aclName = "acl";
    IpAccessList acl =
        _nf.aclBuilder()
            .setOwner(_c)
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
            .setName(aclName)
            .build();
    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_c.getName(), _c), _folder);

    assertThat(_c, hasIpAccessLists(hasEntry(equalTo(aclName), equalTo(acl))));

    AclReachability2Question question = new AclReachability2Question();
    AclReachability2Answerer answerer = new AclReachability2Answerer(question, batfish);
    AnswerElement baseClassAnswer = answerer.answer();

    assertThat(baseClassAnswer, instanceOf(AclLinesNewAnswerElement.class));
    AclLinesNewAnswerElement answer = (AclLinesNewAnswerElement) baseClassAnswer;

    Multiset<Row> rows = answer.getInitialRows().getData();
    assertThat(rows.size(), equalTo(3));

    // Check that one of the rows has the right nodes, ACL name, and lines
    Row firstRow = (Row) rows.toArray()[0];
    assertThat(
        firstRow.get(AclLinesNewAnswerElement.COL_NODES, new TypeReference<List<String>>() {}),
        equalTo(ImmutableList.of(_c.getName())));
    assertThat(firstRow.get(AclLinesNewAnswerElement.COL_ACL, String.class), equalTo(aclName));
    assertThat(
        firstRow.get(AclLinesNewAnswerElement.COL_LINES, new TypeReference<List<String>>() {}),
        hasSize(5));

    // Ensure the blocked lines are lines 1, 2, and 3
    assertThat(
        "Lines 1, 2, and 3 are unreachable",
        rows.stream()
            .map(r -> r.get(AclLinesNewAnswerElement.COL_BLOCKED_LINE_NUM, Integer.class))
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(1, 2, 3)));

    for (Row row : rows) {
      int blockedLineNum = row.get(AclLinesNewAnswerElement.COL_BLOCKED_LINE_NUM, Integer.class);
      if (blockedLineNum == 1 || blockedLineNum == 3) {
        // Lines 1 and 3: Blocked by line 0 but not unmatchable
        assertThat(
            row.get(
                AclLinesNewAnswerElement.COL_BLOCKING_LINE_NUMS,
                new TypeReference<List<Integer>>() {}),
            equalTo(ImmutableList.of(0)));
        assertThat(row.get(AclLinesNewAnswerElement.COL_DIFF_ACTION, Boolean.class), equalTo(true));
      } else {
        // Line 2: Unmatchable
        assertThat(
            row.get(
                AclLinesNewAnswerElement.COL_BLOCKING_LINE_NUMS,
                new TypeReference<List<Integer>>() {}),
            hasSize(0));
        assertThat(
            row.get(AclLinesNewAnswerElement.COL_MESSAGE, String.class)
                .contains("This line will never match any packet, independent of preceding lines."),
            equalTo(true));
        assertThat(
            row.get(AclLinesNewAnswerElement.COL_DIFF_ACTION, Boolean.class), equalTo(false));
      }
    }
  }
}
