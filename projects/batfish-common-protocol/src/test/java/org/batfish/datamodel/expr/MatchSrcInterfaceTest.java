package org.batfish.datamodel.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.junit.Test;

public class MatchSrcInterfaceTest {
  private static Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSrcInterface() {
    // Test source interface matching

    Set<String> interfaceNames = new HashSet<>();
    interfaceNames.add("test");
    MatchSrcInterface exprSrcInterface = new MatchSrcInterface(interfaceNames);

    // Confirm the interface matching boolean expression matches the same interface name
    assertThat(exprSrcInterface.match(createFlow(), "test", new HashSet<>()), equalTo(true));

    // Confirm the interface matching boolean expression does not match a different interface name
    assertThat(exprSrcInterface.match(createFlow(), "fail", new HashSet<>()), equalTo(false));
  }
}
