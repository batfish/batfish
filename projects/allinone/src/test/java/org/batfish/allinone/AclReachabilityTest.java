package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.IpAccessListLine.rejectingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedSet;
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
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.AclLinesAnswerElement.AclReachabilityEntry;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.AclReachabilityQuestionPlugin.AclReachabilityAnswerer;
import org.batfish.question.AclReachabilityQuestionPlugin.AclReachabilityQuestion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AclReachabilityTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration _c;

  private IpAccessList.Builder _aclb;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c = cb.build();
    _aclb = nf.aclBuilder().setOwner(_c);
  }

  @Test
  public void test2CircularReferences() throws IOException {
    // acl1 permits anything acl2 permits
    // acl2 permits anything acl1 permits
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("acl2"))
                    .setName("reference acl2")
                    .build()))
        .setName("acl1")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("acl1"))
                    .setName("reference acl1")
                    .build()))
        .setName("acl2")
        .build();

    AclLinesAnswerElement answer = answer(new AclReachabilityQuestion());

    // Should find a cycle result for each ACL.
    assertThat(answer.getUnreachableLines().keySet(), equalTo(ImmutableSet.of(_c.getName())));
    assertThat(
        answer.getUnreachableLines().get(_c.getName()).keySet(),
        equalTo(ImmutableSet.of("acl1", "acl2")));

    AclReachabilityEntry entry1 =
        answer.getUnreachableLines().get(_c.getName()).get("acl1").first();
    assertThat(entry1.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        entry1.getEarliestMoreGeneralLineName(),
        equalTo("This line contains a reference that is part of a circular chain of references."));
    AclReachabilityEntry entry2 =
        answer.getUnreachableLines().get(_c.getName()).get("acl2").first();
    assertThat(entry2.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        entry2.getEarliestMoreGeneralLineName(),
        equalTo("This line contains a reference that is part of a circular chain of references."));
  }

  @Test
  public void test3CircularReferences() throws IOException {
    // acl0 permits anything acl1 permits
    // acl1 permits anything acl2 permits, plus 1 other line to avoid acl0's line being unmatchable
    // acl2 permits anything acl0 permits
    // acl3 permits anything acl1 permits (not part of cycle)
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("acl1"))
                    .setName("reference acl1 from acl0")
                    .build()))
        .setName("acl0")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("acl2"))
                    .setName("reference acl2 from acl1")
                    .build(),
                acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                        .build())))
        .setName("acl1")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("acl0"))
                    .setName("reference acl0 from acl2")
                    .build()))
        .setName("acl2")
        .build();
    _aclb
        .setLines(
            ImmutableList.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(new PermittedByAcl("acl1"))
                    .setName("reference acl1 from acl3")
                    .build()))
        .setName("acl3")
        .build();

    AclLinesAnswerElement answer = answer(new AclReachabilityQuestion());

    // Should find 3 cycle results for ACLs 0, 1, and 2.
    assertThat(answer.getUnreachableLines().keySet(), equalTo(ImmutableSet.of(_c.getName())));
    assertThat(
        answer.getUnreachableLines().get(_c.getName()).keySet(),
        equalTo(ImmutableSet.of("acl0", "acl1", "acl2")));

    AclReachabilityEntry entry0 =
        answer.getUnreachableLines().get(_c.getName()).get("acl0").first();
    assertThat(entry0.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        entry0.getEarliestMoreGeneralLineName(),
        equalTo("This line contains a reference that is part of a circular chain of references."));
    AclReachabilityEntry entry1 =
        answer.getUnreachableLines().get(_c.getName()).get("acl1").first();
    assertThat(entry1.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        entry1.getEarliestMoreGeneralLineName(),
        equalTo("This line contains a reference that is part of a circular chain of references."));
    AclReachabilityEntry entry2 =
        answer.getUnreachableLines().get(_c.getName()).get("acl2").first();
    assertThat(entry2.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        entry2.getEarliestMoreGeneralLineName(),
        equalTo("This line contains a reference that is part of a circular chain of references."));
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

    AclLinesAnswerElement answer = answer(new AclReachabilityQuestion());

    // Should find an undefined ACL result.
    assertThat(answer.getUnreachableLines().keySet(), equalTo(ImmutableSet.of(_c.getName())));
    assertThat(
        answer.getUnreachableLines().get(_c.getName()).keySet(), equalTo(ImmutableSet.of("acl")));

    AclReachabilityEntry entry0 = answer.getUnreachableLines().get(_c.getName()).get("acl").first();
    assertThat(entry0.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        entry0.getEarliestMoreGeneralLineName(),
        equalTo(
            "This line will never match any packet because it references an undefined structure."));
  }

  @Test
  public void testIndirection() throws IOException {
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

    AclLinesAnswerElement answer = answer(new AclReachabilityQuestion());
    AclLinesAnswerElement specificAnswer = answer(new AclReachabilityQuestion(acl.getName(), null));

    // Tests for general ACL reachability answer
    assertThat(
        answer.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(acl.getName()), hasSize(1))));
    AclReachabilityEntry blockedLine =
        answer.getUnreachableLines().get(_c.getName()).get(acl.getName()).first();
    assertThat("Line 0 is blocking", blockedLine.getEarliestMoreGeneralLineIndex(), equalTo(0));
    assertThat("Line 1 is blocked", blockedLine.getIndex(), equalTo(1));
    assertThat("Same action", blockedLine.getDifferentAction(), equalTo(false));

    // Tests for ACL reachability of main ACL specifically
    assertThat(
        specificAnswer.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(acl.getName()), hasSize(1))));
    blockedLine = specificAnswer.getUnreachableLines().get(_c.getName()).get(acl.getName()).first();
    assertThat("Line 0 is blocking", blockedLine.getEarliestMoreGeneralLineIndex(), equalTo(0));
    assertThat("Line 1 is blocked", blockedLine.getIndex(), equalTo(1));
    assertThat("Same action", blockedLine.getDifferentAction(), equalTo(false));
  }

  @Test
  public void testMultipleCoveringLines() throws IOException {
    String aclName = "acl";
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
            .setName(aclName)
            .build();

    AclLinesAnswerElement answer = answer(new AclReachabilityQuestion());

    assertThat(
        answer.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(aclName), hasSize(1))));
    AclReachabilityEntry multipleBlockingLinesEntry =
        answer.getUnreachableLines().get(_c.getName()).get(aclName).first();
    assertThat(multipleBlockingLinesEntry.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        multipleBlockingLinesEntry.getEarliestMoreGeneralLineName(),
        equalTo("Multiple earlier lines partially block this line, making it unreachable."));
    assertThat(multipleBlockingLinesEntry.getDifferentAction(), equalTo(false));
  }

  @Test
  public void testIndependentlyUnmatchableLines() throws IOException {
    String aclName = "acl";
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
            .setName(aclName)
            .build();

    AclLinesAnswerElement answer = answer(new AclReachabilityQuestion());

    assertThat(
        answer.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(aclName), hasSize(3))));

    SortedSet<AclReachabilityEntry> unreachableLines =
        answer.getUnreachableLines().get(_c.getName()).get(aclName);
    assertThat(
        "Lines 1, 2, and 3 are unreachable",
        unreachableLines.stream().map(entry -> entry.getIndex()).collect(Collectors.toSet()),
        contains(1, 2, 3));

    for (AclReachabilityEntry entry : unreachableLines) {
      if (entry.getIndex() == 1 || entry.getIndex() == 3) {
        // Lines 1 and 3: Blocked by line 0 but not unmatchable
        assertThat(entry.getEarliestMoreGeneralLineIndex(), equalTo(0));
        assertThat(entry.getDifferentAction(), equalTo(true));
      } else {
        // Line 2: Unmatchable
        assertThat(entry.getEarliestMoreGeneralLineIndex(), equalTo(-1));
        assertThat(entry.getDifferentAction(), equalTo(false));
        assertThat(
            entry.getEarliestMoreGeneralLineName(),
            equalTo("This line will never match any packet, independent of preceding lines."));
      }
    }
  }

  private AclLinesAnswerElement answer(AclReachabilityQuestion q) throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_c.getName(), _c), _folder);
    AclReachabilityAnswerer answerer = new AclReachabilityAnswerer(q, batfish);
    return (AclLinesAnswerElement) answerer.answer();
  }
}
