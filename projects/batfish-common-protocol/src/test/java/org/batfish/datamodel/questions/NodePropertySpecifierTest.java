package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Collectors;
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
        equalTo(
            new ImmutableSet.Builder<String>()
                .addAll(NodePropertySpecifier.JAVA_MAP.keySet())
                .add(".*")
                .build()));

    // the capital P shouldn't matter and this should autoComplete to three entries
    assertThat(
        NodePropertySpecifier.autoComplete("ntP")
            .stream()
            .map(s -> s.getText())
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("ntp.*", "ntp-servers", "ntp-source-interface")));
  }

  @Test
  public void getMatchingProperties() {
    // match everything
    assertThat(
        new NodePropertySpecifier(".*").getMatchingProperties().size(),
        equalTo(NodePropertySpecifier.JAVA_MAP.size()));

    // match the ntp properties
    assertThat(new NodePropertySpecifier("ntp.*").getMatchingProperties().size(), equalTo(2));

    // match nothing: ntp
    assertTrue(new NodePropertySpecifier("ntp").getMatchingProperties().isEmpty());
  }
}
