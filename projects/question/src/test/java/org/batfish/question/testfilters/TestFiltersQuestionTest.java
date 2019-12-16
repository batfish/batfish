package org.batfish.question.testfilters;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestFiltersQuestionTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  /**
   * Check that if a simple string is the specifier input, that maps to the expected
   * FilterSpecifier. (That this happens is being assumed by SearchFiltersAnswerer, which it ideally
   * shouldn't but in the meanwhile this test helps.)
   */
  @Test
  public void testDefaultSpecifierInput() {
    TestFiltersQuestion question = new TestFiltersQuestion(null, "filter1", null, null);

    IpAccessList filter1 =
        IpAccessList.builder()
            .setName("filter1")
            .setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL))
            .build();

    IpAccessList filter2 =
        IpAccessList.builder()
            .setName("filter2")
            .setLines(ImmutableList.of(ExprAclLine.REJECT_ALL))
            .build();

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder().setHostname("node");
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.build();

    n1.getIpAccessLists()
        .putAll(ImmutableMap.of(filter1.getName(), filter1, filter2.getName(), filter2));

    SpecifierContext ctxt =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of("node", n1)).build();

    assertThat(
        question.getFilterSpecifier().resolve("node", ctxt), equalTo(ImmutableSet.of(filter1)));
  }

  @Test
  public void testDeserializationDefaultValues() throws IOException {
    String serialized =
        String.format("{\"class\":\"%s\"}", TestFiltersQuestion.class.getCanonicalName());
    TestFiltersQuestion q =
        BatfishObjectMapper.mapper().readValue(serialized, TestFiltersQuestion.class);

    assertThat(q.getFilterSpecifier(), notNullValue());
    assertThat(q.getNodeSpecifier(), notNullValue());
  }
}
