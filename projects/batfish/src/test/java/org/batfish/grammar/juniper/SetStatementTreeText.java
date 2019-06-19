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

/** Test of {@link SetStatementTree}. */
@ParametersAreNonnullByDefault
public final class SetStatementTreeText {

  @Test
  public void testAddSetStatementIndex() {
    SetStatementTree tree = new SetStatementTree();
    tree.addSetStatementIndex(1);

    assertThat(tree._lines.build(), contains(1));
  }

  @Test
  public void testGetOrAddSubtree() {
    String text = "foo";
    SetStatementTree tree = new SetStatementTree();

    assertThat(tree._children, anEmptyMap());

    SetStatementTree child = tree.getOrAddSubtree(text);

    assertThat(child, notNullValue());
    assertThat(tree._children, hasEntry(equalTo(text), sameInstance(child)));
    assertThat(tree.getOrAddSubtree(text), sameInstance(child));
  }

  @Test
  public void testGetSetStatementIndices() {
    SetStatementTree tree = new SetStatementTree();

    assertThat(tree.getSetStatementIndices(), empty());

    tree.addSetStatementIndex(1);

    assertThat(tree.getSetStatementIndices(), contains(1));

    tree.getOrAddSubtree("foo").addSetStatementIndex(2);

    assertThat(tree.getSetStatementIndices(), containsInAnyOrder(1, 2));

    tree.replaceSubtree("foo").addSetStatementIndex(3);

    assertThat(tree.getSetStatementIndices(), containsInAnyOrder(1, 3));
  }

  @Test
  public void testReplaceSubtree() {
    String text = "foo";
    SetStatementTree tree = new SetStatementTree();
    SetStatementTree oldChild = new SetStatementTree();
    tree._children.put("foo", oldChild);

    SetStatementTree newChild = tree.replaceSubtree(text);

    assertThat(tree._children, hasEntry(equalTo(text), sameInstance(newChild)));
    assertThat(newChild, not(sameInstance(oldChild)));
  }
}
