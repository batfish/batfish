package org.batfish.question.reachfilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.question.reachfilter.ReachFilterQuestion.Type;
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

    assertThat(q.getFilterSpecifierInput(), notNullValue());
    assertThat(q.getType(), is(Type.PERMIT));
    assertThat(q.getNodesSpecifier(), notNullValue());
  }

  @Test
  public void testSetQuery() {
    ReachFilterQuestion question = new ReachFilterQuestion();
    assertThat(question.getType(), is(Type.PERMIT));
    assertThat(question.getLineNumber(), nullValue());

    question.setQuery("deny");
    assertThat(question.getType(), is(Type.DENY));
    assertThat(question.getLineNumber(), nullValue());

    question.setQuery("matchLine 5");
    assertThat(question.getType(), is(Type.MATCH_LINE));
    assertThat(question.getLineNumber(), is(5));

    question.setQuery("permit");
    assertThat(question.getType(), is(Type.PERMIT));
    assertThat(question.getLineNumber(), nullValue());

    exception.expect(BatfishException.class);
    exception.expectMessage("Unrecognized query: foo");
    question.setQuery("foo");
  }
}
