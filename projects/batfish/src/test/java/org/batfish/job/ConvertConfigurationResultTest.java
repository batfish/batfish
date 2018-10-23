package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;

import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.junit.Test;

public class ConvertConfigurationResultTest {

  @Test
  public void testConvertConfigurationResultFileMap() {
    BatfishLogger logger = new BatfishLogger("INFO", false);
    Configuration config1 = new Configuration("aws", ConfigurationFormat.AWS);
    SortedMap<String, Configuration> configurations = new TreeMap<>();
    configurations.put("hostname1", config1);
    configurations.put("hostname2", config1);

    // Apply CCR to CCAE
    ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
    new ConvertConfigurationResult(
            0,
            logger.getHistory(),
            new TreeMap<>(),
            "aws",
            configurations,
            new ConvertConfigurationAnswerElement())
        .applyTo(new TreeMap<>(), logger, ccae);

    // Confirm both sub-configs show up in the resulting fileMap
    SortedMap<String, String> fileMap = ccae.getFileMap();
    assertThat(fileMap, aMapWithSize(2));
    assertThat(fileMap, hasEntry("hostname1", "aws"));
    assertThat(fileMap, hasEntry("hostname2", "aws"));
  }
}
