package org.batfish.allinone;

import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.batfish.datamodel.matchers.DataModelMatchers.forAll;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasEvents;
import static org.batfish.datamodel.matchers.DataModelMatchers.isDefaultDeniedByIpAccessListNamed;
import static org.batfish.datamodel.matchers.DataModelMatchers.isPermittedByIpAccessListLineThat;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.tracefilters.TraceFiltersAnswerer.COLUMN_FILTER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.matchers.PermittedByIpAccessListLineMatchers;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.tracefilters.TraceFiltersAnswerer;
import org.batfish.question.tracefilters.TraceFiltersQuestion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TraceFiltersTest {

  private Configuration _c;

  private Configuration.Builder _cb;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private NetworkFactory _nf;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c = _cb.build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testIndirection() throws IOException {
    IpAccessList.Builder aclb = _nf.aclBuilder().setOwner(_c);

    /*
    Reference ACL contains 1 line: Permit 1.0.0.0/24
    Main ACL contains 2 lines:
    0. Permit anything that reference ACL permits
    1. Permit 1.0.0.0/24
     */

    IpAccessList referencedAcl =
        aclb.setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/32").toIpSpace())
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

    TraceFiltersQuestion question =
        new TraceFiltersQuestion(NodesSpecifier.ALL, FiltersSpecifier.ALL);
    question.setSrcIp(new Ip("1.0.0.4"));
    TraceFiltersAnswerer answerer = new TraceFiltersAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer();

    /* Trace should be present for referencing acl with two events: being denied by referenced acl, and permited by referencing acl in later line */
    assertThat(
        answer,
        hasRows(
            forAll(
                hasColumn(
                    COLUMN_FILTER_NAME, equalTo(acl.getName()), new TypeReference<String>() {}),
                hasColumn(
                    TraceFiltersAnswerer.COLUMN_TRACE,
                    hasEvents(
                        contains(
                            isDefaultDeniedByIpAccessListNamed(referencedAcl.getName()),
                            isPermittedByIpAccessListLineThat(
                                PermittedByIpAccessListLineMatchers.hasName(acl.getName())))),
                    new TypeReference<AclTrace>() {}))));
    /* Trace should be present for referenced acl with one event: not matching the referenced acl */
    assertThat(
        answer,
        hasRows(
            forAll(
                hasColumn(
                    COLUMN_FILTER_NAME,
                    equalTo(referencedAcl.getName()),
                    new TypeReference<String>() {}),
                hasColumn(
                    TraceFiltersAnswerer.COLUMN_TRACE,
                    hasEvents(
                        contains(isDefaultDeniedByIpAccessListNamed(referencedAcl.getName()))),
                    new TypeReference<AclTrace>() {}))));
    System.out.print(answer.prettyPrint());
  }

  @Test
  public void testOneRowPerAclPerConfig() throws IOException {
    IpAccessList.Builder aclb = _nf.aclBuilder();
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Configuration c3 = _cb.build();

    // Create 2 ACLs for each of 3 configs.
    aclb.setOwner(c1).build();
    aclb.build();
    aclb.setOwner(c2).build();
    aclb.build();
    aclb.setOwner(c3).build();
    aclb.build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(c1.getName(), c1, c2.getName(), c2, c3.getName(), c3), _folder);
    TraceFiltersQuestion question =
        new TraceFiltersQuestion(NodesSpecifier.ALL, FiltersSpecifier.ALL);
    TraceFiltersAnswerer answerer = new TraceFiltersAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer();

    // There should be 6 rows
    assertThat(answer.getRows(), hasSize(6));
  }
}
