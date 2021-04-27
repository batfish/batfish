package org.batfish.grammar.silent_syntax;

import com.google.common.testing.EqualsTester;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection.SilentSyntaxElem;
import org.junit.Test;

/** Test of {@link SilentSyntaxCollection}. */
public final class SilentSyntaxElemTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new SilentSyntaxElem("r1", 1, "t"), new SilentSyntaxElem("r1", 1, "t"))
        .addEqualityGroup(new SilentSyntaxElem("r2", 1, "t"))
        .addEqualityGroup(new SilentSyntaxElem("r1", 2, "t"))
        .addEqualityGroup(new SilentSyntaxElem("r1", 1, "u"))
        .testEquals();
  }
}
