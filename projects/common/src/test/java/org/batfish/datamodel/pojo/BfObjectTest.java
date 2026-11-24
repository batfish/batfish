package org.batfish.datamodel.pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

/** Tests of {@link BfObject}. */
class BfObjectTest {
  private static class A extends BfObject {
    public A(String id) {
      super(id);
    }
  }

  private static class B extends BfObject {
    public B(String id) {
      super(id);
    }
  }

  @Test
  void equalsSameIdAndType() {
    // We create the second "id" dynamically because otherwise compiler will intern the constants.
    assertThat(new A("id"), equalTo(new A("ID".toLowerCase())));
  }

  @Test
  void notEqualsSameIdAndDifferentType() {
    assertThat(new A("id"), not(equalTo(new B("id"))));
  }

  @Test
  void notEqualsDifferentIdAndSameType() {
    assertThat(new A("foo"), not(equalTo(new A("bar"))));
  }
}
