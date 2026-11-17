package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.batfish.datamodel.applications.NamedApplication;
import org.batfish.datamodel.applications.TcpApplication;
import org.junit.jupiter.api.Test;

class TcpPortTest {
  @Test
  void getName() {
    assertThat(TcpPort.of(22).getName(), equalTo(Optional.of("SSH")));
    assertThat(TcpPort.of(21).getName(), equalTo(Optional.of("FTP-CONTROL")));
    assertThat(TcpPort.of(49152).getName(), equalTo(Optional.empty()));
  }

  @Test
  void namesDontHaveUnderscores() {
    for (int i = 1; i < 65535; ++i) {
      TcpPort p = TcpPort.of(i);
      p.getName().ifPresent(s -> assertThat(s, not(containsString("_"))));
    }
  }

  @Test
  void testNamedApplicationCoverage() {
    for (NamedApplication app : NamedApplication.values()) {
      if (!(app.getApplication() instanceof TcpApplication)) {
        continue;
      }
      TcpApplication tcpApp = (TcpApplication) app.getApplication();
      for (SubRange portRange : tcpApp.getPorts()) {
        for (int port = portRange.getStart(); port <= portRange.getEnd(); ++port) {
          assertTrue(TcpPort.of(port).getName().isPresent(), tcpApp.toString());
        }
      }
    }
  }
}
