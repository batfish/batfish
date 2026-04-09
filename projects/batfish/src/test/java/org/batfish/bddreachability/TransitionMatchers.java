package org.batfish.bddreachability;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/** Matchers for {@link Transition}. */
public final class TransitionMatchers {
  /** Tests input-output behavior of {@link Transition#transitForward(net.sf.javabdd.BDD)}. */
  public static Matcher<Transition> mapsForward(BDD input, BDD output) {
    return new MapsForward(input, output);
  }

  /** Tests input-output behavior of {@link Transition#transitBackward(net.sf.javabdd.BDD)}. */
  public static Matcher<Transition> mapsBackward(BDD input, BDD output) {
    return new MapsBackward(input, output);
  }

  private static final class MapsForward extends TypeSafeDiagnosingMatcher<Transition> {
    private final BDD _input;
    private final BDD _output;

    MapsForward(BDD input, BDD output) {
      _input = input;
      _output = output;
    }

    @Override
    protected boolean matchesSafely(Transition transition, Description description) {
      BDD actualOutput = transition.transitForward(_input);
      if (actualOutput.equals(_output)) {
        return true;
      }
      description.appendText(String.format("got %s, expected %s", actualOutput, _output));
      return false;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format("A Transition forward-mapping %s to %s", _input, _output));
    }
  }

  private static final class MapsBackward extends TypeSafeDiagnosingMatcher<Transition> {
    private final BDD _input;
    private final BDD _output;

    MapsBackward(BDD input, BDD output) {
      _input = input;
      _output = output;
    }

    @Override
    protected boolean matchesSafely(Transition transition, Description description) {
      BDD actualOutput = transition.transitBackward(_input);
      if (actualOutput.equals(_output)) {
        return true;
      }
      description.appendText(String.format("got %s, expected %s", actualOutput, _output));
      return false;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format("A Transition backward-mapping %s to %s", _input, _output));
    }
  }
}
