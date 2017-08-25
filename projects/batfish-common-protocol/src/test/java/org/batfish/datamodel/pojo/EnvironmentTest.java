package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    List<String> nodeBlacklist = Lists.newArrayList("node1");
    Map<String, String> bgpTables = Collections.singletonMap("bgpTable1", "table1Content");
    Map<String, String> routingTables = Collections.singletonMap("routingTable1", "table1Content");
    Environment e =
        new Environment(
            "environment",
            Lists.newArrayList(),
            Lists.newArrayList(),
            nodeBlacklist,
            bgpTables,
            routingTables,
            "announcement");
    assertThat(e.getName(), equalTo("environment"));
    assertThat(e.getEdgeBlacklist(), equalTo(Lists.newArrayList()));
    assertThat(e.getInterfaceBlacklist(), equalTo(Lists.newArrayList()));
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
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            Maps.newHashMap(),
            Maps.newHashMap(),
            "announcement");
    assertThat(
        e.toString(),
        equalTo(
            "Environment{name=environment, edgeBlacklist=[], interfaceBlacklist=[], "
                + "nodeBlacklist=[], bgpTables={}, routingTables={}, "
                + "externalBgpAnnouncements=announcement}"));
  }
}
