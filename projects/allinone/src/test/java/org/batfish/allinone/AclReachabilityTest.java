package org.batfish.allinone;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.AclReachabilityQuestionPlugin.AclReachabilityAnswerer;
import org.batfish.question.AclReachabilityQuestionPlugin.AclReachabilityQuestion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AclReachabilityTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testIndirection() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    IpAccessList.Builder aclb = nf.aclBuilder().setOwner(c);

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
}
