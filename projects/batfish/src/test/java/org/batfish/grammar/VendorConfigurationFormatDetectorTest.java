package org.batfish.grammar;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.ConfigurationFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VendorConfigurationFormatDetectorTest {
  @Test
  public void recognizeCadant() {
    String fileText =
        "# ChassisType=<E6000> shelfName=<Arris CER CMTS> shelfSwVersion=<CER_V03.05.02.0008> \n"
            + "configure\n"
            + "shelfname \"Arris CER CMTS\"\n";
    assertThat(
        VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
        equalTo(ConfigurationFormat.CADANT));
  }
}
