package org.batfish.grammar.cisco;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link CiscoParser}Cisco parser and {@link CiscoControlPlaneExtractor}. */
public class CiscoGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco/testconfigs/";

  private static String TESTRIGS_PREFIX = "org/batfish/grammar/cisco/testrigs/";

  @Test
  public void testAaaNewmodel() throws IOException {
    SortedMap<String, String> configurationText = new TreeMap<>();
    String configurationName = "aaaNoNewmodel";
    String aaaNoNewmodelConfigurationText =
        CommonUtil.readResource(TESTCONFIGS_PREFIX + configurationName);
    configurationText.put(configurationName, aaaNoNewmodelConfigurationText);
    configurationName = "aaaNewmodel";
    String aaaNewmodelConfigurationText =
        CommonUtil.readResource(TESTCONFIGS_PREFIX + configurationName);
    configurationText.put(configurationName, aaaNewmodelConfigurationText);
    Batfish batfish =
        BatfishTestUtils.getBatfishFromConfigurationText(
            configurationText, Collections.emptySortedMap(), Collections.emptySortedMap(), _folder);
    SortedMap<String, Configuration> configurations = batfish.loadConfigurations();
    Configuration newModelConfiguration = configurations.get("aaaNewmodel");
    boolean aaaNewmodel = newModelConfiguration.getVendorFamily().getCisco().getAaa().getNewModel();
    assertTrue(aaaNewmodel);
    Configuration noNewModelConfiguration = configurations.get("aaaNoNewmodel");
    aaaNewmodel = noNewModelConfiguration.getVendorFamily().getCisco().getAaa().getNewModel();
    assertFalse(aaaNewmodel);
  }

  @Test
  public void testBgpLocalAs() throws IOException {
    String testrigName = "bgp-local-as";
    String[] configurationNames = new String[] {"r1", "r2"};
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigResource(
            TESTRIGS_PREFIX + testrigName, configurationNames, _folder);
    SortedMap<String, Configuration> configurations = batfish.loadConfigurations();
    Map<Ip, Set<String>> ipOwners = batfish.computeIpOwners(configurations, true);
    batfish.initRemoteBgpNeighbors(configurations, ipOwners);
    Configuration r1 = configurations.get("r1");
    Configuration r2 = configurations.get("r2");
    assertThat(
        r1.getDefaultVrf()
            .getBgpProcess()
            .getNeighbors()
            .get(new Prefix("1.2.0.2/32"))
            .getRemoteBgpNeighbor(),
        is(notNullValue()));
    assertThat(
        r2.getDefaultVrf()
            .getBgpProcess()
            .getNeighbors()
            .get(new Prefix("1.2.0.1/32"))
            .getRemoteBgpNeighbor(),
        is(notNullValue()));
  }
}
