package org.batfish.coordinator.matchers;

import javax.annotation.Nonnull;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.QueuedWork;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class WorkQueueMatchers {
  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * QueuedWork}'s {@code WorkItem}.
   */
  public static @Nonnull Matcher<QueuedWork> hasWorkItem(
      @Nonnull Matcher<? super WorkItem> subMatcher) {
    return new HasWorkItem(subMatcher);
  }

  private static final class HasWorkItem extends FeatureMatcher<QueuedWork, WorkItem> {

    public HasWorkItem(@Nonnull Matcher<? super WorkItem> subMatcher) {
      super(subMatcher, "A WorkQueue with workItem:", "workItem");
    }

    @Override
    protected WorkItem featureValueOf(QueuedWork actual) {
      return actual.getWorkItem();
    }
  }
}
