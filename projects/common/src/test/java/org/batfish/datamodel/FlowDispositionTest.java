package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.FlowDisposition.ALL_DISPOSITIONS;
import static org.batfish.datamodel.FlowDisposition.FAILURE_DISPOSITIONS;
import static org.batfish.datamodel.FlowDisposition.SUCCESS_DISPOSITIONS;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

/** Tests for {@link FlowDisposition}. */
class FlowDispositionTest {
  @Test
  void testInvariants() {
    checkState(
        Sets.intersection(SUCCESS_DISPOSITIONS, FAILURE_DISPOSITIONS).isEmpty(),
        "A FlowDisposition cannot be both a success and a failure.");
    checkState(
        Sets.union(SUCCESS_DISPOSITIONS, FAILURE_DISPOSITIONS).equals(ALL_DISPOSITIONS),
        "Every FlowDisposition must be either a success or a failure.");
  }
}
