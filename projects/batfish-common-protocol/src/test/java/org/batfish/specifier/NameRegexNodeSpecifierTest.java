package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.junit.Test;

public class NameRegexNodeSpecifierTest {

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

    assertThat(
        new NameRegexNodeSpecifier(Pattern.compile("node1")).resolve(ctxt),
        equalTo(ImmutableSet.of("node1")));
    assertThat(
        new NameRegexNodeSpecifier(Pattern.compile("node")).resolve(ctxt),
        equalTo(ImmutableSet.of()));
  }
}
