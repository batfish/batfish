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
  public void testCadant() {
    String fileText =
        "# ChassisType=<E6000> shelfName=<Arris CER CMTS> shelfSwVersion=<CER_V03.05.02.0008> \n"
            + "configure\n"
            + "shelfname \"Arris CER CMTS\"\n";
    assertThat(
        VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
        equalTo(ConfigurationFormat.CADANT));
  }

  @Test
  public void testIos() {
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
  public void testIosXr() {
    String xr = "!! IOS XR Configuration 5.2.4\n";
    String xrRancid = "!RANCID-CONTENT-TYPE: cisco\n" + xr;

    for (String fileText : ImmutableList.of(xr, xrRancid)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.CISCO_IOS_XR));
    }
  }

  @Test
  public void testJuniper() {
    String firewall = "firewall {\n}\n";
    String policyOptions = "policy-options {\n}\n";
    String rancid = "!RANCID-CONTENT-TYPE: juniper\n!\nsomething {\n blah;\n}\n";
    String snmp = "snmp {\n}\n";

    String flatHostname = "#\nset system host-name blah";
    String flatRancid = "!RANCID-CONTENT-TYPE: juniper\n!\nset blah\n";
    String flatSet = "#\nset apply-groups blah\n";
    String flattened = "####BATFISH FLATTENED JUNIPER CONFIG####\n";

    String flatSwitch = "set hostname\n";

    /* Confirm hierarchical configs are correctly identified */
    for (String fileText : ImmutableList.of(firewall, policyOptions, rancid, snmp)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.JUNIPER));
    }

    /* Confirm flat (set-style) configs are correctly identified */
    for (String fileText : ImmutableList.of(flatHostname, flatRancid, flatSet, flattened)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.FLAT_JUNIPER));
    }

    /* Confirm Juniper switch format is detected */
    assertThat(
        VendorConfigurationFormatDetector.identifyConfigurationFormat(flatSwitch),
        equalTo(ConfigurationFormat.JUNIPER_SWITCH));
  }

  @Test
  public void testNxos() {
    String n7000 = "boot system bootflash:n7000-s2-dk9.7.2.1.D1.1.bin sup-2 \n";
    String nxos = "boot nxos bootflash:nxos.7.0.3.I4.7.bin \n";

    for (String fileText : ImmutableList.of(n7000, nxos)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.CISCO_NX));
    }
  }

  @Test
  public void testPaloAlto() {
    String rancid = "!RANCID-CONTENT-TYPE: paloalto\n!\nstructure {";
    String panorama = "deviceconfig {\n  system {\n    panorama-server 1.2.3.4;\n  }\n}";
    String sendPanorama = "alarm {\n  informational {\n    send-to-panorama yes;\n  }\n}";
    String deviceConfig = "deviceconfig {\n  system {\n    blah;\n  }\n}";

    String flatRancid = "!RANCID-CONTENT-TYPE: paloalto\n!\n";
    String flatPanorama = "set deviceconfig system panorama-server 1.2.3.4\n}";
    String flatSendPanorama = "set alarm informational send-to-panorama yes\n";
    String flatDeviceConfig = "set deviceconfig system blah\n";
    String flattened = "####BATFISH FLATTENED PALO ALTO CONFIG####\n";

    /* Confirm hierarchical PAN configs are correctly identified */
    for (String fileText : ImmutableList.of(rancid, panorama, sendPanorama, deviceConfig)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.PALO_ALTO_NESTED));
    }

    /* Confirm flat (set-style) PAN configs are correctly identified */
    for (String fileText :
        ImmutableList.of(flatRancid, flatPanorama, flatSendPanorama, flatDeviceConfig, flattened)) {
      assertThat(
          VendorConfigurationFormatDetector.identifyConfigurationFormat(fileText),
          equalTo(ConfigurationFormat.PALO_ALTO));
    }
  }

  @Test
  public void testUnknown() {
    String unknownConfig = "unknown config line\n";

    /* Make sure bogus config is not misidentified */
    assertThat(
        VendorConfigurationFormatDetector.identifyConfigurationFormat(unknownConfig),
        equalTo(ConfigurationFormat.UNKNOWN));
  }
}
