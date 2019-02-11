package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledAutoComplete.autoCompletePartialMatch;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Test;

public class ParboiledAutoCompleteTest {

  @Test
  public void testAutoCompletePartialMatchStringLiteral() {
    PartialMatch pm = new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "pfx", "comp");
    assertThat(
        autoCompletePartialMatch(pm, 2, Integer.MAX_VALUE, null, null, null),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("comp", true, null, -1, 2))));
  }

  @Test
  public void testAutoCompletePartialMatchUnknownLabel() {
    PartialMatch pm = new PartialMatch("Unknown", "pfx", "comp");
    assertThat(
        autoCompletePartialMatch(pm, 2, Integer.MAX_VALUE, null, null, null),
        equalTo(ImmutableList.of()));
  }
}
