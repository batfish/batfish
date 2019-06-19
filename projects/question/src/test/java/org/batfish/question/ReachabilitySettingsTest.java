package org.batfish.question;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.InterfaceSpecifierInterfaceLocationSpecifier;
import org.batfish.specifier.IntersectionLocationSpecifier;
import org.batfish.specifier.NameInterfaceSpecifier;
import org.batfish.specifier.NameNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierInterfaceLocationSpecifier;
import org.junit.Test;

public class ReachabilitySettingsTest {

  @Test
  public void testToReachabilityParameters_ingressNodes_ingressInterface() {
    NodeSpecifier nodeSpecifier = new NameNodeSpecifier("nodes");
    InterfaceSpecifier interfaceSpecifier = new NameInterfaceSpecifier("interfaces");
    ReachabilitySettings settings =
        ReachabilitySettings.builder()
            .setActions(ImmutableList.of())
            .setHeaderSpace(HeaderSpace.builder().build())
            .setIngressNodes(nodeSpecifier)
            .setIngressInterfaces(interfaceSpecifier)
            .build();
    ReachabilityParameters params = settings.toReachabilityParameters();
    assertThat(
        params.getSourceLocationSpecifier(),
        equalTo(
            new IntersectionLocationSpecifier(
                new InterfaceSpecifierInterfaceLocationSpecifier(interfaceSpecifier),
                new NodeSpecifierInterfaceLocationSpecifier(nodeSpecifier))));
  }
}
