package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Environment}. */
@RunWith(JUnit4.class)
public class EnvironmentTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorAndGetter() {
    Set<String> nodeBlacklist = Sets.newHashSet("node1");
    Map<String, String> bgpTables = Collections.singletonMap("bgpTable1", "table1Content");
    Map<String, String> routingTables = Collections.singletonMap("routingTable1", "table1Content");
    Environment e =
        new Environment(
            "environment",
            "testrig",
            Sets.newHashSet(),
            Sets.newHashSet(),
            nodeBlacklist,
            bgpTables,
            routingTables,
            "announcement");
    assertThat(e.getEnvName(), equalTo("environment"));
    assertThat(e.getTestrigName(), equalTo("testrig"));
    assertThat(e.getEdgeBlacklist(), equalTo(Sets.newHashSet()));
    assertThat(e.getInterfaceBlacklist(), equalTo(Sets.newHashSet()));
    assertThat(e.getNodeBlacklist(), equalTo(nodeBlacklist));
    assertThat(e.getBgpTables(), equalTo(bgpTables));
    assertThat(e.getRoutingTables(), equalTo(routingTables));
    assertThat(e.getExternalBgpAnnouncements(), equalTo("announcement"));
  }

  @Test
  public void testToString() {
    Environment e =
        new Environment(
            "environment",
            "testrig",
            Sets.newHashSet(),
            Sets.newHashSet(),
            Sets.newHashSet(),
            Maps.newHashMap(),
            Maps.newHashMap(),
            "announcement");
    assertThat(
        e.toString(),
        equalTo(
            "Environment{envName=environment, testrigName=testrig, "
                + "edgeBlacklist=[], interfaceBlacklist=[], "
                + "nodeBlacklist=[], bgpTables={}, routingTables={}, "
                + "externalBgpAnnouncements=announcement}"));
  }
}
