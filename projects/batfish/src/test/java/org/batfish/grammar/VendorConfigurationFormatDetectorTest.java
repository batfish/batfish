package org.batfish.grammar;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
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

  @Test
  public void recognizeIos() {
    String asr1000 =
        "boot system flash bootflash:asr1000rp1-adventerprisek9.03.03.01.S.151-2.S1.bin \n";
    String catalyst =
        "boot system flash bootflash:cat4500e-universalk9.SPA.03.05.03.E.152-1.E3.bin \n";

    for (String fileText : ImmutableList.of(asr1000, catalyst)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.CISCO_IOS));
    }
  }

  @Test
  public void recognizeIosXr() {
    String xr = "!! IOS XR Configuration 5.2.4\n";
    String xrRancid = "!RANCID-CONTENT-TYPE: cisco\n" + xr;

    for (String fileText : ImmutableList.of(xr, xrRancid)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.CISCO_IOS_XR));
    }
  }

  @Test
  public void recognizeNxos() {
    String n7000 = "boot system bootflash:n7000-s2-dk9.7.2.1.D1.1.bin sup-2 \n";
    String nxos = "boot nxos bootflash:nxos.7.0.3.I4.7.bin \n";

    for (String fileText : ImmutableList.of(n7000, nxos)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.CISCO_NX));
    }
  }

  @Test
  public void recognizePaloAlto() {
    String rancid = "!RANCID-CONTENT-TYPE: paloalto\n!\n";
    String panorama = "deviceconfig {\n  system {\n    panorama-server 1.2.3.4;\n  }\n}";
    String sendPanorama = "alarm {\n  informational {\n    send-to-panorama yes;\n  }\n}";

    for (String fileText : ImmutableList.of(rancid, panorama, sendPanorama)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.PALO_ALTO));
    }
  }
}
