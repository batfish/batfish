package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.apic.AciPolUniInternal;

/** Parser utilities for Cisco ACI APIC exports and optional fabric-link payloads. */
public final class AciParser {

  private AciParser() {}

  /**
   * Creates an {@link AciConfiguration} from JSON text.
   *
   * <p>Parses the native ACI JSON structure with {@code polUni} as root and extracts tenants, VRFs,
   * bridge domains, EPGs, contracts, and fabric nodes from nested children arrays.
   */
  public static AciConfiguration fromJson(String filename, String text, Warnings warnings)
      throws IOException {
    JsonNode rootNode = BatfishObjectMapper.mapper().readTree(text);
    JsonNode polUniNode = rootNode.has("polUni") ? rootNode.get("polUni") : rootNode;
    AciPolUniInternal polUni =
        BatfishObjectMapper.mapper().treeToValue(polUniNode, AciPolUniInternal.class);
    return buildConfiguration(filename, polUni, warnings);
  }

  /**
   * Creates an {@link AciConfiguration} from XML text.
   *
   * <p>Parses the native ACI XML structure with {@code polUni} as root and extracts tenants, VRFs,
   * bridge domains, EPGs, contracts, and fabric nodes from nested children arrays.
   */
  public static AciConfiguration fromXml(String filename, String text, Warnings warnings)
      throws IOException {
    AciPolUniInternal polUni =
        BatfishObjectMapper.xmlMapper().readValue(text, AciPolUniInternal.class);
    return buildConfiguration(filename, polUni, warnings);
  }

  /**
   * Creates an {@link AciConfiguration} from file content with automatic format detection.
   *
   * <p>Detects JSON or XML based on the first non-whitespace character.
   */
  public static AciConfiguration fromFile(String filename, String text, Warnings warnings)
      throws IOException {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      throw new IOException("Empty configuration file: " + filename);
    }

    char firstChar = trimmed.charAt(0);
    if (firstChar == '{') {
      return fromJson(filename, text, warnings);
    } else if (firstChar == '<') {
      return fromXml(filename, text, warnings);
    } else {
      throw new IOException(
          "Unrecognized configuration format. Expected JSON (starting with '{') or "
              + "XML (starting with '<'), but file starts with '"
              + firstChar
              + "': "
              + filename);
    }
  }

  /** Returns {@code true} iff the provided JSON text looks like a fabricLink export. */
  public static boolean isFabricLinksJson(String text) {
    try {
      JsonNode rootNode = BatfishObjectMapper.mapper().readTree(text);
      JsonNode imdata = rootNode.get("imdata");
      if (imdata == null || !imdata.isArray() || imdata.isEmpty()) {
        return false;
      }
      for (JsonNode item : imdata) {
        if (!item.has("fabricLink")) {
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Parses APIC fabricLink export JSON into explicit Layer1 link records.
   *
   * <p>Expected format is the common APIC response shape with top-level {@code imdata[]} containing
   * {@code fabricLink} objects.
   */
  public static @Nonnull List<FabricLink> parseFabricLinksJson(String filename, String text)
      throws IOException {
    JsonNode rootNode = BatfishObjectMapper.mapper().readTree(text);
    JsonNode imdata = rootNode.get("imdata");
    if (imdata == null || !imdata.isArray()) {
      throw new IOException("Not a fabricLink JSON payload: " + filename);
    }
    List<FabricLink> links = new ArrayList<>();
    for (JsonNode item : imdata) {
      JsonNode fabricLinkNode = item.get("fabricLink");
      if (fabricLinkNode == null || !fabricLinkNode.isObject()) {
        continue;
      }
      JsonNode attrs = fabricLinkNode.get("attributes");
      if (attrs == null || !attrs.isObject()) {
        continue;
      }
      String node1Id = textOrNull(attrs.get("n1"));
      String node2Id = textOrNull(attrs.get("n2"));
      String slot1 = textOrNull(attrs.get("s1"));
      String slot2 = textOrNull(attrs.get("s2"));
      String port1 = textOrNull(attrs.get("p1"));
      String port2 = textOrNull(attrs.get("p2"));
      if (node1Id == null || node2Id == null || port1 == null || port2 == null) {
        continue;
      }
      FabricLink link =
          new FabricLink(
              node1Id, toAciInterfaceName(slot1, port1), node2Id, toAciInterfaceName(slot2, port2));
      link.setLinkState(textOrNull(attrs.get("linkState")));
      links.add(link);
    }
    return links;
  }

  private static AciConfiguration buildConfiguration(
      String filename, AciPolUniInternal polUni, Warnings warnings) {
    AciConfiguration aciConfiguration = new AciConfiguration();
    aciConfiguration.setWarnings(warnings);
    aciConfiguration.setFilename(filename);
    aciConfiguration.setHostname(extractHostname(polUni, filename));
    aciConfiguration.parsePolUni(polUni, warnings);
    aciConfiguration.finalizeStructures();
    return aciConfiguration;
  }

  /** Extracts hostname from polUni attributes; falls back to filename-derived hostname. */
  private static @Nonnull String extractHostname(AciPolUniInternal polUni, String filename) {
    if (polUni.getAttributes() != null && polUni.getAttributes().getName() != null) {
      return polUni.getAttributes().getName();
    }
    String basename = filename.substring(filename.lastIndexOf('/') + 1);
    if (basename.contains(".")) {
      basename = basename.substring(0, basename.lastIndexOf('.'));
    }
    return "aci-" + basename;
  }

  private static @Nullable String textOrNull(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    String text = node.asText();
    return text.isEmpty() ? null : text;
  }

  private static @Nonnull String toAciInterfaceName(@Nullable String slot, String port) {
    String effectiveSlot = slot == null || slot.isEmpty() ? "1" : slot;
    return String.format("Ethernet%s/%s", effectiveSlot, port);
  }
}
