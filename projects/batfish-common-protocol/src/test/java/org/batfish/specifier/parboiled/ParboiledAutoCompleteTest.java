package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.specifier.parboiled.Completion.Type;
import org.junit.Test;

public class ParboiledAutoCompleteTest {

  ParboiledAutoComplete _testPAC =
      new ParboiledAutoComplete(null, null, null, null, null, 0, null, null, null);

  @Test
  public void testAutoCompletePartialMatchStringLiteral() {
    PartialMatch pm = new PartialMatch(Type.STRING_LITERAL, "pfx", "comp");
    assertThat(
        _testPAC.autoCompletePartialMatch(pm, 2),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("comp", true, null, -1, 2))));
  }

  @Test
  public void testAutoCompletePartialMatchSkipLabel() {
    PartialMatch pm = new PartialMatch(Type.IP_RANGE, "pfx", "comp");
    assertThat(_testPAC.autoCompletePartialMatch(pm, 2), equalTo(ImmutableList.of()));
  }
}
