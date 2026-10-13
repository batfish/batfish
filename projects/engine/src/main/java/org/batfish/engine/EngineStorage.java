package org.batfish.engine;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/** Exclusive ownership of the standalone engine's rebuildable snapshot cache. */
final class EngineStorage implements AutoCloseable {
  // Never open a second channel for an owned lock file: on some systems closing
  // that channel would release another channel's process-wide file lock.
  private static final Set<Path> OWNED = new HashSet<>();
  private final Path root;
  private boolean closed;
  private final FileChannel channel;
  private final FileLock lock;

  private EngineStorage(Path root, FileChannel channel, FileLock lock) {
    this.root = root;
    this.channel = channel;
    this.lock = lock;
  }

  static synchronized EngineStorage open(Path base) throws IOException {
    Files.createDirectories(base);
    base = base.toRealPath();
    if (!OWNED.add(base)) throw new IOException("engine storage is already in use");
    FileChannel channel;
    try {
      channel =
          FileChannel.open(
              base.resolve(".engine.lock"),
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE,
              LinkOption.NOFOLLOW_LINKS);
    } catch (IOException | RuntimeException failure) {
      OWNED.remove(base);
      throw failure;
    }
    try {
      FileLock lock;
      try {
        lock = channel.tryLock();
      } catch (OverlappingFileLockException e) {
        throw new IOException("engine storage is already in use", e);
      }
      if (lock == null) throw new IOException("engine storage is already in use");
      EngineStorage storage = new EngineStorage(base, channel, lock);
      try {
        // The public registry is process-local. No previous snapshot can be addressed
        // after restart; remove only this engine's tenant cache, never other networks.
        Path current = base;
        for (String part : new String[] {"networks", EngineBatfish.NETWORK.getId(), "snapshots"}) {
          current = current.resolve(part);
          if (Files.isSymbolicLink(current)) {
            throw new IOException("engine snapshot cache must not contain directory symlinks");
          }
        }
        if (Files.exists(current)) {
          try (var paths = Files.walk(current)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
          }
        }
        return storage;
      } catch (IOException | RuntimeException e) {
        storage.close();
        throw e;
      }
    } catch (IOException | RuntimeException e) {
      try {
        channel.close();
      } finally {
        OWNED.remove(base);
      }
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (EngineStorage.class) {
      if (closed) return;
      try {
        if (lock.isValid()) lock.release();
      } finally {
        try {
          channel.close();
        } finally {
          closed = !channel.isOpen();
          if (closed) OWNED.remove(root);
        }
      }
    }
  }
}
