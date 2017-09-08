package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link CreateEnvironmentRequest}. */
@RunWith(JUnit4.class)
public class CreateEnvironmentRequestTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorAndGetter() {
    List<String> nodeBlacklist = Lists.newArrayList("node1");
    List<FileObject> bgpTables = Lists.newArrayList(new FileObject("bgpTable", "tableContent"));
    List<FileObject> routingTables =
        Lists.newArrayList(new FileObject("routingTable", "tableContent"));
    CreateEnvironmentRequest e =
        new CreateEnvironmentRequest(
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
    CreateEnvironmentRequest e =
        new CreateEnvironmentRequest(
            "environment",
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            "announcement");
    assertThat(
        e.toString(),
        equalTo(
            "CreateEnvironmentRequest{name=environment, edgeBlacklist=[], interfaceBlacklist=[], "
                + "nodeBlacklist=[], bgpTables=[], routingTables=[], "
                + "externalBgpAnnouncements=announcement}"));
  }
}
