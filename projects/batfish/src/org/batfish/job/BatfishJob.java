package org.batfish.job;

import java.util.concurrent.Callable;

public abstract class BatfishJob<R extends BatfishJobResult<?>> implements
      Callable<R> {
}
