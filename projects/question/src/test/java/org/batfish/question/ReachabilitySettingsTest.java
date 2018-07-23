package org.batfish.question;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.specifier.IntersectionLocationSpecifier;
import org.batfish.specifier.LocationSpecifiers;
import org.junit.Test;

public class ReachabilitySettingsTest {

  @Test
  public void testToReachabilityParameters_ingressNodes_ingressInterface() {
    NodesSpecifier nodesSpecifier = new NodesSpecifier("nodes");
    InterfacesSpecifier interfacesSpecifier = new InterfacesSpecifier("interfaces");
    ReachabilitySettings settings =
        ReachabilitySettings.builder()
            .setActions(ImmutableList.of())
            .setHeaderSpace(HeaderSpace.builder().build())
            .setIngressNodes(nodesSpecifier)
            .setIngressInterfaces(interfacesSpecifier)
            .build();
    ReachabilityParameters params = settings.toReachabilityParameters();
    assertThat(
        params.getSourceLocationSpecifier(),
        equalTo(
            new IntersectionLocationSpecifier(
                LocationSpecifiers.from(interfacesSpecifier),
                LocationSpecifiers.from(nodesSpecifier))));
  }
}
