package org.batfish.grammar.juniper;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Test;

/** Test of {@link FlatStatementTree}. */
@ParametersAreNonnullByDefault
public final class FlatStatementTreeTest {

  @Test
  public void testAddFlatStatementIndex() {
    FlatStatementTree tree = new FlatStatementTree();
    tree.addFlatStatementIndex(1);

    assertThat(tree._lines.build(), contains(1));
  }

  @Test
  public void testGetOrAddSubtree() {
    String text = "foo";
    FlatStatementTree tree = new FlatStatementTree();

    assertThat(tree._children, anEmptyMap());

    FlatStatementTree child = tree.getOrAddSubtree(text);

    assertThat(child, notNullValue());
    assertThat(tree._children, hasEntry(equalTo(text), sameInstance(child)));
    assertThat(tree.getOrAddSubtree(text), sameInstance(child));
  }

  @Test
  public void testGetFlatStatementIndices() {
    FlatStatementTree tree = new FlatStatementTree();

    assertThat(tree.getFlatStatementIndices(), empty());

    tree.addFlatStatementIndex(1);

    assertThat(tree.getFlatStatementIndices(), contains(1));

    tree.getOrAddSubtree("foo").addFlatStatementIndex(2);

    assertThat(tree.getFlatStatementIndices(), containsInAnyOrder(1, 2));

    tree.replaceSubtree("foo").addFlatStatementIndex(3);

    assertThat(tree.getFlatStatementIndices(), containsInAnyOrder(1, 3));
  }

  @Test
  public void testReplaceSubtree() {
    String text = "foo";
    FlatStatementTree tree = new FlatStatementTree();
    FlatStatementTree oldChild = new FlatStatementTree();
    tree._children.put("foo", oldChild);

    FlatStatementTree newChild = tree.replaceSubtree(text);

    assertThat(tree._children, hasEntry(equalTo(text), sameInstance(newChild)));
    assertThat(newChild, not(sameInstance(oldChild)));
  }
}
