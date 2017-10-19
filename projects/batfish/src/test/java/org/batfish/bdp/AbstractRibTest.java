package org.batfish.bdp;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterableOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the AbstractRib tree logic. To avoid worrying about route preference comparisons, testing
 * with StaticRoutes and StaticRib. All static routes will be stored in the RIB, without eviction
 * based on preference.
 */
public class AbstractRibTest {
  private AbstractRib<StaticRoute> _rib;
  private StaticRoute _mostGeneralRoute;

  @Before
  public void setupEmptyRib() {
    _rib = new StaticRib(null);
    _mostGeneralRoute = new StaticRoute(new Prefix("0.0.0.0/0"), Ip.ZERO, null, 0, 0);
  }

  @Test
  public void testRibConstructor() {
    // Test: the setupEmptyRib() fixture

    // Assertions: Ensure that a new rib is empty upon construction
    assertThat(_rib.getRoutes(), is(emptyIterableOf(StaticRoute.class)));
    assertThat(_rib.containsRoute(_mostGeneralRoute), is(false));
  }

  @Test
  public void testSingleRouteAdd() {
    // Test: Adding one route
    _rib.mergeRoute(_mostGeneralRoute);

    // Assertions
    // Check routes size
    assertThat(_rib.getRoutes().size(), is(1));
    // Check that containsRoute works as expected for this simple case
    assertThat(_rib.containsRoute(_mostGeneralRoute), is(true));
    assertThat(
        _rib.containsRoute(new StaticRoute(new Prefix("1.1.1.1/32"), Ip.ZERO, null, 0, 0)),
        is(false));
  }

  /** Inserts overlapping routes into the RIB and returns a (manually) ordered list of them */
  private List<StaticRoute> setupOverlappingRoutes() {
    // Setup helper
    String[] testPrefixes =
        new String[] {"10.0.0.0/8", "10.0.0.0/9", "10.128.0.0/9", "10.1.1.1/32"};
    List<StaticRoute> routes = new ArrayList<>();

    // Test: merge the routes into the RIB
    for (String prefixStr : testPrefixes) {
      StaticRoute r = new StaticRoute(new Prefix(prefixStr), Ip.ZERO, null, 0, 0);
      _rib.mergeRoute(r);
      routes.add(r);
    }
    return routes;
  }

  @Test
  public void testRepeatedAdd() {
    // Setup
    StaticRoute route = new StaticRoute(new Prefix("10.0.0.0/11"), Ip.ZERO, null, 0, 0);

    // Test: add the same route multiple times
    for (int i = 0; i < 5; i++) {
      _rib.mergeRoute(route);
    }

    // Assertions
    // Ensure only one route is stored
    assertThat(_rib.getRoutes().size(), is(1));
    // Check that containsRoute works as expected for this simple case
    assertThat(_rib.containsRoute(route), is(true));
  }

  @Test
  public void testNonOverlappingRouteAdd() {
    // Setup
    StaticRoute r1 = new StaticRoute(new Prefix("1.1.1.1/32"), Ip.ZERO, null, 0, 0);
    StaticRoute r2 = new StaticRoute(new Prefix("128.1.1.1/32"), Ip.ZERO, null, 0, 0);

    // Test:
    _rib.mergeRoute(r1);
    _rib.mergeRoute(r2);

    // Assertions
    // Check that both routes exist
    Set<StaticRoute> collectedRoutes = _rib.getRoutes();
    assertThat(collectedRoutes.size(), is(2));
    assertThat(_rib.containsRoute(r1), is(true));
    assertThat(_rib.containsRoute(r2), is(true));
    // Also check route collection via getRoutes()
    assertThat(collectedRoutes, hasItem(r1));
    assertThat(collectedRoutes, hasItem(r2));
  }

  @Test
  public void testMultiOverlappingRouteAdd() {
    // Setup/Test: Add multiple routes with overlapping prefixes
    List<StaticRoute> routes = setupOverlappingRoutes();

    // Assertions
    // Check number of routes
    Set<StaticRoute> collectedRoutes = _rib.getRoutes();
    assertThat(collectedRoutes.size(), is(routes.size()));
    // Check that contains returns true for all routes
    assertThat(_rib.containsRoute(_mostGeneralRoute), is(false));
    for (StaticRoute r : routes) {
      assertThat(_rib.containsRoute(r), is(true));
      // And check getRoutes() collection
      assertThat(collectedRoutes, hasItem(r));
    }
  }

  @Test
  public void testLongestPrefixMatchWhenEmpty() {
    // Setup/Test: the setupEmptyRib() fixture

    // Assertions
    // Check that for empty rib prefix match is empty when the RIB is empty
    assertThat(_rib.longestPrefixMatch(new Ip("1.1.1.1")), is(emptyIterableOf(StaticRoute.class)));
  }

  @Test
  public void testLongestPrefixMatch() {
    // Setup/Test:
    List<StaticRoute> routes = setupOverlappingRoutes();

    // Assertions
    // Check that longestPrefixMatch executes correctly
    Set<StaticRoute> match = _rib.longestPrefixMatch(new Ip("10.1.1.1"));
    assertThat(match.size(), equalTo(1));
    assertThat(match, contains(routes.get(3)));

    match = _rib.longestPrefixMatch(new Ip("10.1.1.2"));
    assertThat(match.size(), equalTo(1));
    assertThat(match, contains(routes.get(1)));
  }
}
