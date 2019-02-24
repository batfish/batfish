package org.batfish.dataplane.traceroute;

/**
 * Represents a stage (or several stages evaluated in order) in the tracing pipeline, on a
 * <em>single node (hop)</em>.
 */
public abstract class TracePipeline<T> {
  TracePipelineMediator<T> _mediator;

  TracePipeline(TracePipelineMediator<T> mediator) {
    _mediator = mediator;
  }

  abstract T process(PipelineContext flow);
}
