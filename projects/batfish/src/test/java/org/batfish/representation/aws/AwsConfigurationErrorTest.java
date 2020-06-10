package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.junit.Test;

public class AwsConfigurationErrorTest {

  /** Test that conversion doesn't return empty topology if one of the regions fails to convert */
  @Test
  public void toVendorIndependentConfigurations() {
    Warnings w = new Warnings(true, true, true);
    AwsConfiguration awsConfiguration = new AwsConfiguration();
    awsConfiguration.setWarnings(w);

    Account account = awsConfiguration.addOrGetAccount("account1");
    account
        .addOrGetRegion("region1")
        .getVpcs()
        .put("vpc1", new Vpc("vpc1", ImmutableSet.of(), ImmutableMap.of()));

    Region r2_mock = mock(Region.class);
    when(r2_mock.getName()).thenReturn("region2");
    doThrow(new NullPointerException()).when(r2_mock).toConfigurationNodes(any(), same(w));
    account.addRegion(r2_mock);

    List<Configuration> c = awsConfiguration.toVendorIndependentConfigurations();

    assertThat(c, contains(hasHostname("vpc1")));
    assertThat(
        w.getRedFlagWarnings().get(0).getText(),
        allOf(
            containsString("Failed conversion for"),
            containsString(NullPointerException.class.getSimpleName())));
  }
}
