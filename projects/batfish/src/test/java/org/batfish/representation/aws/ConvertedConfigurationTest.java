package org.batfish.representation.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.junit.Test;

public class ConvertedConfigurationTest {
  @Test
  public void testGetNodeCaseInsensitive() {
    Configuration cfg = new Configuration("aBcDeFg", ConfigurationFormat.CISCO_IOS);
    ConvertedConfiguration cvtCfg = new ConvertedConfiguration(ImmutableList.of(cfg));
    assertThat(cvtCfg.getNode("AbCdefg"), equalTo(cfg));
  }
}
