package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableMultimap;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.junit.Test;

public class ParseVendorConfigurationJobTest {
  private static final String HOST_TESTCONFIGS_PREFIX = "org/batfish/grammar/host/testconfigs/";

  private static ParseVendorConfigurationResult parseHost(String resourcePath) throws Exception {
    return new ParseVendorConfigurationJob(
            new Settings(),
            CommonUtil.readResource(resourcePath),
            "filename",
            new Warnings(),
            ConfigurationFormat.HOST,
            ImmutableMultimap.of())
        .call();
  }

  @Test
  public void testHost() throws Exception {
    ParseVendorConfigurationResult result = parseHost(HOST_TESTCONFIGS_PREFIX + "host.json");
    // Confirm a good host file results in no failure cause
    assertThat(result.getFailureCause(), equalTo(null));
  }

  @Test
  public void testHostInvalid() throws Exception {
    ParseVendorConfigurationResult result = parseHost(HOST_TESTCONFIGS_PREFIX + "hostInvalid.json");
    // Confirm a bad host file does not cause a crash but results in failure cause
    assertThat(result.getFailureCause(), not(equalTo(null)));
  }
}
