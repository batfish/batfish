package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class TableAnswerElementMatchersImpl {

  static final class HasRows extends FeatureMatcher<TableAnswerElement, Iterable<Row>> {
    public HasRows(@Nonnull Matcher<? super Iterable<Row>> subMatcher) {
      super(subMatcher, "TableAnswerElement with rows:", "rows");
    }

    @Override
    protected Iterable<Row> featureValueOf(TableAnswerElement actual) {
      return () -> actual.getRows().iterator();
    }
  }

  static final class HasWarnings extends FeatureMatcher<TableAnswerElement, Iterable<String>> {
    public HasWarnings(@Nonnull Matcher<? super Iterable<String>> subMatcher) {
      super(subMatcher, "TableAnswerElement with warnings:", "warnings");
    }

    @Override
    protected Iterable<String> featureValueOf(TableAnswerElement tableAnswerElement) {
      return tableAnswerElement.getWarnings();
    }
  }

  private TableAnswerElementMatchersImpl() {}
}
