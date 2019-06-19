package org.batfish.job;

import java.util.concurrent.Callable;
import org.batfish.common.BatfishLogger;
import org.batfish.config.Settings;

public abstract class BatfishJob<R extends BatfishJobResult<?, ?>> implements Callable<R> {

  protected final BatfishLogger _logger;

  protected final Settings _settings;

  public BatfishJob(Settings settings) {
    _settings = settings;
    _logger = new BatfishLogger(_settings.getLogLevel(), _settings.getTimestamp());
  }
}
