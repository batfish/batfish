package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.google.common.collect.ImmutableList;
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
import org.batfish.datamodel.answers.AnswerElement;
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

    AclReachabilityQuestion question = new AclReachabilityQuestion();
    AclReachabilityAnswerer answerer = new AclReachabilityAnswerer(question, batfish);
    question.setAclNameRegex(acl.getName());
    AclReachabilityAnswerer specificAnswerer = new AclReachabilityAnswerer(question, batfish);

    AnswerElement answer = answerer.answer();
    AnswerElement specificAnswer = specificAnswerer.answer();
    assertThat(answer, instanceOf(AclLinesAnswerElement.class));
    assertThat(specificAnswer, instanceOf(AclLinesAnswerElement.class));
    AclLinesAnswerElement aclLinesAnswerElement = (AclLinesAnswerElement) answer;
    AclLinesAnswerElement specificAclLinesAnswerElement = (AclLinesAnswerElement) specificAnswer;

    // Tests for general ACL reachability answer
    assertThat(
        aclLinesAnswerElement.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(acl.getName()), hasSize(1))));
    AclReachabilityEntry blockedLine =
        aclLinesAnswerElement.getUnreachableLines().get(_c.getName()).get(acl.getName()).first();
    assertThat("Line 0 is blocking", blockedLine.getEarliestMoreGeneralLineIndex(), equalTo(0));
    assertThat("Line 1 is blocked", blockedLine.getIndex(), equalTo(1));

    // Tests for ACL reachability of main ACL specifically
    assertThat(
        specificAclLinesAnswerElement.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(acl.getName()), hasSize(1))));
    blockedLine =
        specificAclLinesAnswerElement
            .getUnreachableLines()
            .get(_c.getName())
            .get(acl.getName())
            .first();
    assertThat("Line 0 is blocking", blockedLine.getEarliestMoreGeneralLineIndex(), equalTo(0));
    assertThat("Line 1 is blocked", blockedLine.getIndex(), equalTo(1));
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

    AclReachabilityQuestion question = new AclReachabilityQuestion();
    AclReachabilityAnswerer answerer = new AclReachabilityAnswerer(question, batfish);
    AnswerElement answer = answerer.answer();

    assertThat(answer, instanceOf(AclLinesAnswerElement.class));

    AclLinesAnswerElement aclLinesAnswerElement = (AclLinesAnswerElement) answer;

    assertThat(
        aclLinesAnswerElement.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(aclName), hasSize(1))));
    AclReachabilityEntry multipleBlockingLinesEntry =
        aclLinesAnswerElement.getUnreachableLines().get(_c.getName()).get(aclName).first();
    assertThat(multipleBlockingLinesEntry.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        multipleBlockingLinesEntry.getEarliestMoreGeneralLineName(),
        equalTo("Multiple earlier lines partially block this line, making it unreachable."));
  }

  @Test
  public void testIndependentlyUnmatchableLines() throws IOException {
    String aclName = "acl";
    IpAccessList acl =
        _nf.aclBuilder()
            .setOwner(_c)
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
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

    AclReachabilityQuestion question = new AclReachabilityQuestion();
    AclReachabilityAnswerer answerer = new AclReachabilityAnswerer(question, batfish);
    AnswerElement answer = answerer.answer();

    assertThat(answer, instanceOf(AclLinesAnswerElement.class));

    AclLinesAnswerElement aclLinesAnswerElement = (AclLinesAnswerElement) answer;

    assertThat(
        aclLinesAnswerElement.getUnreachableLines(),
        hasEntry(equalTo(_c.getName()), hasEntry(equalTo(aclName), hasSize(3))));

    SortedSet<AclReachabilityEntry> unreachableLines =
        aclLinesAnswerElement.getUnreachableLines().get(_c.getName()).get(aclName);
    assertThat(
        "Lines 1, 2, and 3 are unreachable",
        unreachableLines.stream().map(entry -> entry.getIndex()).collect(Collectors.toSet()),
        contains(1, 2, 3));

    for (AclReachabilityEntry entry : unreachableLines) {
      if (entry.getIndex() == 1 || entry.getIndex() == 3) {
        // Lines 1 and 3: Blocked by line 0 but not unmatchable
        assertThat(entry.getEarliestMoreGeneralLineIndex(), equalTo(0));
      } else {
        // Line 2: Unmatchable
        assertThat(entry.getEarliestMoreGeneralLineIndex(), equalTo(-1));
        assertThat(
            entry.getEarliestMoreGeneralLineName(),
            equalTo("This line will never match any packet, independent of preceding lines."));
      }
    }
  }
}
