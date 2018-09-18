package org.batfish.question.testfilters;

import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Before;
import org.junit.Test;

public class TestFiltersAnswererTest {

  private NetworkFactory _nf = new NetworkFactory();
  private Configuration.Builder _cb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
  }

  private static final class MockBatfish extends IBatfishTestAdapter {

    SortedMap<String, Configuration> __configurations;

    public MockBatfish(SortedMap<String, Configuration> configurations) {
      __configurations = configurations;
    }

    @Override
    public BatfishLogger getLogger() {
      return null;
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations() {
      return __configurations;
    }

    @Override
    public SpecifierContext specifierContext() {
      return MockSpecifierContext.builder().setConfigs(loadConfigurations()).build();
    }
  }

  @Test
  public void testOneRowPerAclPerConfig() throws IOException {
    IpAccessList.Builder aclb = _nf.aclBuilder();
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Configuration c3 = _cb.build();

    // Create 2 ACLs for each of 3 configs.
    aclb.setOwner(c1).build();
    aclb.build();
    aclb.setOwner(c2).build();
    aclb.build();
    aclb.setOwner(c3).build();
    aclb.build();

    IBatfish batfish =
        new MockBatfish(
            ImmutableSortedMap.of(
                c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3));
    TestFiltersQuestion question = new TestFiltersQuestion(null, null, null, null);
    TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer();

    // There should be 6 rows
    assertThat(answer.getRows(), hasSize(6));
  }
}
