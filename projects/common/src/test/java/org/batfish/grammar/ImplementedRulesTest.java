package org.batfish.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.junit.Test;

/** Tests of {@link ImplementedRules}. */
@SuppressWarnings("unused")
public class ImplementedRulesTest {
  private abstract static class NoRules implements ParseTreeListener {
    public int someOtherMethod() {
      return 5;
    }
  }

  private abstract static class EnterRule implements ParseTreeListener {
    public void enterRule() {}
  }

  private abstract static class EnterAndExit implements ParseTreeListener {
    public void enterRule() {}

    public void exitRule2() {}
  }

  private abstract static class Collision implements ParseTreeListener {
    public void enterRule() {}

    public void enterRULE() {}
  }

  private abstract static class Extends extends EnterRule {
    public void enterRule2() {}
  }

  @Test
  public void testNoRules() {
    assertThat(ImplementedRules.getImplementedRules(NoRules.class), empty());
  }

  @Test
  public void testEnterRule() {
    assertThat(ImplementedRules.getImplementedRules(EnterRule.class), containsInAnyOrder("rule"));
  }

  @Test
  public void testEnterAndExitRule() {
    assertThat(
        ImplementedRules.getImplementedRules(EnterAndExit.class),
        containsInAnyOrder("rule", "rule2"));
  }

  /** Tests that multiple variants with same name is not a problem. */
  @Test
  public void testCollision() {
    assertThat(ImplementedRules.getImplementedRules(Collision.class), containsInAnyOrder("rule"));
  }

  /** Tests that inherited methods are not counted. */
  @Test
  public void testExtends() {
    assertThat(ImplementedRules.getImplementedRules(Extends.class), containsInAnyOrder("rule2"));
  }
}
