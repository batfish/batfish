package org.batfish.dataplane;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.dataplane.ibdp.IncrementalDataPlanePlugin;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GetRoutesTest {

  private NetworkFactory _nf;

  private Vrf.Builder _vb;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{"bdp"}, {"ibdp"}});
  }

  @Parameter public String dpEngine;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _vb = _nf.vrfBuilder();
  }

  @Test
  public void testRoutesOutputHasAllVrfs() throws IOException {
    Configuration n1 =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();

    Vrf emptyVrf = _vb.setOwner(n1).setName("empty").build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(n1.getHostname(), n1), _folder);
    batfish.getSettings().setDataplaneEngineName(IncrementalDataPlanePlugin.PLUGIN_NAME);
    DataPlanePlugin dataPlanePlugin = batfish.getDataPlanePlugin();
    ComputeDataPlaneResult dp = dataPlanePlugin.computeDataPlane();

    SortedMap<String, SortedMap<String, SortedSet<AnnotatedRoute<AbstractRoute>>>> routes =
        dataPlanePlugin.getRoutes(dp._dataPlane);

    assertThat(routes, hasKey(n1.getHostname()));
    // Empty VRF is there
    assertThat(routes.get(n1.getHostname()), hasKey(emptyVrf.getName()));
    assertThat(routes.get(n1.getHostname()).get(emptyVrf.getName()), empty());
  }
}
