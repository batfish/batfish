package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NodePropertySpecifierTest {

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
