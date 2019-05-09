package org.batfish.grammar;

import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.datamodel.ConfigurationFormat.CADANT;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS_XR;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_NX;
import static org.batfish.datamodel.ConfigurationFormat.F5_BIGIP_STRUCTURED;
import static org.batfish.datamodel.ConfigurationFormat.FLAT_JUNIPER;
import static org.batfish.datamodel.ConfigurationFormat.JUNIPER;
import static org.batfish.datamodel.ConfigurationFormat.JUNIPER_SWITCH;
import static org.batfish.datamodel.ConfigurationFormat.PALO_ALTO;
import static org.batfish.datamodel.ConfigurationFormat.PALO_ALTO_NESTED;
import static org.batfish.datamodel.ConfigurationFormat.UNKNOWN;
import static org.batfish.grammar.VendorConfigurationFormatDetector.identifyConfigurationFormat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link VendorConfigurationFormatDetector}. */
@RunWith(JUnit4.class)
public class VendorConfigurationFormatDetectorTest {
  @Test
  public void testArista() {
    String eosFlash = "! boot system flash:/vEOS-lab.swi\n";
    String aristaRancid = "!RANCID-CONTENT-TYPE: arista\n";

    for (String fileText : ImmutableList.of(eosFlash, aristaRancid)) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(ARISTA));
    }
  }

  @Test
  public void testCadant() {
    String fileText =
        "# ChassisType=<E6000> shelfName=<Arris CER CMTS> shelfSwVersion=<CER_V03.05.02.0008> \n"
            + "configure\n"
            + "shelfname \"Arris CER CMTS\"\n";
    assertThat(identifyConfigurationFormat(fileText), equalTo(CADANT));
  }

  @Test
  public void testF5BigipStructured() {
    String withRancid = "#RANCID-CONTENT-TYPE: bigip\n";
    String withoutRancid = "#TMSH-VERSION: 1.0\nsys global-settings { }\n";

    assertThat(identifyConfigurationFormat(withRancid), equalTo(F5_BIGIP_STRUCTURED));
    assertThat(identifyConfigurationFormat(withoutRancid), equalTo(F5_BIGIP_STRUCTURED));
  }

  @Test
  public void testIos() {
    String asr1000 =
        "boot system flash bootflash:asr1000rp1-adventerprisek9.03.03.01.S.151-2.S1.bin \n";
    String catalyst =
        "boot system flash bootflash:cat4500e-universalk9.SPA.03.05.03.E.152-1.E3.bin \n";

    for (String fileText : ImmutableList.of(asr1000, catalyst)) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_IOS));
    }
  }

  @Test
  public void testIosXr() {
    String xr = "!! IOS XR Configuration 5.2.4\n";
    String xrRancid = "!RANCID-CONTENT-TYPE: cisco\n" + xr;

    for (String fileText : ImmutableList.of(xr, xrRancid)) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_IOS_XR));
    }
  }

  @Test
  public void testJuniper() {

    /* Confirm hierarchical configs are correctly identified */
    for (String fileText :
        ImmutableList.of(
            "firewall {\n}\n",
            "policy-options {\n}\n",
            "!RANCID-CONTENT-TYPE: juniper\n!\nsomething {\n blah;\n}\n",
            "#RANCID-CONTENT-TYPE: juniper\n!\nsomething {\n blah;\n}\n",
            "#RANCID-CONTENT-TYPE: juniper-srx\n!\nsomething {\n blah;\n}\n",
            "snmp {\n}\n")) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(JUNIPER));
    }

    /* Confirm flat (set-style) configs are correctly identified */
    for (String fileText :
        ImmutableList.of(
            "#\nset system host-name blah",
            "!RANCID-CONTENT-TYPE: juniper\n!\nset blah\n",
            "#RANCID-CONTENT-TYPE: juniper\n!\nset blah\n",
            "#RANCID-CONTENT-TYPE: juniper-srx\n!\nset blah\n",
            "#\nset apply-groups blah\n",
            "####BATFISH FLATTENED JUNIPER CONFIG####\n")) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(FLAT_JUNIPER));
    }

    /* Confirm Juniper switch format is detected */
    assertThat(identifyConfigurationFormat("set hostname\n"), equalTo(JUNIPER_SWITCH));
  }

  @Test
  public void testNxos() {
    String n7000 = "boot system bootflash:n7000-s2-dk9.7.2.1.D1.1.bin sup-2 \n";
    String nxos = "boot nxos bootflash:nxos.7.0.3.I4.7.bin \n";

    for (String fileText : ImmutableList.of(n7000, nxos)) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_NX));
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
      assertThat(identifyConfigurationFormat(fileText), equalTo(PALO_ALTO_NESTED));
    }

    /* Confirm flat (set-style) PAN configs are correctly identified */
    for (String fileText :
        ImmutableList.of(flatRancid, flatPanorama, flatSendPanorama, flatDeviceConfig, flattened)) {
      assertThat(identifyConfigurationFormat(fileText), equalTo(PALO_ALTO));
    }
  }

  @Test
  public void testUnknown() {
    String unknownConfig = "unknown config line\n";

    /* Make sure bogus config is not misidentified */
    assertThat(identifyConfigurationFormat(unknownConfig), equalTo(UNKNOWN));
  }
}
