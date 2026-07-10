package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.CommonParser.SET_OP_DIFFERENCE;
import static org.batfish.specifier.parse.CommonParser.SET_OP_INTERSECTION;
import static org.batfish.specifier.parse.CommonParser.SET_OP_UNION;
import static org.batfish.specifier.parse.ParsedAutoCompleteSuggestion.SET_PREFIX_DIFFERENCE;
import static org.batfish.specifier.parse.ParsedAutoCompleteSuggestion.SET_PREFIX_INTERSECTION;
import static org.batfish.specifier.parse.ParsedAutoCompleteSuggestion.SET_PREFIX_UNION;
import static org.batfish.specifier.parse.ParsedAutoCompleteSuggestion.completeDescriptionIfNeeded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.specifier.parse.Anchor.Type;
import org.junit.Test;

/** Tests for {@link ParsedAutoCompleteSuggestion} */
public class ParsedAutoCompleteSuggestionTest {

  @Test
  public void testCompleteDescriptionIfNeededNonSet() {
    assertThat(
        completeDescriptionIfNeeded(new ParsedAutoCompleteSuggestion(",", 0, Type.IP_ADDRESS)),
        equalTo(Type.IP_ADDRESS.getDescription()));
  }

  @Test
  public void testCompleteDescriptionIfNeededSetDifference() {
    assertThat(
        completeDescriptionIfNeeded(
            new ParsedAutoCompleteSuggestion(SET_OP_DIFFERENCE, 0, Type.NODE_SET_OP)),
        equalTo(SET_PREFIX_DIFFERENCE + Type.NODE_SET_OP.getDescription()));
  }

  @Test
  public void testCompleteDescriptionIfNeededSetIntersection() {
    assertThat(
        completeDescriptionIfNeeded(
            new ParsedAutoCompleteSuggestion(SET_OP_INTERSECTION, 0, Type.NODE_SET_OP)),
        equalTo(SET_PREFIX_INTERSECTION + Type.NODE_SET_OP.getDescription()));
  }

  @Test
  public void testCompleteDescriptionIfNeededSetUnion() {
    assertThat(
        completeDescriptionIfNeeded(
            new ParsedAutoCompleteSuggestion(SET_OP_UNION, 0, Type.NODE_SET_OP)),
        equalTo(SET_PREFIX_UNION + Type.NODE_SET_OP.getDescription()));
  }

  @Test
  public void testCompleteDescriptionIfNeededSetUnknownOp() {
    assertThat(
        // '.' is not a set operator
        completeDescriptionIfNeeded(new ParsedAutoCompleteSuggestion(".", 0, Type.NODE_SET_OP)),
        equalTo(Type.NODE_SET_OP.getDescription()));
  }
}
