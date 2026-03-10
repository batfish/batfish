package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class ForAll<E> extends TypeSafeDiagnosingMatcher<Iterable<? extends E>> {

  private final Matcher<? super E> _conformsTo;
  private final Matcher<? super E> _identifiedBy;

  ForAll(@Nonnull Matcher<? super E> identifiedBy, @Nonnull Matcher<? super E> conformsTo) {
    _identifiedBy = identifiedBy;
    _conformsTo = conformsTo;
  }

  @Override
  public void describeTo(Description description) {
    description
        .appendText("An iterable in which all items identified by: ")
        .appendDescriptionOf(_identifiedBy)
        .appendText(" conform to: ")
        .appendDescriptionOf(_conformsTo);
  }

  @Override
  protected boolean matchesSafely(Iterable<? extends E> item, Description mismatchDescription) {
    for (E element : item) {
      if (_identifiedBy.matches(element)) {
        if (!_conformsTo.matches(element)) {
          mismatchDescription
              .appendText("Item identified by: ")
              .appendDescriptionOf(_identifiedBy)
              .appendText(" does not conform to: ")
              .appendDescriptionOf(_conformsTo)
              .appendText(". ");
          _conformsTo.describeMismatch(element, mismatchDescription);
          return false;
        }
      }
    }
    return true;
  }
}
