package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.IpAccessListLine.rejectingHeaderSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

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
import org.batfish.datamodel.answers.AclLines2AnswerElement;
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
    assertThat(baseClassAnswer, instanceOf(AclLines2AnswerElement.class));
    assertThat(specificBaseClassAnswer, instanceOf(AclLines2AnswerElement.class));
    AclLines2AnswerElement answer = (AclLines2AnswerElement) baseClassAnswer;
    AclLines2AnswerElement specificAnswer = (AclLines2AnswerElement) specificBaseClassAnswer;

    // Construct the expected rows set
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(AclLines2AnswerElement.COL_NODES, ImmutableList.of(_c.getName()))
                .put(AclLines2AnswerElement.COL_ACL, acl.getName())
                .put(AclLines2AnswerElement.COL_LINES, new String[2])
                .put(AclLines2AnswerElement.COL_BLOCKED_LINE_NUM, 1)
                .put(AclLines2AnswerElement.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2AnswerElement.COL_DIFF_ACTION, false)
                .put(
                    AclLines2AnswerElement.COL_MESSAGE,
                    "In node(s) '~Configuration_0~', ACL 'acl2' has an unreachable line '1: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}'. "
                        + "Blocking line(s):\n  [index 0] IpAccessListLine{action=ACCEPT, matchCondition=PermittedByAcl{aclName=acl1}}")
                .build());

    assertThat(answer.getInitialRows().getData(), equalTo(expected));
    assertThat(specificAnswer.getInitialRows().getData(), equalTo(expected));
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

    assertThat(baseClassAnswer, instanceOf(AclLines2AnswerElement.class));
    AclLines2AnswerElement answer = (AclLines2AnswerElement) baseClassAnswer;

    // Construct the expected rows set
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(AclLines2AnswerElement.COL_NODES, ImmutableList.of(_c.getName()))
                .put(AclLines2AnswerElement.COL_ACL, aclName)
                .put(AclLines2AnswerElement.COL_LINES, new String[3])
                .put(AclLines2AnswerElement.COL_BLOCKED_LINE_NUM, 2)
                .put(AclLines2AnswerElement.COL_BLOCKING_LINE_NUMS, ImmutableList.of())
                .put(AclLines2AnswerElement.COL_DIFF_ACTION, false)
                .put(
                    AclLines2AnswerElement.COL_MESSAGE,
                    "In node(s) '~Configuration_0~', ACL 'acl' has an unreachable line '2: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=IpWildcardIpSpace{ipWildcard=1.0.0.0/31}}}}'."
                        + " Multiple earlier lines partially block this line, making it unreachable.")
                .build());

    assertThat(answer.getInitialRows().getData(), equalTo(expected));
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

    assertThat(baseClassAnswer, instanceOf(AclLines2AnswerElement.class));
    AclLines2AnswerElement answer = (AclLines2AnswerElement) baseClassAnswer;

    // Construct the expected rows set
    Multiset<Row> expected =
        ImmutableMultiset.of(
            Row.builder()
                .put(AclLines2AnswerElement.COL_NODES, ImmutableList.of(_c.getName()))
                .put(AclLines2AnswerElement.COL_ACL, aclName)
                .put(AclLines2AnswerElement.COL_LINES, new String[5])
                .put(AclLines2AnswerElement.COL_BLOCKED_LINE_NUM, 1)
                .put(AclLines2AnswerElement.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2AnswerElement.COL_DIFF_ACTION, true)
                .put(
                    AclLines2AnswerElement.COL_MESSAGE,
                    "In node(s) '~Configuration_0~', ACL 'acl' has an unreachable line '1: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}'. "
                        + "Blocking line(s):\n  [index 0] IpAccessListLine{action=REJECT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}")
                .build(),
            Row.builder()
                .put(AclLines2AnswerElement.COL_NODES, ImmutableList.of(_c.getName()))
                .put(AclLines2AnswerElement.COL_ACL, aclName)
                .put(AclLines2AnswerElement.COL_LINES, new String[5])
                .put(AclLines2AnswerElement.COL_BLOCKED_LINE_NUM, 2)
                .put(AclLines2AnswerElement.COL_BLOCKING_LINE_NUMS, ImmutableList.of())
                .put(AclLines2AnswerElement.COL_DIFF_ACTION, false)
                .put(
                    AclLines2AnswerElement.COL_MESSAGE,
                    "In node(s) '~Configuration_0~', ACL 'acl' has an unreachable line '2: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=FalseExpr{}}'. This line will never match any packet, independent of preceding lines.")
                .build(),
            Row.builder()
                .put(AclLines2AnswerElement.COL_NODES, ImmutableList.of(_c.getName()))
                .put(AclLines2AnswerElement.COL_ACL, aclName)
                .put(AclLines2AnswerElement.COL_LINES, new String[5])
                .put(AclLines2AnswerElement.COL_BLOCKED_LINE_NUM, 3)
                .put(AclLines2AnswerElement.COL_BLOCKING_LINE_NUMS, ImmutableList.of(0))
                .put(AclLines2AnswerElement.COL_DIFF_ACTION, true)
                .put(
                    AclLines2AnswerElement.COL_MESSAGE,
                    "In node(s) '~Configuration_0~', ACL 'acl' has an unreachable line '3: IpAccessListLine{action=ACCEPT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/32}}}}'. "
                        + "Blocking line(s):\n  [index 0] IpAccessListLine{action=REJECT, "
                        + "matchCondition=MatchHeaderSpace{headerSpace=HeaderSpace{srcIps=PrefixIpSpace{prefix=1.0.0.0/24}}}}")
                .build());

    assertThat(answer.getInitialRows().getData(), equalTo(expected));
  }
}
