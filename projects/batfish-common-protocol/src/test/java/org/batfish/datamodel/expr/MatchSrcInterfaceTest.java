package org.batfish.datamodel.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.junit.Test;

public class MatchSrcInterfaceTest {
  private Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSrcInterfaceMatch() {
    Set<String> interfaceNames = new HashSet<>();
    interfaceNames.add("test");
    MatchSrcInterface msi = new MatchSrcInterface(interfaceNames);

    // Confirm the interface matching boolean expression matches the same interface name
    assertThat(msi.match(createFlow(), "test", new HashSet<>()), equalTo(true));
  }

  @Test
  public void testSrcInterfaceNoMatch() {
    Set<String> interfaceNames = new HashSet<>();
    interfaceNames.add("test");
    MatchSrcInterface msi = new MatchSrcInterface(interfaceNames);

    // Confirm the interface matching boolean expression matches the same interface name
    assertThat(msi.match(createFlow(), "fail", new HashSet<>()), equalTo(false));
  }
}
