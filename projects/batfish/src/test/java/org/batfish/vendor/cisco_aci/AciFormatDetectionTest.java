package org.batfish.vendor.cisco_aci;

import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_ACI;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.ConfigurationFormat.EMPTY;
import static org.batfish.datamodel.ConfigurationFormat.UNKNOWN;
import static org.batfish.grammar.VendorConfigurationFormatDetector.identifyConfigurationFormat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests of ACI format detection in {@link org.batfish.grammar.VendorConfigurationFormatDetector}.
 */
@RunWith(JUnit4.class)
public class AciFormatDetectionTest {

  @Test
  public void testAciJson_polUni() {
    String fileText =
        """
        {
          "polUni": {
            "attributes": {},
            "children": []
          }
        }
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciJson_fvTenant() {
    String fileText =
        """
        {
          "fvTenant": {
            "attributes": {
              "name": "tenant1"
            }
          }
        }
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciJson_fabricNode() {
    String fileText =
        """
        {
          "fabricNode": {
            "attributes": {
              "id": "1",
              "name": "spine1"
            }
          }
        }
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciJson_completeConfig() {
    String fileText =
        """
        {
          "polUni": {
            "attributes": {},
            "children": [
              {
                "fvTenant": {
                  "attributes": {"name": "tenant1"},
                  "children": []
                }
              }
            ]
          }
        }
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciXml_polUni() {
    String fileText =
        """
        <?xml version="1.0"?>
        <polUni>
          <children>
          </children>
        </polUni>
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciXml_fvTenant() {
    String fileText =
        """
        <?xml version="1.0"?>
        <fvTenant name="tenant1">
          <children>
          </children>
        </fvTenant>
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciXml_fabricNode() {
    String fileText =
        """
        <?xml version="1.0"?>
        <fabricNode id="1" name="spine1"/>
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciXml_completeConfig() {
    String fileText =
        """
        <?xml version="1.0"?>
        <polUni>
          <children>
            <fvTenant name="tenant1">
              <children>
                <fvCtx name="ctx1"/>
              </children>
            </fvTenant>
            <fabricNode id="1" name="spine1" role="spine"/>
          </children>
        </polUni>
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testAciBatfishFormat() {
    String fileText = "!BATFISH_FORMAT: cisco_aci\n";
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_ACI));
  }

  @Test
  public void testCiscoIos_notAci() {
    String fileText =
        """
        !
        version 16.9.4
        hostname ios-switch
        interface GigabitEthernet0/0
         description Uplink
         no shutdown
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(CISCO_IOS));
  }

  @Test
  public void testArista_notAci() {
    String fileText =
        """
        ! device: Arista (EOS-4.28.1F)
        hostname arista-switch
        interface Ethernet1
           description Uplink
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(ARISTA));
  }

  @Test
  public void testEmptyFile() {
    String fileText = "";
    assertThat(identifyConfigurationFormat(fileText), equalTo(EMPTY));
  }

  @Test
  public void testWhitespaceOnlyFile() {
    String fileText = "   \n  \t  \n";
    assertThat(identifyConfigurationFormat(fileText), equalTo(EMPTY));
  }

  @Test
  public void testUnknownFormat_notAci() {
    String fileText =
        """
        # This is not an ACI config
        random content here
        more random content
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(UNKNOWN));
  }

  @Test
  public void testPartialMatch_polUniSubstring() {
    // Should not match "polUni" as a substring of a different word
    String fileText =
        """
        {
          "myPolUniField": "value",
          "someOtherField": "value2"
        }
        """;
    // The pattern looks for quoted "polUni", so this should NOT match
    assertThat(identifyConfigurationFormat(fileText), equalTo(UNKNOWN));
  }

  @Test
  public void testJsonWithoutAciMarkers() {
    // Valid JSON but without ACI-specific markers
    String fileText =
        """
        {
          "hostname": "some-device",
          "interfaces": [
            {
              "name": "eth0",
              "enabled": true
            }
          ]
        }
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(UNKNOWN));
  }

  @Test
  public void testXmlWithoutAciMarkers() {
    // Valid XML but without ACI-specific markers
    String fileText =
        """
        <?xml version="1.0"?>
        <config>
          <hostname>some-device</hostname>
          <interfaces>
            <interface name="eth0" enabled="true"/>
          </interfaces>
        </config>
        """;
    assertThat(identifyConfigurationFormat(fileText), equalTo(UNKNOWN));
  }
}
