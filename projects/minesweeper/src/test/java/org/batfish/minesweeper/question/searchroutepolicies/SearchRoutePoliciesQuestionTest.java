package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.DEFAULT_PATH_OPTION;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.jupiter.api.Test;

class SearchRoutePoliciesQuestionTest {

  @Test
  void testDenyWithOutputConstraints() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new SearchRoutePoliciesQuestion(
              Environment.Direction.IN,
              SearchRoutePoliciesQuestion.DEFAULT_ROUTE_CONSTRAINTS,
              BgpRouteConstraints.builder().setMed(LongSpace.of(3)).build(),
              null,
              null,
              LineAction.DENY,
              DEFAULT_PATH_OPTION);
        });
  }
}
