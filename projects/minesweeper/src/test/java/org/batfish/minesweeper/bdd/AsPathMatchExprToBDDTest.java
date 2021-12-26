package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link org.batfish.minesweeper.bdd.AsPathMatchExprToBDD}. */
public class AsPathMatchExprToBDDTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private AsPathMatchExprToBDD _matchExprToBDD;

  private static final String ASPATH1 = " 40$";
  private static final String ASPATH2 = "^$";
  private BDD _asPath1BDD;
  private BDD _asPath2BDD;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    IBatfish batfish =
        new TransferBDDTest.MockBatfish(ImmutableSortedMap.of(HOSTNAME, _baseConfig));

    Graph g =
        new Graph(
            batfish, batfish.getSnapshot(), null, null, null, ImmutableSet.of(ASPATH1, ASPATH2));
    TransferBDD transferBDD = new TransferBDD(g, _baseConfig, ImmutableList.of());
    BDDRoute bddRoute = new BDDRoute(transferBDD.getFactory(), g);
    _arg = new CommunitySetMatchExprToBDD.Arg(transferBDD, bddRoute);
    _matchExprToBDD = new AsPathMatchExprToBDD();

    Map<SymbolicAsPathRegex, Set<Integer>> regexMap =
        g.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    BDD[] asPathAPs = bddRoute.getAsPathRegexAtomicPredicates();
    _asPath1BDD = asPathAPs[regexMap.get(new SymbolicAsPathRegex(ASPATH1)).iterator().next()];
    _asPath2BDD = asPathAPs[regexMap.get(new SymbolicAsPathRegex(ASPATH2)).iterator().next()];
  }

  @Test
  public void testVisitAsPathMatchAny() {
    assertTrue(AsPathMatchAny.of(ImmutableList.of()).accept(_matchExprToBDD, _arg).isZero());

    BDD result =
        AsPathMatchAny.of(
                ImmutableList.of(AsPathMatchRegex.of(ASPATH1), AsPathMatchRegex.of(ASPATH2)))
            .accept(_matchExprToBDD, _arg);
    assertEquals(_asPath1BDD.or(_asPath2BDD), result);
  }

  @Test
  public void testAsPathMatchExprReference() {
    String name = "name";

    _baseConfig.setAsPathMatchExprs(ImmutableMap.of(name, AsPathMatchRegex.of(ASPATH1)));

    AsPathMatchExprReference reference = AsPathMatchExprReference.of(name);

    assertEquals(_asPath1BDD, reference.accept(_matchExprToBDD, _arg));
  }

  @Test
  public void testAsPathMatchRegex() {
    assertEquals(_asPath1BDD, AsPathMatchRegex.of(ASPATH1).accept(_matchExprToBDD, _arg));
  }
}
