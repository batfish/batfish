package org.batfish.minesweeper.question.searchroutepolicies;

import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SearchRoutePoliciesQuestionTest {

  @Rule public ExpectedException _exception = ExpectedException.none();

  @Test
  public void testDenyWithOutputConstraints() {
    _exception.expect(IllegalArgumentException.class);
    new SearchRoutePoliciesQuestion(
        Environment.Direction.IN,
        SearchRoutePoliciesQuestion.DEFAULT_ROUTE_CONSTRAINTS,
        BgpRouteConstraints.builder().setMed(LongSpace.of(3)).build(),
        null,
        null,
        SearchRoutePoliciesQuestion.Action.DENY);
  }
}
