package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;
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
    Date now = new Date();
    Environment e = new Environment("environment", now, 0, 0, 0, 0, 0,
            "announcement");
    assertThat(e.getName(), equalTo("environment"));
    assertThat(e.getCreatedAt(), equalTo(now));
    assertThat(e.getExternalBgpAnnouncements(), equalTo("announcement"));
  }

  @Test
  public void testToString() {
    Date now = new Date();
    Environment e = new Environment("environment", now, 0, 0, 0, 0, 0,
            "announcement");
    assertThat(
        e.toString(),
        equalTo(String.format(
            "Environment{name=environment, createdAt=%s, edgeBlacklistCount=0, "
                + "interfaceBlacklistCount=0, nodeBlacklistCount=0, bgpTablesCount=0, "
                + "routingTablesCount=0, externalBgpAnnouncements=announcement}",
            now)));
  }
}
