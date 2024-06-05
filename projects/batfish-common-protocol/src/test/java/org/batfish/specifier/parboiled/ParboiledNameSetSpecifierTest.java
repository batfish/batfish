package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.HashMap;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Mlag;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link ParboiledNameSetSpecifier} */
public class ParboiledNameSetSpecifierTest {

  private MockSpecifierContext.Builder _ctxtB;
  private MockSpecifierContext _ctxt;

  @Before
  public void init() {
    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);

    String mlag1 = "mlag1";
    String mlag2 = "mlag2";
    String otherMlag = "other";

    node1.setMlags(
        ImmutableSortedMap.of(
            mlag1,
            Mlag.builder().setId(mlag1).build(),
            mlag2,
            Mlag.builder().setId(mlag2).build(),
            otherMlag,
            Mlag.builder().setId(otherMlag).build()));

    Map<String, Configuration> configs = new HashMap<>();
    configs.put(node1.getHostname(), node1);

    _ctxtB = MockSpecifierContext.builder().setConfigs(configs);
    _ctxt = _ctxtB.build();
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParboiledNameSetSpecifier(
                new SingletonNameSetAstNode("mlag1"), Grammar.MLAG_ID_SPECIFIER)
            .resolve(_ctxt),
        equalTo(ImmutableSet.of("mlag1")));
    // bad names lead to empty set
    assertThat(
        new ParboiledNameSetSpecifier(
                new SingletonNameSetAstNode("nono"), Grammar.MLAG_ID_SPECIFIER)
            .resolve(_ctxt),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveNameRegex() {
    assertThat(
        new ParboiledNameSetSpecifier(new RegexNameSetAstNode("mlag.*"), Grammar.MLAG_ID_SPECIFIER)
            .resolve(_ctxt),
        equalTo(ImmutableSet.of("mlag1", "mlag2")));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledNameSetSpecifier(
                new UnionNameSetAstNode(
                    new SingletonNameSetAstNode("mlag1"), new SingletonNameSetAstNode("mlag2")),
                Grammar.MLAG_ID_SPECIFIER)
            .resolve(_ctxt),
        equalTo(ImmutableSet.of("mlag1", "mlag2")));
  }
}
