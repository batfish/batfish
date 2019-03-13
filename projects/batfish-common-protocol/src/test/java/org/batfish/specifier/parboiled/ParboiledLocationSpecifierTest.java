package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Test;

public class ParboiledLocationSpecifierTest {

  private static final String _node1 = "node1";
  private static final String _node2 = "node2";
  private static final String _iface1 = "eth1";
  private static final String _iface2 = "eth2";
  private static final MockSpecifierContext _ctxt;

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname(_node1).build();
    nf.interfaceBuilder().setOwner(n1).setName(_iface1).build();
    nf.interfaceBuilder().setOwner(n1).setName(_iface2).build();

    // make the address different because interface equality does not consider owners
    Configuration n2 = cb.setHostname(_node2).build();
    nf.interfaceBuilder()
        .setOwner(n2)
        .setName(_iface1)
        .setAddress(new InterfaceAddress("3.3.3.3/30"))
        .build();

    _ctxt =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of(_node1, n1, _node2, n2)).build();
  }

  @Test
  public void testResolveDifference() {
    assertThat(
        new ParboiledLocationSpecifier(
                new DifferenceLocationAstNode(
                    InterfaceLocationAstNode.createFromNode(_node1),
                    InterfaceLocationAstNode.createFromInterface(
                        new NameInterfaceAstNode(_iface1))))
            .resolve(_ctxt),
        equalTo(ImmutableSet.of(new InterfaceLocation(_node1, _iface2))));
  }

  @Test
  public void testResolveIntersection() {
    assertThat(
        new ParboiledLocationSpecifier(
                new IntersectionLocationAstNode(
                    InterfaceLocationAstNode.createFromNode(_node1),
                    InterfaceLocationAstNode.createFromInterface(
                        new NameInterfaceAstNode(_iface1))))
            .resolve(_ctxt),
        equalTo(ImmutableSet.of(new InterfaceLocation(_node1, _iface1))));
  }

  @Test
  public void testResolveInterface() {
    assertThat(
        new ParboiledLocationSpecifier(
                InterfaceLocationAstNode.createFromInterface(new NameInterfaceAstNode(_iface1)))
            .resolve(_ctxt),
        equalTo(
            ImmutableSet.of(
                new InterfaceLocation(_node1, _iface1), new InterfaceLocation(_node2, _iface1))));
  }

  @Test
  public void testResolveNode() {
    assertThat(
        new ParboiledLocationSpecifier(InterfaceLocationAstNode.createFromNode(_node1))
            .resolve(_ctxt),
        equalTo(
            ImmutableSet.of(
                new InterfaceLocation(_node1, _iface1), new InterfaceLocation(_node1, _iface2))));
  }

  @Test
  public void testResolveNodeInterface() {
    assertThat(
        new ParboiledLocationSpecifier(
                InterfaceLocationAstNode.createFromInterfaceWithNode(
                    new NameNodeAstNode(_node1), new NameInterfaceAstNode(_iface1)))
            .resolve(_ctxt),
        equalTo(ImmutableSet.of(new InterfaceLocation(_node1, _iface1))));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledLocationSpecifier(
                new UnionLocationAstNode(
                    InterfaceLocationAstNode.createFromNode(_node1),
                    InterfaceLocationAstNode.createFromInterface(
                        new NameInterfaceAstNode(_iface1))))
            .resolve(_ctxt),
        equalTo(
            ImmutableSet.of(
                new InterfaceLocation(_node1, _iface1),
                new InterfaceLocation(_node1, _iface2),
                new InterfaceLocation(_node2, _iface1))));
  }
}
