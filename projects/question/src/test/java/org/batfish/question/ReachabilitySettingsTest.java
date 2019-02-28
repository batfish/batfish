package org.batfish.question;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.specifier.IntersectionLocationSpecifier;
import org.batfish.specifier.LocationSpecifiers;
import org.batfish.specifier.NameNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierInterfaceLocationSpecifier;
import org.junit.Test;

public class ReachabilitySettingsTest {

  @Test
  public void testToReachabilityParameters_ingressNodes_ingressInterface() {
    NodeSpecifier nodeSpecifier = new NameNodeSpecifier("nodes");
    InterfacesSpecifier interfacesSpecifier = new InterfacesSpecifier("interfaces");
    ReachabilitySettings settings =
        ReachabilitySettings.builder()
            .setActions(ImmutableList.of())
            .setHeaderSpace(HeaderSpace.builder().build())
            .setIngressNodes(nodeSpecifier)
            .setIngressInterfaces(interfacesSpecifier)
            .build();
    ReachabilityParameters params = settings.toReachabilityParameters();
    assertThat(
        params.getSourceLocationSpecifier(),
        equalTo(
            new IntersectionLocationSpecifier(
                LocationSpecifiers.from(interfacesSpecifier),
                new NodeSpecifierInterfaceLocationSpecifier(nodeSpecifier))));
  }
}
