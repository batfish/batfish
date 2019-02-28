package org.batfish.bddreachability;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.TransitionMatchersImpl.MapsBackward;
import org.batfish.bddreachability.TransitionMatchersImpl.MapsForward;
import org.batfish.bddreachability.transition.Transition;
import org.hamcrest.Matcher;

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
}
