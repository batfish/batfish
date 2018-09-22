package org.batfish.question.searchfilters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
import org.batfish.question.searchfilters.SearchFiltersQuestion.Type;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SearchFiltersQuestionTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testDeserializationDefaultValues() throws IOException {
    String serialized =
        String.format("{\"class\":\"%s\"}", SearchFiltersQuestion.class.getCanonicalName());
    SearchFiltersQuestion q =
        BatfishObjectMapper.mapper().readValue(serialized, SearchFiltersQuestion.class);

    assertThat(q.getFilterSpecifier(), notNullValue());
    assertThat(q.getType(), is(Type.PERMIT));
    assertThat(q.getNodesSpecifier(), notNullValue());
    assertThat(q.getDataPlane(), equalTo(false));
    assertThat(q.getNodes(), nullValue());
    // src/dst IPs NOT stored in headerspace at this stage
    assertThat(q.getHeaderSpace().getDstIps(), nullValue());
    assertThat(q.getHeaderSpace().getSrcIps(), nullValue());
    // src/dst IPs are in specifiers at this stage
    SearchFiltersParameters parameters = q.toSearchFiltersParameters();

    for (IpSpaceSpecifier s :
        Arrays.asList(
            parameters.getSourceIpSpaceSpecifier(), parameters.getDestinationIpSpaceSpecifier())) {
      assertThat(
          s.resolve(ImmutableSet.of(), MockSpecifierContext.builder().build())
              .getEntries()
              .stream()
              .map(Entry::getIpSpace)
              .collect(ImmutableList.toImmutableList()),
          hasItem(UniverseIpSpace.INSTANCE));
      assertThat(q.getLineNumber(), nullValue());
    }
  }

  @Test
  public void testSetQuery() {
    SearchFiltersQuestion question = new SearchFiltersQuestion();
    assertThat(question.getType(), is(Type.PERMIT));
    assertThat(question.getLineNumber(), nullValue());

    question = SearchFiltersQuestion.builder().setAction("deny").build();
    assertThat(question.getType(), is(Type.DENY));
    assertThat(question.getLineNumber(), nullValue());

    question = SearchFiltersQuestion.builder().setAction("matchLine 5").build();
    assertThat(question.getType(), is(Type.MATCH_LINE));
    assertThat(question.getLineNumber(), is(5));

    question = SearchFiltersQuestion.builder().setAction("permit").build();
    assertThat(question.getType(), is(Type.PERMIT));
    assertThat(question.getLineNumber(), nullValue());

    exception.expect(BatfishException.class);
    exception.expectMessage("Unrecognized query: foo");
    SearchFiltersQuestion.builder().setAction("foo").build();
  }

  @Test
  public void testIpProtocols() throws IOException {
    ImmutableSortedSet<IpProtocol> ipProtocols =
        ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.ICMP);
    SearchFiltersQuestion question =
        SearchFiltersQuestion.builder()
            .setHeaders(PacketHeaderConstraints.builder().setIpProtocols(ipProtocols).build())
            .build();

    assertThat(question.getHeaderSpace().getIpProtocols(), equalTo(ipProtocols));

    // test (de)serialization
    question = BatfishObjectMapper.clone(question, SearchFiltersQuestion.class);
    assertThat(question.getHeaderSpace().getIpProtocols(), equalTo(ipProtocols));
  }
}
