package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_AND_DIMENSION_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_DIMENSION_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.OPERATOR_END;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleDimensionMapping;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for node role name auto completion in {@link ParboiledAutoComplete}. The testing strategy
 * is in three parts:
 *
 * <ol>
 *   <li>testFindPreceding* tests in {@link ParboiledAutoCompleteTest} check that we are correctly
 *       extracting the preceding component (by explicitly setting up PotentialMatch object based on
 *       what we expect Parboiled to do on our grammars);
 *   <li>testAutoCompleteInterfaceName* tests below check that we do the right thing when nodeInput
 *       is given
 *   <li>testE2e* tests below check that the machinery works end-to-end on different grammars and
 *       potential partial things (thus helping validate the assumptions made in testFind*)
 * </ol>
 */
public class ParboiledAutoCompleteNodeRoleNameTest {

  private static NodeRolesData testNodeRolesData =
      NodeRolesData.builder()
          .setRoleMappings(
              ImmutableList.of(
                      NodeRoleDimension.builder("dim1")
                          .setRoleDimensionMappings(
                              ImmutableList.of(new RoleDimensionMapping("(r1.+)")))
                          .build(),
                      NodeRoleDimension.builder("dim2")
                          .setRoleDimensionMappings(
                              ImmutableList.of(new RoleDimensionMapping("(r2.+)")))
                          .build())
                  .stream()
                  .flatMap(d -> d.toRoleMappings().stream())
                  .collect(ImmutableList.toImmutableList()))
          .build();

  private static ParboiledAutoComplete getPAC() {
    return getPAC("dummy", Grammar.NODE_SPECIFIER);
  }

  private static ParboiledAutoComplete getPAC(String query, Grammar grammar) {
    return getPAC(Parser.instance(), query, grammar, testNodeRolesData);
  }

  private static ParboiledAutoComplete getPAC(
      Parser parser, String query, Grammar grammar, NodeRolesData testNodeRolesData) {
    return new ParboiledAutoComplete(
        parser,
        grammar,
        Parser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.builder().setNodes(ImmutableSet.of("r11", "r12", "r21")).build(),
        testNodeRolesData,
        new ReferenceLibrary(null));
  }

  /** Context-sensitive completion of node role name after dimension name */
  @Test
  public void testAutoCompleteNodeRoleName() {
    PathElement anchor = new PathElement(NODE_ROLE_NAME, null, 1, 42);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of());

    assertThat(
        getPAC().autoCompleteNodeRoleName(pm, "(dim1"),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("r11", 42, NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion("r12", 42, NODE_ROLE_NAME)));

    // return nothing if the dimension does not exist
    assertThat(getPAC().autoCompleteNodeRoleName(pm, "(nono"), equalTo(ImmutableSet.of()));
  }

  /**
   * Context-sensitive completion of node role name after dimension name when role name prefix is
   * present
   */
  @Test
  public void testAutoCompleteNodeRoleNamePrefix() {
    ParboiledAutoComplete pac = getPAC();

    PathElement anchor = new PathElement(Type.NODE_ROLE_NAME, null, 1, 42);

    // should match only r11
    assertThat(
        pac.autoCompleteNodeRoleName(
            new PotentialMatch(anchor, "r11", ImmutableList.of()), "(dim1"),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("r11", 42, NODE_ROLE_NAME)));

    // should not match anything
    assertThat(
        pac.autoCompleteNodeRoleName(new PotentialMatch(anchor, "r2", ImmutableList.of()), "(dim1"),
        equalTo(ImmutableSet.of()));

    // should match only r11 but preserve quotes
    assertThat(
        pac.autoCompleteNodeRoleName(
            new PotentialMatch(anchor, "\"r11", ImmutableList.of()), "(dim1"),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("\"r11\"", 42, NODE_ROLE_NAME)));
  }

  /**
   * The role name portion hasn't begun yet. We should suggest starting that and offer any matching
   * role names
   */
  @Test
  public void testE2eNoTailNoMatchingDimension() {
    String query = "@role(nodim";
    assertThat(
        getPAC(query, Grammar.NODE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(
                ",", query.length(), NODE_ROLE_AND_DIMENSION_TAIL)));

    // now as location specifier
    assertThat(
        getPAC(query, Grammar.LOCATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(
                ",", query.length(), NODE_ROLE_AND_DIMENSION_TAIL)));
  }

  @Test
  public void testE2eNoTailMatchingDimension() {
    String query = "@role(dim";
    assertThat(
        getPAC(query, Grammar.NODE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("dim1", 6, NODE_ROLE_DIMENSION_NAME),
            new ParboiledAutoCompleteSuggestion("dim2", 6, NODE_ROLE_DIMENSION_NAME),
            new ParboiledAutoCompleteSuggestion(
                ",", query.length(), NODE_ROLE_AND_DIMENSION_TAIL)));

    // now as location specifier
    assertThat(
        getPAC(query, Grammar.LOCATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("dim1", 6, NODE_ROLE_DIMENSION_NAME),
            new ParboiledAutoCompleteSuggestion("dim2", 6, NODE_ROLE_DIMENSION_NAME),
            new ParboiledAutoCompleteSuggestion(
                ",", query.length(), NODE_ROLE_AND_DIMENSION_TAIL)));
  }

  /** The role name portion has started but no prefix yet */
  @Test
  public void testE2eNoRoleNamePrefix() {
    String query = "@role(dim1,";

    Set<ParboiledAutoCompleteSuggestion> expected =
        ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion("r11", query.length(), NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion("r12", query.length(), NODE_ROLE_NAME));

    assertThat(getPAC(query, Grammar.NODE_SPECIFIER).run(), Matchers.equalTo(expected));

    // location specifier yields identical results
    assertThat(getPAC(query, Grammar.LOCATION_SPECIFIER).run(), Matchers.equalTo(expected));
  }

  /** The role name portion has started but no prefix yet */
  @Test
  public void testE2eRoleNamePrefix() {
    String query = "@role(dim1,r11";

    Set<ParboiledAutoCompleteSuggestion> expected =
        ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion("r11", 11, NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END));

    assertThat(getPAC(query, Grammar.NODE_SPECIFIER).run(), Matchers.equalTo(expected));

    // location specifier yields identical results
    assertThat(getPAC(query, Grammar.LOCATION_SPECIFIER).run(), Matchers.equalTo(expected));
  }

  /** Test auto completion when embedded within a location function */
  @Test
  public void testE2eLocationFuncNoRoleNamePrefix() {
    String query = "@enter(@role(dim1,";
    assertThat(
        getPAC(query, Grammar.LOCATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("r11", query.length(), NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion("r12", query.length(), NODE_ROLE_NAME)));
  }

  @Test
  public void testE2eLocationFuncRoleNamePrefix() {
    String query = "@enter(@role(dim1,r11";
    assertThat(
        getPAC(query, Grammar.LOCATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("r11", 18, NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END)));
  }

  /** Test that spaces do not disrupt us */
  @Test
  public void testE2eSpacesNoRoleNamePrefix() {
    String query = " @role ( dim1 ,";
    assertThat(
        getPAC(query, Grammar.NODE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("r11", query.length(), NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion("r12", query.length(), NODE_ROLE_NAME)));
  }

  @Test
  public void testE2eSpacesRoleNamePrefix() {
    String query = " @role ( dim1 , r11";
    assertThat(
        getPAC(query, Grammar.LOCATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("r11", 16, NODE_ROLE_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END)));
  }
}
