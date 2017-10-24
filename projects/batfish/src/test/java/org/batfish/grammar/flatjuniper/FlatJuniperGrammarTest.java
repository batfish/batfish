package org.batfish.grammar.flatjuniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link FlatJuniperParser}flat Junpier parser and {@link
 * FlatJuniperControlPlaneExtractor}.
 */
public class FlatJuniperGrammarTest {

  private static String TESTRIGS_PREFIX = "org/batfish/grammar/juniper/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBgpMultipathMultipleAs() throws IOException {
    String testrigName = "multipath-multiple-as";
    String[] configurationNames =
        new String[] {"multiple_as_disabled", "multiple_as_enabled", "multiple_as_mixed"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName, configurationNames, null, null, null, null, _folder);
    SortedMap<String, Configuration> configurations = batfish.loadConfigurations();
    MultipathEquivalentAsPathMatchMode multipleAsDisabled =
        configurations
            .get("multiple_as_disabled")
            .getDefaultVrf()
            .getBgpProcess()
            .getMultipathEquivalentAsPathMatchMode();
    MultipathEquivalentAsPathMatchMode multipleAsEnabled =
        configurations
            .get("multiple_as_enabled")
            .getDefaultVrf()
            .getBgpProcess()
            .getMultipathEquivalentAsPathMatchMode();
    MultipathEquivalentAsPathMatchMode multipleAsMixed =
        configurations
            .get("multiple_as_mixed")
            .getDefaultVrf()
            .getBgpProcess()
            .getMultipathEquivalentAsPathMatchMode();

    assertThat(multipleAsDisabled, equalTo(MultipathEquivalentAsPathMatchMode.FIRST_AS));
    assertThat(multipleAsEnabled, equalTo(MultipathEquivalentAsPathMatchMode.PATH_LENGTH));
    assertThat(multipleAsMixed, equalTo(MultipathEquivalentAsPathMatchMode.FIRST_AS));
  }
}
