package org.batfish.question.searchfilters;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.question.searchfilters.SearchFiltersAnswerer.canQuery;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Test;

/** Tests of {@link SearchFiltersAnswerer}. */
public class SearchFiltersAnswererTest {
  private static final IpAccessList ACL1 =
      IpAccessList.builder()
          .setName("acl1")
          .setLines(
              ImmutableList.of(
                  ExprAclLine.accepting().setMatchCondition(matchDstIp("1.1.1.1")).build()))
          .build();
  private static final IpAccessList ACL2 =
      IpAccessList.builder()
          .setName("acl2")
          .setLines(
              ImmutableList.of(
                  ExprAclLine.accepting().setMatchCondition(matchDstIp("2.2.2.2")).build()))
          .build();

  @Test
  public void testCanQuery() {
    // Permit or deny queries should work
    SearchFiltersQuestion permit = SearchFiltersQuestion.builder().setAction("permit").build();
    SearchFiltersQuestion deny = SearchFiltersQuestion.builder().setAction("deny").build();
    assertTrue(canQuery(ACL1, permit));
    assertTrue(canQuery(ACL1, deny));

    // Match line queries should work only if the line number is within range for the ACL
    int numLines = ACL1.getLines().size();
    SearchFiltersQuestion matchLastLine =
        SearchFiltersQuestion.builder().setAction("matchLine " + (numLines - 1)).build();
    SearchFiltersQuestion matchLineOutOfRange =
        SearchFiltersQuestion.builder().setAction("matchLine " + numLines).build();
    assertTrue(canQuery(ACL1, matchLastLine));
    assertFalse(canQuery(ACL1, matchLineOutOfRange));
  }

  @Test
  public void testGetSpecifiedAcls_includeAll() {
    Configuration c = createConfigWithAcls(ACL1, ACL2);
    IBatfish bf = new MockBatfish(c);
    SearchFiltersQuestion question = SearchFiltersQuestion.builder().build();
    SearchFiltersAnswerer answerer = new SearchFiltersAnswerer(question, bf);
    assertThat(
        answerer.getSpecifiedAcls(bf.getSnapshot(), question),
        equalTo(ImmutableMap.of(c.getHostname(), c.getIpAccessLists())));
  }

  @Test
  public void testGetSpecifiedAcls_onlyAcl1() {
    Configuration c = createConfigWithAcls(ACL1, ACL2);
    IBatfish bf = new MockBatfish(c);
    SearchFiltersQuestion question =
        SearchFiltersQuestion.builder()
            .setNodeSpecifier(c.getHostname())
            .setFilterSpecifier(ACL1.getName())
            .build();
    SearchFiltersAnswerer answerer = new SearchFiltersAnswerer(question, bf);
    assertThat(
        answerer.getSpecifiedAcls(bf.getSnapshot(), question),
        equalTo(ImmutableMap.of(c.getHostname(), ImmutableMap.of(ACL1.getName(), ACL1))));
  }

  @Test
  public void testGetSpecifiedAcls_noHostnameMatch() {
    Configuration c = createConfigWithAcls(ACL1);
    IBatfish bf = new MockBatfish(c);
    SearchFiltersQuestion question =
        SearchFiltersQuestion.builder().setNodeSpecifier("unknown_hostname").build();
    SearchFiltersAnswerer answerer = new SearchFiltersAnswerer(question, bf);
    assertThat(answerer.getSpecifiedAcls(bf.getSnapshot(), question), anEmptyMap());
  }

  @Test
  public void testGetSpecifiedAcls_noFilterMatch() {
    Configuration c = createConfigWithAcls(ACL1);
    IBatfish bf = new MockBatfish(c);
    SearchFiltersQuestion question =
        SearchFiltersQuestion.builder().setFilterSpecifier("unknown_filter").build();
    SearchFiltersAnswerer answerer = new SearchFiltersAnswerer(question, bf);
    assertThat(
        answerer.getSpecifiedAcls(bf.getSnapshot(), question),
        equalTo(ImmutableMap.of(c.getHostname(), ImmutableMap.of())));
  }

  private static Configuration createConfigWithAcls(IpAccessList... acls) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    for (IpAccessList acl : acls) {
      c.getIpAccessLists().put(acl.getName(), acl);
    }
    return c;
  }
}
