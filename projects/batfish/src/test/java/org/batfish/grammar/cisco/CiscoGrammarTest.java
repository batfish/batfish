package org.batfish.grammar.cisco;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for Cisco parser and CiscoControlPlaneExtractor. */
public class CiscoGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco/testconfigs/";
  
  @Test
  public void testAaaNewmodel() throws IOException {
    String configurationName = "aaaNewmodel";
    SortedMap<String, String> configurationText = new TreeMap<>();
    String aaaNewmodelConfigurationText = CommonUtil.readResource(TESTCONFIGS_PREFIX + configurationName);
    configurationText.put(configurationName, aaaNewmodelConfigurationText);
    Batfish batfish = BatfishTestUtils.getBatfishFromConfigurationText(configurationText, _folder);
    SortedMap<String, Configuration> configurations = batfish.loadConfigurations();
    Configuration configuration = configurations.get(configurationName);
    boolean aaaNewmodel = configuration.getVendorFamily().getCisco().getAaa().getNewModel();
    assertThat(aaaNewmodel, equalTo(false));
  }


}
