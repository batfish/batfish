package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Test;

public class InterfacePropertySpecifierTest {

  @Test
  public void autoComplete() {
    // null or empty string should yield all options
    assertThat(
        InterfacePropertySpecifier.autoComplete(null)
            .stream()
            .map(s -> s.getText())
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.builder()
                .addAll(InterfacePropertySpecifier.JAVA_MAP.keySet())
                .add(".*")
                .build()));

    // the capital S shouldn't matter and this should autoComplete to two entries
    assertThat(
        InterfacePropertySpecifier.autoComplete("deSc").stream().collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(".*desc.*", true),
                new AutocompleteSuggestion("description", false))));
  }

  @Test
  public void getMatchingProperties() {
    // match everything
    assertThat(
        new InterfacePropertySpecifier(".*").getMatchingProperties().size(),
        equalTo(InterfacePropertySpecifier.JAVA_MAP.size()));

    // match the description
    assertThat(new InterfacePropertySpecifier("desc.*").getMatchingProperties().size(), equalTo(1));

    // match nothing: ntp
    assertTrue(new InterfacePropertySpecifier("ntp").getMatchingProperties().isEmpty());
  }
}
