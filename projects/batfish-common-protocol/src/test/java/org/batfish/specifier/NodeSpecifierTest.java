package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;
import org.junit.Test;

public class NodeSpecifierTest {
  private static final Map<String, Configuration> _configs;
  private static final SpecifierContext _context;
  private static final String _roleDim;
  private static final Configuration _roleNode;
  private static final Pattern _rolePattern;

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.build();
    Configuration n2 = cb.build();
    Configuration n3 = cb.build();

    _configs = ImmutableMap.of(n1.getHostname(), n1, n2.getHostname(), n2, n3.getHostname(), n3);

    // create a role that matches just n1
    String roleName = "testRole";
    _roleDim = "roleDim";
    _roleNode = n1;
    _rolePattern = Pattern.compile(roleName);
    _context =
        MockSpecifierContext.builder()
            .setConfigs(_configs)
            .setNodeRoleDimensions(
                ImmutableSet.of(
                    NodeRoleDimension.builder()
                        .setName(_roleDim)
                        .setRoleDimensionMappings(
                            ImmutableList.of(
                                new RoleDimensionMapping(
                                    "(" + n1.getHostname() + ")",
                                    null,
                                    ImmutableMap.of(n1.getHostname(), roleName))))
                        .build()))
            .build();
  }

  @Test
  public void testAllNodesNodeSpecifier() {
    assertThat(AllNodesNodeSpecifier.INSTANCE.resolve(_context), equalTo(_configs.keySet()));
  }

  @Test
  public void testDifferenceNodeSpecifier() {
    String excludedNodeName = _roleNode.getHostname();
    assertThat(
        new DifferenceNodeSpecifier(
                AllNodesNodeSpecifier.INSTANCE,
                new NameRegexNodeSpecifier(Pattern.compile(excludedNodeName)))
            .resolve(_context),
        equalTo(Sets.difference(_configs.keySet(), ImmutableSet.of(excludedNodeName))));
  }

  @Test
  public void testNameRegexNodeSpecifier() {
    String name = _roleNode.getHostname();
    Pattern pattern = Pattern.compile(name);
    assertThat(
        new NameRegexNodeSpecifier(pattern).resolve(_context), equalTo(ImmutableSet.of(name)));
  }

  @Test
  public void testRoleRegexNodeSpecifier() {
    assertThat(
        new RoleRegexNodeSpecifier(_rolePattern, _roleDim).resolve(_context),
        equalTo(ImmutableSet.of(_roleNode.getHostname())));
  }
}
