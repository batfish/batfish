package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.batfish.datamodel.applications.NamedApplication;
import org.batfish.datamodel.applications.UdpApplication;
import org.junit.Test;

public class UdpPortTest {
  @Test
  public void getName() {
    assertThat(UdpPort.of(53).getName(), equalTo(Optional.of("DNS")));
    assertThat(UdpPort.of(49152).getName(), equalTo(Optional.empty()));
  }

  @Test
  public void namesDontHaveUnderscores() {
    for (int i = 1; i < 65535; ++i) {
      UdpPort p = UdpPort.of(i);
      p.getName().ifPresent(s -> assertThat(s, not(containsString("_"))));
    }
  }

  @Test
  public void testNamedApplicationCoverage() {
    for (NamedApplication app : NamedApplication.values()) {
      if (!(app.getApplication() instanceof UdpApplication)) {
        continue;
      }
      UdpApplication udpApp = (UdpApplication) app.getApplication();
      for (SubRange portRange : udpApp.getPorts()) {
        for (int port = portRange.getStart(); port <= portRange.getEnd(); ++port) {
          assertTrue(udpApp.toString(), UdpPort.of(port).getName().isPresent());
        }
      }
    }
  }
}
