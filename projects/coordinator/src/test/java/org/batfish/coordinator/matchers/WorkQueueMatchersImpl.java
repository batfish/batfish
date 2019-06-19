package org.batfish.coordinator.matchers;

import javax.annotation.Nonnull;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.QueuedWork;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class WorkQueueMatchersImpl {
  static class HasWorkItem extends FeatureMatcher<QueuedWork, WorkItem> {

    public HasWorkItem(@Nonnull Matcher<? super WorkItem> subMatcher) {
      super(subMatcher, "A WorkQueue with workItem:", "workItem");
    }

    @Override
    protected WorkItem featureValueOf(QueuedWork actual) {
      return actual.getWorkItem();
    }
  }
}
