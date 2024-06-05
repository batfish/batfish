package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.OPERATOR_END;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.junit.Test;

/**
 * Tests for reference book entity auto completion in {@link ParboiledAutoComplete}. The testing
 * strategy is in three parts:
 *
 * <ol>
 *   <li>testFindPreceding* tests in {@link ParboiledAutoCompleteTest} check that we are correctly
 *       extracting the preceding component (by explicitly setting up PotentialMatch object based on
 *       what we expect Parboiled to do on our grammars);
 *   <li>testAutoCompleteReferenceBookEntity* tests below check that we do the right thing when
 *       nodeInput is given
 *   <li>testE2e* tests below check that the machinery works end-to-end on different grammars and
 *       potential partial things (thus helping validate the assumptions made in testFind*)
 * </ol>
 */
public class ParboiledAutoCompleteReferenceBookEntityTest {

  private static ReferenceLibrary testLibrary =
      new ReferenceLibrary(
          ImmutableList.of(
              ReferenceBook.builder("b1a")
                  .setAddressGroups(
                      ImmutableList.of(
                          new AddressGroup(null, "g11"), new AddressGroup(null, "g12")))
                  .setInterfaceGroups(
                      ImmutableList.of(
                          new InterfaceGroup(ImmutableSortedSet.of(), "i11"),
                          new InterfaceGroup(ImmutableSortedSet.of(), "i12")))
                  .build(),
              ReferenceBook.builder("b2a")
                  .setAddressGroups(ImmutableList.of(new AddressGroup(null, "g21")))
                  .setInterfaceGroups(
                      ImmutableList.of(new InterfaceGroup(ImmutableSortedSet.of(), "i21")))
                  .build()));

  private static ParboiledAutoComplete getPAC() {
    return getPAC("dummy", Grammar.IP_SPACE_SPECIFIER);
  }

  private static ParboiledAutoComplete getPAC(String query, Grammar grammar) {
    return getPAC(Parser.instance(), query, grammar, testLibrary);
  }

  private static ParboiledAutoComplete getPAC(
      Parser parser, String query, Grammar grammar, ReferenceLibrary testLibrary) {
    return new ParboiledAutoComplete(
        parser,
        grammar,
        Parser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.builder().build(),
        NodeRolesData.builder().build(),
        testLibrary);
  }

  /** Context-sensitive completion of entity name after book name */
  @Test
  public void testAutoCompleteReferenceBookEntity() {
    PathElement anchor = new PathElement(ADDRESS_GROUP_NAME, null, 1, 42);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of());

    assertThat(
        getPAC()
            .autoCompleteReferenceBookEntity(pm, "(b1a", ParboiledAutoComplete::addressGroupGetter),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", 42, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", 42, ADDRESS_GROUP_NAME)));

    // return nothing if the dimension does not exist
    assertThat(getPAC().autoCompleteNodeRoleName(pm, "(nono"), equalTo(ImmutableSet.of()));
  }

  /**
   * Context-sensitive completion of entity name after book name when group name prefix is present
   */
  @Test
  public void testAutoCompleteNodeRoleNamePrefix() {
    ParboiledAutoComplete pac = getPAC();

    PathElement anchor = new PathElement(ADDRESS_GROUP_NAME, null, 1, 42);

    // should match only r11
    assertThat(
        pac.autoCompleteReferenceBookEntity(
            new PotentialMatch(anchor, "g11", ImmutableList.of()),
            "(b1a",
            ParboiledAutoComplete::addressGroupGetter),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("g11", 42, ADDRESS_GROUP_NAME)));

    // should not match anything
    assertThat(
        pac.autoCompleteReferenceBookEntity(
            new PotentialMatch(anchor, "g2", ImmutableList.of()),
            "(b1a",
            ParboiledAutoComplete::addressGroupGetter),
        equalTo(ImmutableSet.of()));

    // should match only r11 but preserve quotes
    assertThat(
        pac.autoCompleteReferenceBookEntity(
            new PotentialMatch(anchor, "\"g11", ImmutableList.of()),
            "(b1a",
            ParboiledAutoComplete::addressGroupGetter),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("\"g11\"", 42, ADDRESS_GROUP_NAME)));
  }

  /**
   * The group name portion hasn't begun yet. We should suggest starting that and offer any matching
   * role names
   */
  @Test
  public void testE2eNoTailNoMatchingBook() {
    String query = "@addressGroup(nobook";
    assertThat(
        getPAC(query, Grammar.IP_SPACE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(
                ",", query.length(), REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL)));
  }

  @Test
  public void testE2eNoTailMatchingBook() {
    String query = "@addressGroup(b";
    assertThat(
        getPAC(query, Grammar.IP_SPACE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("b1a", 14, REFERENCE_BOOK_NAME),
            new ParboiledAutoCompleteSuggestion("b2a", 14, REFERENCE_BOOK_NAME),
            new ParboiledAutoCompleteSuggestion(
                ",", query.length(), REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL)));
  }

  /** The group name portion has started but no prefix yet */
  @Test
  public void testE2eNoGroupNamePrefix() {
    String query = "@addressGroup(b1a,";

    Set<ParboiledAutoCompleteSuggestion> expected =
        ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion("g11", query.length(), ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", query.length(), ADDRESS_GROUP_NAME));

    assertThat(getPAC(query, Grammar.IP_SPACE_SPECIFIER).run(), equalTo(expected));
  }

  /** The group name portion has started but no prefix yet */
  @Test
  public void testE2eGroupNamePrefix() {
    String query = "@addressGroup(b1a,g11";

    Set<ParboiledAutoCompleteSuggestion> expected =
        ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion("g11", 18, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END));

    assertThat(getPAC(query, Grammar.IP_SPACE_SPECIFIER).run(), equalTo(expected));
  }

  /** Test auto completion when embedded within a function */
  @Test
  public void testE2eEmbeddedNoGroupNamePrefix() {
    String query = "@connectedTo(@addressGroup(b1a,";
    assertThat(
        getPAC(query, Grammar.INTERFACE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", query.length(), ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", query.length(), ADDRESS_GROUP_NAME)));
  }

  @Test
  public void testE2eLocationFuncGroupNamePrefix() {
    String query = "@connectedTo(@addressGroup(b1a,g11";
    assertThat(
        getPAC(query, Grammar.LOCATION_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", 31, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END)));
  }

  /** Test that spaces do not disrupt us */
  @Test
  public void testE2eSpacesNoGroupNamePrefix() {
    String query = " @addressGroup ( b1a ,";
    assertThat(
        getPAC(query, Grammar.IP_SPACE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", query.length(), ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", query.length(), ADDRESS_GROUP_NAME)));
  }

  @Test
  public void testE2eSpacesGroupNamePrefix() {
    String query = " @addressGroup ( b1a , g11";
    assertThat(
        getPAC(query, Grammar.IP_SPACE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", 23, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END)));
  }

  /** A test that interface groups work as well */
  @Test
  public void testE2eInterfaceGroup() {
    String query = " @interfaceGroup ( b1a ,";
    assertThat(
        getPAC(query, Grammar.INTERFACE_SPECIFIER).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("i11", query.length(), INTERFACE_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("i12", query.length(), INTERFACE_GROUP_NAME)));
  }
}
