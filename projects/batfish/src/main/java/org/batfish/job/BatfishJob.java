package org.batfish.job;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import org.batfish.common.BatfishLogger;
import org.batfish.config.Settings;

public abstract class BatfishJob<R extends BatfishJobResult<?, ?>> implements Callable<R> {

  protected final BatfishLogger _logger;

  protected Semaphore _semaphore;

  protected final Settings _settings;

  public BatfishJob(Settings settings) {
    _settings = settings;
    _logger = new BatfishLogger(_settings.getLogLevel(), _settings.getTimestamp());
  }

  @Override
  public final R call() {
    try {
      R r = callBatfishJob();
      return r;
    } finally {
      _semaphore.release();
    }
  }

  protected abstract R callBatfishJob();

  public void setSemaphore(Semaphore semaphore) {
    _semaphore = semaphore;
  }
}
