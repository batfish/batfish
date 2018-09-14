package org.batfish.datamodel.matchers;

import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class TableAnswerElementMatchersImpl {

  static final class HasRowMatchers extends TypeSafeDiagnosingMatcher<TableAnswerElement> {

    private final Collection<Matcher<? super Row>> _rowMatchers;

    public HasRowMatchers(@Nonnull Collection<Matcher<? super Row>> rowMatchers) {
      _rowMatchers = rowMatchers;
    }

    @Override
    public void describeTo(Description description) {
      description.appendList("A TableAnswerElement with rows matching: [", ",", "]", _rowMatchers);
    }

    @Override
    protected boolean matchesSafely(TableAnswerElement item, Description mismatchDescription) {
      Iterator<Row> iterator = item.getRows().iterator();
      Iterable<Row> rows = () -> iterator;
      Matcher<Iterable<? extends Row>> matcher = Matchers.containsInAnyOrder(_rowMatchers);
      if (!matcher.matches(rows)) {
        matcher.describeMismatch(rows, mismatchDescription);
        return false;
      }
      return true;
    }
  }

  static final class HasRows extends FeatureMatcher<TableAnswerElement, Iterable<Row>> {
    public HasRows(@Nonnull Matcher<? super Iterable<Row>> subMatcher) {
      super(subMatcher, "TableAnswerElement with rows:", "rows");
    }

    @Override
    protected Iterable<Row> featureValueOf(TableAnswerElement actual) {
      return () -> actual.getRows().iterator();
    }
  }

  private TableAnswerElementMatchersImpl() {}
}
