package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.FabricLink;
import org.junit.Test;

/** Tests for parser behavior and fabric-link sidecar parsing in {@link AciConfiguration}. */
public final class AciParserTest {

  @Test
  public void testFromJson_polUniRoot() throws IOException {
    String json = "{\"polUni\":{\"attributes\":{\"name\":\"aci-json-root\"},\"children\":[]}}";
    AciConfiguration config = AciConfiguration.fromJson("apic.json", json, new Warnings());
    assertThat(config.getHostname(), equalTo("aci-json-root"));
  }

  @Test
  public void testFromJson_bareRoot() throws IOException {
    String json = "{\"attributes\":{\"name\":\"aci-bare-root\"},\"children\":[]}";
    AciConfiguration config = AciConfiguration.fromJson("apic.json", json, new Warnings());
    assertThat(config.getHostname(), equalTo("aci-bare-root"));
  }

  @Test
  public void testFromFile_jsonDetectionWithLeadingWhitespace() throws IOException {
    String json = "  \n\t {\"polUni\":{\"attributes\":{\"name\":\"aci-detect\"},\"children\":[]}}";
    AciConfiguration config = AciConfiguration.fromFile("apic.json", json, new Warnings());
    assertThat(config.getHostname(), equalTo("aci-detect"));
  }

  @Test
  public void testFromFile_emptyThrows() {
    IOException e =
        assertThrows(
            IOException.class,
            () -> AciConfiguration.fromFile("empty.json", " \n\t  ", new Warnings()));
    assertThat(e.getMessage(), equalTo("Empty configuration file: empty.json"));
  }

  @Test
  public void testFromFile_unrecognizedFirstCharThrows() {
    IOException e =
        assertThrows(
            IOException.class,
            () -> AciConfiguration.fromFile("bad.txt", "not-json-or-xml", new Warnings()));
    assertTrue(e.getMessage().contains("file starts with 'n': bad.txt"));
  }

  @Test
  public void testIsFabricLinksJson_true() {
    String json =
        "{\"imdata\":[{\"fabricLink\":{\"attributes\":{"
            + "\"n1\":\"101\",\"n2\":\"201\",\"p1\":\"3\",\"p2\":\"49\""
            + "}}}]}";
    assertTrue(AciConfiguration.isFabricLinksJson(json));
  }

  @Test
  public void testIsFabricLinksJson_falseWhenImdataMissing() {
    String json = "{\"polUni\":{\"attributes\":{\"name\":\"x\"},\"children\":[]}}";
    assertFalse(AciConfiguration.isFabricLinksJson(json));
  }

  @Test
  public void testIsFabricLinksJson_falseWhenMixedEntries() {
    String json =
        "{\"imdata\":["
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"1\",\"n2\":\"2\",\"p1\":\"1\",\"p2\":\"2\"}}},"
            + "{\"notFabricLink\":{\"attributes\":{}}}]}";
    assertFalse(AciConfiguration.isFabricLinksJson(json));
  }

  @Test
  public void testParseFabricLinksJson_defaultsMissingSlotsToOne() throws IOException {
    String json =
        "{\"imdata\":[{\"fabricLink\":{\"attributes\":{"
            + "\"n1\":\"101\",\"n2\":\"201\",\"p1\":\"3\",\"p2\":\"49\""
            + "}}}]}";

    List<FabricLink> links = AciConfiguration.parseFabricLinksJson("fabric_links.json", json);
    assertThat(links, hasSize(1));
    assertThat(links.get(0).getNode1Interface(), equalTo("Ethernet1/3"));
    assertThat(links.get(0).getNode2Interface(), equalTo("Ethernet1/49"));
  }

  @Test
  public void testParseFabricLinksJson_ignoresIncompleteEntries() throws IOException {
    String json =
        "{\"imdata\":["
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"101\",\"n2\":\"201\",\"p1\":\"3\"}}},"
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"101\",\"n2\":\"202\",\"p2\":\"7\"}}},"
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"101\",\"n2\":\"203\",\"p1\":\"8\",\"p2\":\"9\"}}}"
            + "]}";

    List<FabricLink> links = AciConfiguration.parseFabricLinksJson("fabric_links.json", json);
    assertThat(links, hasSize(1));
    assertThat(links.get(0).getNode2Id(), equalTo("203"));
    assertThat(links.get(0).getNode1Interface(), equalTo("Ethernet1/8"));
    assertThat(links.get(0).getNode2Interface(), equalTo("Ethernet1/9"));
  }

  @Test
  public void testParseFabricLinksJson_preservesLinkState() throws IOException {
    String json =
        "{\"imdata\":[{\"fabricLink\":{\"attributes\":{"
            + "\"n1\":\"101\",\"n2\":\"201\",\"s1\":\"2\",\"s2\":\"3\",\"p1\":\"5\",\"p2\":\"6\","
            + "\"linkState\":\"down\""
            + "}}}]}";

    List<FabricLink> links = AciConfiguration.parseFabricLinksJson("fabric_links.json", json);
    assertThat(links, hasSize(1));
    assertThat(links.get(0).getLinkState(), equalTo("down"));
    assertThat(links.get(0).getNode1Interface(), equalTo("Ethernet2/5"));
    assertThat(links.get(0).getNode2Interface(), equalTo("Ethernet3/6"));
  }

  @Test
  public void testParseFabricLinksJson_throwsWhenNotFabricPayload() {
    IOException e =
        assertThrows(
            IOException.class,
            () -> AciConfiguration.parseFabricLinksJson("bad.json", "{\"notImdata\":[]}"));
    assertThat(e.getMessage(), equalTo("Not a fabricLink JSON payload: bad.json"));
  }
}
