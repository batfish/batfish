package org.batfish.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.Interval;
import org.batfish.grammar.recovery.RecoveryCombinedParser;
import org.junit.jupiter.api.Test;

/** Tests of {@link BatfishCombinedParser}. */
final class BatfishCombinedParserTest {

  private static final GrammarSettings SETTINGS =
      MockGrammarSettings.builder()
          .setDisableUnrecognized(false)
          .setThrowOnLexerError(true)
          .setThrowOnParserError(true)
          .build();

  @Test
  void constructorComputesLookaheadSetOfEveryState() {
    BatfishCombinedParser<?, ?> parser = new RecoveryCombinedParser("simple\n", SETTINGS);

    ATN atn = parser.getParser().getATN();
    List<Integer> statesWithoutLookaheadSet =
        atn.states.stream()
            .filter(Objects::nonNull)
            .filter(state -> state.nextTokenWithinRule == null)
            .map(state -> state.stateNumber)
            .toList();

    assertThat(statesWithoutLookaheadSet, empty());
  }

  @Test
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // the cache is private to ANTLR
  void antlrIntervalCacheIsFullOnceAParserExists() throws ReflectiveOperationException {
    new RecoveryCombinedParser("simple\n", SETTINGS);

    // Throws once ANTLR no longer has the cache. BatfishCombinedParser then need not fill it.
    Field cacheField = Interval.class.getDeclaredField("cache");
    cacheField.setAccessible(true);
    Interval[] cache = (Interval[]) cacheField.get(null);

    assertThat(Arrays.asList(cache), everyItem(notNullValue()));
  }
}
