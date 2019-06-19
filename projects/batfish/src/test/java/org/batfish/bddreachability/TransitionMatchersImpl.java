package org.batfish.bddreachability;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class TransitionMatchersImpl {
  private TransitionMatchersImpl() {}

  static final class MapsForward extends TypeSafeDiagnosingMatcher<Transition> {
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

  static final class MapsBackward extends TypeSafeDiagnosingMatcher<Transition> {
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
