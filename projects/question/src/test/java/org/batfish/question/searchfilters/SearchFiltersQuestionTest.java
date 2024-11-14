package org.batfish.question.searchfilters;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocols;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Arrays;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceAssignmentSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SearchFiltersQuestionTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testJsonSerialization() {
    SearchFiltersQuestion q =
        SearchFiltersQuestion.builder()
            .setAction("deny")
            .setFilterSpecifier("filter")
            .setNodeSpecifier("node")
            .setHeaders(PacketHeaderConstraints.builder().setDstIp("1.1.1.1").build())
            .setStartLocation("start")
            .setComplementHeaderSpace(true)
            .build();
    SearchFiltersQuestion clone = BatfishObjectMapper.clone(q, SearchFiltersQuestion.class);
    assertThat(clone.getQuery(), is(DenyQuery.INSTANCE));
    assertThat(clone.getFilters(), equalTo(q.getFilters()));
    assertThat(clone.getNodes(), equalTo(q.getNodes()));
    assertThat(clone.getHeaderConstraints(), equalTo(q.getHeaderConstraints()));
    assertThat(clone.getStartLocation(), equalTo(q.getStartLocation()));
    assertThat(clone.getComplementHeaderSpace(), equalTo(q.getComplementHeaderSpace()));
  }

  @Test
  public void testDeserializationDefaultValues() throws IOException {
    String serialized =
        String.format("{\"class\":\"%s\"}", SearchFiltersQuestion.class.getCanonicalName());
    SearchFiltersQuestion q =
        BatfishObjectMapper.mapper().readValue(serialized, SearchFiltersQuestion.class);

    assertThat(q.getFilterSpecifier(), notNullValue());
    assertThat(q.getQuery(), is(PermitQuery.INSTANCE));
    assertThat(q.getNodesSpecifier(), notNullValue());
    assertThat(q.getDataPlane(), equalTo(false));
    assertThat(q.getNodes(), nullValue());
    assertThat(q.getStartLocation(), nullValue());
    assertEquals(
        q.getHeaderSpaceExpr(),
        and(matchSrc(UniverseIpSpace.INSTANCE), matchDst(UniverseIpSpace.INSTANCE)));
    // src/dst IPs are in specifiers at this stage
    SearchFiltersParameters parameters = q.toSearchFiltersParameters();
    assertThat(parameters.getStartLocationSpecifier(), equalTo(LocationSpecifier.ALL_LOCATIONS));

    for (IpSpaceAssignmentSpecifier s :
        Arrays.asList(
            parameters.getSourceIpSpaceSpecifier(), parameters.getDestinationIpSpaceSpecifier())) {
      assertThat(
          s.resolve(ImmutableSet.of(), MockSpecifierContext.builder().build()).getEntries().stream()
              .map(Entry::getIpSpace)
              .collect(ImmutableList.toImmutableList()),
          hasItem(UniverseIpSpace.INSTANCE));
    }
  }

  @Test
  public void testGenerateQuery() {
    SearchFiltersQuestion question = new SearchFiltersQuestion();
    assertThat(question.getQuery(), is(PermitQuery.INSTANCE));

    question = SearchFiltersQuestion.builder().setAction("permit").build();
    assertThat(question.getQuery(), is(PermitQuery.INSTANCE));

    question = SearchFiltersQuestion.builder().setAction("PermIT").build();
    assertThat(question.getQuery(), is(PermitQuery.INSTANCE));

    question = SearchFiltersQuestion.builder().setAction("deny").build();
    assertThat(question.getQuery(), is(DenyQuery.INSTANCE));

    question = SearchFiltersQuestion.builder().setAction("DenY").build();
    assertThat(question.getQuery(), is(DenyQuery.INSTANCE));

    question = SearchFiltersQuestion.builder().setAction("matchLine 5").build();
    SearchFiltersQuery query = question.getQuery();
    assertThat(query, instanceOf(MatchLineQuery.class));
    assertThat(((MatchLineQuery) query).getLineNum(), is(5));

    question = SearchFiltersQuestion.builder().setAction("maTChLine 5").build();
    query = question.getQuery();
    assertThat(query, instanceOf(MatchLineQuery.class));
    assertThat(((MatchLineQuery) query).getLineNum(), is(5));

    exception.expect(BatfishException.class);
    exception.expectMessage("Unrecognized action 'foo'");
    SearchFiltersQuestion.builder().setAction("foo").build();
  }

  @Test
  public void testGenerateQuery_badLineIndex() {
    exception.expect(BatfishException.class);
    exception.expectMessage("Line index for matchLine must be zero or higher");
    SearchFiltersQuestion.builder().setAction("matchLine -1").build();
  }

  @Test
  public void testIpProtocols() {
    ImmutableSortedSet<IpProtocol> ipProtocols =
        ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.ICMP);
    SearchFiltersQuestion question =
        SearchFiltersQuestion.builder()
            .setHeaders(PacketHeaderConstraints.builder().setIpProtocols(ipProtocols).build())
            .build();

    assertEquals(
        question.getHeaderSpaceExpr(),
        and(
            matchSrc(UniverseIpSpace.INSTANCE),
            matchDst(UniverseIpSpace.INSTANCE),
            matchIpProtocols(IpProtocol.ICMP, IpProtocol.TCP)));

    // test (de)serialization
    question = BatfishObjectMapper.clone(question, SearchFiltersQuestion.class);
    assertEquals(
        question.getHeaderSpaceExpr(),
        and(
            matchSrc(UniverseIpSpace.INSTANCE),
            matchDst(UniverseIpSpace.INSTANCE),
            matchIpProtocols(IpProtocol.ICMP, IpProtocol.TCP)));
  }
}
