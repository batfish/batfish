package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Test;

public class NodePropertySpecifierTest {

  @Test
  public void autoComplete() {
    // null or empty string should yield all options
    assertThat(
        NodePropertySpecifier.autoComplete(null)
            .stream()
            .map(s -> s.getText())
            .collect(Collectors.toSet()),
        equalTo(NodePropertySpecifier.JAVA_MAP.keySet()));

    // the capital P shouldn't matter and this should autoComplete to two entries
    assertThat(
        NodePropertySpecifier.autoComplete("ntP"),
        equalTo(
            ImmutableList.of(
                new AutocompleteSuggestion("ntp-servers", false),
                new AutocompleteSuggestion("ntp-source-interface", false))));
  }
}
