package org.batfish.datamodel.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsInstanceThat<SuperT, SubT extends SuperT> extends TypeSafeDiagnosingMatcher<SuperT> {

  private Class<SubT> _class;

  private final Matcher<? super SubT> _subMatcher;

  public IsInstanceThat(Class<SubT> clazz, Matcher<? super SubT> subMatcher) {
    _class = clazz;
    _subMatcher = subMatcher;
  }

  @Override
  public void describeTo(Description description) {
    description.appendDescriptionOf(_subMatcher);
  }

  @Override
  protected boolean matchesSafely(SuperT item, Description mismatchDescription) {
    if (!_class.isInstance(item)) {
      mismatchDescription.appendText(
          String.format("%s is not an instance of %s", item, _class.getSimpleName()));
      return false;
    }
    boolean matches = _subMatcher.matches(item);
    if (!matches) {
      _subMatcher.describeMismatch(item, mismatchDescription);
    }
    return matches;
  }
}
