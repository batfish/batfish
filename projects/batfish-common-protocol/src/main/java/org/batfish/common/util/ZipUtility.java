package org.batfish.common.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.StringUtils;

public final class ZipUtility {

  /** Appends a zip of the given folder to the input {@link OutputStream}. */
  public static void zipToStream(Path srcFolder, OutputStream outputStream) throws IOException {
    ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
    try {
      Files.walkFileTree(
          srcFolder,
          new FileVisitor<Path>() {
            /** A list of parent directory names. */
            private final Stack<String> _path = new Stack<>();

            /** Memoized _path joined with "/"; does not contain a trailing '/'. */
            private String _pathStr = "";

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
              _path.push(dir.getFileName().toString());
              _pathStr = StringUtils.join(_path, "/");
              zipOutputStream.putNextEntry(new ZipEntry(_pathStr + '/'));
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              zipOutputStream.putNextEntry(new ZipEntry(_pathStr + '/' + file.getFileName()));
              Files.copy(file, zipOutputStream);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
              throw new IOException(
                  "Failed to access " + srcFolder.relativize(file) + " for zipping");
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
              _path.pop();
              _pathStr = StringUtils.join(_path, "/");
              return FileVisitResult.CONTINUE;
            }
          });
    } finally {
      zipOutputStream.finish();
    }
  }
}
