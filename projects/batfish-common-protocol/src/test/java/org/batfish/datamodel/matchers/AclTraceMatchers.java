package org.batfish.datamodel.matchers;

import java.util.List;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class AclTraceMatchers {
  static final class HasEvents extends FeatureMatcher<AclTrace, List<TraceEvent>> {
    HasEvents(Matcher<? super List<TraceEvent>> subMatcher) {
      super(subMatcher, "An AclTrace with events:", "events");
    }

    @Override
    protected List<TraceEvent> featureValueOf(AclTrace actual) {
      return actual.getEvents();
    }
  }
}
