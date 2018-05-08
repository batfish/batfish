package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
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

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
  }

  @Test
  public void testIndirection() throws IOException {
    Configuration c = _cb.build();
    IpAccessList.Builder aclb = _nf.aclBuilder().setOwner(c);

    IpAccessList acl1 = aclb.setLines(ImmutableList.of()).setName("acl1").build();
    IpAccessList acl2 =
        aclb.setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl(acl1.getName()))
                        .build()))
            .setName("acl2")
            .build();

    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c.getName(), c), _folder);

    assertThat(c, hasIpAccessLists(hasEntry(acl1.getName(), acl1)));
    assertThat(c, hasIpAccessLists(hasEntry(acl2.getName(), acl2)));

    AclReachabilityQuestion question = new AclReachabilityQuestion();
    AclReachabilityAnswerer answerer = new AclReachabilityAnswerer(question, batfish);

    /*
     *  Test for NPE introduced by reverted PR #1272 due to missing definition for acl1 when
     *  processing acl2.
     */
    answerer.answer();
  }

  @Test
  public void testMultipleCoveringLines() throws IOException {
    Configuration c = _cb.build();
    String aclName = "acl";
    IpAccessList acl =
        _nf.aclBuilder()
            .setOwner(c)
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
    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c.getName(), c), _folder);

    assertThat(c, hasIpAccessLists(hasEntry(equalTo(aclName), equalTo(acl))));

    AclReachabilityQuestion question = new AclReachabilityQuestion();
    AclReachabilityAnswerer answerer = new AclReachabilityAnswerer(question, batfish);
    AnswerElement answer = answerer.answer();

    assertThat(answer, instanceOf(AclLinesAnswerElement.class));

    AclLinesAnswerElement aclLinesAnswerElement = (AclLinesAnswerElement) answer;

    assertThat(
        aclLinesAnswerElement.getUnreachableLines(),
        hasEntry(equalTo(c.getName()), hasEntry(equalTo(aclName), hasSize(1))));
    AclReachabilityEntry multipleBlockingLinesEntry =
        aclLinesAnswerElement.getUnreachableLines().get(c.getName()).get(aclName).first();
    assertThat(multipleBlockingLinesEntry.getEarliestMoreGeneralLineIndex(), equalTo(-1));
    assertThat(
        multipleBlockingLinesEntry.getEarliestMoreGeneralLineName(),
        equalTo("Multiple earlier lines partially block this line, making it unreachable."));
  }
}
