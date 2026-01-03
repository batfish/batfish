package org.batfish.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

/** Tests for {@link Pruner}. */
public class PrunerTest {

  @Test
  public void testString() {
    Pruner<String> pruner =
        Pruner.<String>builder()
            .addProperty(s -> s.charAt(0))
            .addProperty(s -> s.charAt(1))
            .addProperty(s -> s.charAt(2))
            .build();

    List<String> strings =
        ImmutableList.of(
            "aaa",
            "aab",
            "aac",
            "aba",
            "abb",
            "abc",
            "aca",
            "acb",
            "acc",
            //
            "baa",
            "bab",
            "bac",
            "bba",
            "bbb",
            "bbc",
            "bca",
            "bcb",
            "bcc",
            //
            "caa",
            "cab",
            "cac",
            "cba",
            "cbb",
            "cbc",
            "cca",
            "ccb",
            "ccc");

    assertThat(pruner.prune(strings, 100), equalTo(strings));
    assertThat(pruner.prune(strings, 5), containsInAnyOrder("aaa", "baa", "caa", "aba", "aca"));
    assertThat(
        pruner.prune(strings, 12),
        containsInAnyOrder(
            "aaa",
            "baa",
            "caa",
            "aba",
            "aca",
            "aab",
            "aac",
            // All properties covered. Start picking unpicked objects in input order
            "abb",
            "abc",
            "acb",
            "acc",
            "bab"));
  }
}
