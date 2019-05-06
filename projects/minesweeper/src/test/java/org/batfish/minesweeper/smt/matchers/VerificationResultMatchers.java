package org.batfish.minesweeper.smt.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import java.util.SortedMap;
import org.batfish.minesweeper.smt.VerificationResult;
import org.batfish.minesweeper.smt.matchers.VerificationResultMatchersImpl.HasFailures;
import org.batfish.minesweeper.smt.matchers.VerificationResultMatchersImpl.HasIsVerified;
import org.batfish.minesweeper.smt.matchers.VerificationResultMatchersImpl.HasPacketModel;
import org.hamcrest.Matcher;

public final class VerificationResultMatchers {
  private VerificationResultMatchers() {}

  public static Matcher<VerificationResult> hasFailures(Matcher<? super Set<String>> matcher) {
    return new HasFailures(matcher);
  }

  public static Matcher<VerificationResult> hasFailures(Set<String> failures) {
    return new HasFailures(equalTo(failures));
  }

  public static Matcher<VerificationResult> hasIsVerified(boolean isVerified) {
    return new HasIsVerified(equalTo(isVerified));
  }

  public static Matcher<VerificationResult> hasPacketModel(
      Matcher<? super SortedMap<String, String>> matcher) {
    return new HasPacketModel(matcher);
  }
}
