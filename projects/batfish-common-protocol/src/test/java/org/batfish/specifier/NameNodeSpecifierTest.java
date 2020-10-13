package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.junit.Test;

public class NameNodeSpecifierTest {

  @Test
  public void testResolve() {
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(
                ImmutableMap.of(
                    "node1",
                    new Configuration("node1", ConfigurationFormat.CISCO_IOS),
                    "node2",
                    new Configuration("node2", ConfigurationFormat.CISCO_IOS)))
            .build();

    assertThat(new NameNodeSpecifier("node1").resolve(ctxt), equalTo(ImmutableSet.of("node1")));
    assertThat(new NameNodeSpecifier("node").resolve(ctxt), equalTo(ImmutableSet.of()));
    assertThat(new NameNodeSpecifier("NODE1").resolve(ctxt), equalTo(ImmutableSet.of("node1")));
  }
}
