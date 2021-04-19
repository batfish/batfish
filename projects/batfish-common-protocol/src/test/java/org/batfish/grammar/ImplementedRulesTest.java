package org.batfish.grammar;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests of {@link ImplementedRules}. */
@SuppressWarnings("unused")
public class ImplementedRulesTest {
  private static class NoRules {
    public int someOtherMethod() {
      return 5;
    }
  }

  private static class EnterRule {
    public void enterRule() {}
  }

  private static class EnterAndExit {
    public void enterRule() {}

    public void exitRule2() {}
  }

  private static class Collision {
    public void enterRule() {}

    public void enterRULE() {}
  }

  private static class Extends extends EnterRule {
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
