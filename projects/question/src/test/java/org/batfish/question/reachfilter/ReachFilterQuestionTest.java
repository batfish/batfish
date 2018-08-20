package org.batfish.question.reachfilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.question.ReachFilterParameters;
import org.batfish.question.reachfilter.ReachFilterQuestion.Type;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.FlexibleUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReachFilterQuestionTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testDeserializationDefaultValues() throws IOException {
    String serialized =
        String.format("{\"class\":\"%s\"}", ReachFilterQuestion.class.getCanonicalName());
    ReachFilterQuestion q =
        BatfishObjectMapper.mapper().readValue(serialized, ReachFilterQuestion.class);

    assertThat(q.getFilterSpecifier(), notNullValue());
    assertThat(q.getType(), is(Type.PERMIT));
    assertThat(q.getNodesSpecifier(), notNullValue());
    assertThat(q.getDataPlane(), equalTo(false));
    assertThat(q.getNodeSpecifierFactory(), equalTo(FlexibleNodeSpecifierFactory.NAME));
    assertThat(q.getNodeSpecifierInput(), nullValue());
    assertThat(
        q.getDestinationIpSpaceSpecifierFactory(),
        equalTo(FlexibleUniverseIpSpaceSpecifierFactory.NAME));
    assertThat(
        q.getSourceIpSpaceSpecifierFactory(),
        equalTo(FlexibleUniverseIpSpaceSpecifierFactory.NAME));
    assertThat(q.getDestinationIpSpaceSpecifierInput(), nullValue());
    assertThat(q.getSourceIpSpaceSpecifierInput(), nullValue());
    // src/dst IPs NOT stored in headerspace at this stage
    assertThat(q.getHeaderSpace().getDstIps(), nullValue());
    assertThat(q.getHeaderSpace().getSrcIps(), nullValue());
    // src/dst IPs are in specifiers at this stage
    ReachFilterParameters parameters = q.toReachFilterParameters();

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
    ReachFilterQuestion question = new ReachFilterQuestion();
    assertThat(question.getType(), is(Type.PERMIT));
    assertThat(question.getLineNumber(), nullValue());

    question = ReachFilterQuestion.builder().setQuery("deny").build();
    assertThat(question.getType(), is(Type.DENY));
    assertThat(question.getLineNumber(), nullValue());

    question = ReachFilterQuestion.builder().setQuery("matchLine 5").build();
    assertThat(question.getType(), is(Type.MATCH_LINE));
    assertThat(question.getLineNumber(), is(5));

    question = ReachFilterQuestion.builder().setQuery("permit").build();
    assertThat(question.getType(), is(Type.PERMIT));
    assertThat(question.getLineNumber(), nullValue());

    exception.expect(BatfishException.class);
    exception.expectMessage("Unrecognized query: foo");
    ReachFilterQuestion.builder().setQuery("foo").build();
  }
}
