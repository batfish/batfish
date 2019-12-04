package org.batfish.representation.aws;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import java.io.IOException;
import org.batfish.common.Warning;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.junit.Test;

/** Tests for {@link Region} */
public class RegionTest {

  /** Test that we don't warn the user upon encountering an empty list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyEmptyList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  [] }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    assertTrue(pvcae.getWarnings().isEmpty());
  }

  /** Test that we warn the user upon encountering a non-empty list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyNonEmptyList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  [1] }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    Warning warning =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(pvcae.getWarnings().values()).getUnimplementedWarnings());
    assertTrue(warning.getText().startsWith("Unrecognized element"));
  }

  /** Test that we warn the user upon encountering a non-list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyNonList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  {} }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    Warning warning =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(pvcae.getWarnings().values()).getUnimplementedWarnings());
    assertTrue(warning.getText().startsWith("Unrecognized element"));
  }
}
