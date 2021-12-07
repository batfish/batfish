package org.batfish.question;

import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.question.VIModelQuestionPlugin.VIModelAnswerer.getConfigs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.question.VIModelQuestionPlugin.VIModelQuestion;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Test;

public class VIModelQuestionPluginTest {

  @Test
  public void testGetConfigs() {
    Configuration c1 =
        Configuration.builder().setConfigurationFormat(ARISTA).setHostname("c1").build();
    Configuration c2 =
        Configuration.builder().setConfigurationFormat(ARISTA).setHostname("c2").build();
    MockSpecifierContext context =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2))
            .build();
    assertThat(
        getConfigs(new VIModelQuestion(null), context),
        equalTo(ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2)));
    assertThat(
        getConfigs(new VIModelQuestion(c1.getHostname()), context),
        equalTo(ImmutableSortedMap.of(c1.getHostname(), c1)));
  }
}
