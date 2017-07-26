package org.batfish.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UrlZipExplorer {

  private Set<String> _entries;
  private ZipInputStream _stream;
  private URL _url;

  public UrlZipExplorer(URL url) throws IOException {
    _url = url;
    _entries = new LinkedHashSet<>();
    initListing();
  }

  public void extractFiles(StringFilter filter, File destinationDir) throws IOException {
    _stream = new ZipInputStream(_url.openStream());
    ZipEntry ze = null;
    while ((ze = _stream.getNextEntry()) != null) {
      String entryName = ze.getName();
      if (filter.accept(entryName)) {
        Path dstPath = Paths.get(destinationDir.getAbsolutePath(), entryName);
        File parentDir = dstPath.toFile().getParentFile();
        parentDir.mkdirs();
        Files.copy(_stream, dstPath);
      }
    }
  }

  public InputStream getInputStream() {
    return _stream;
  }

  private void initListing() throws IOException {
    _stream = new ZipInputStream(_url.openStream());
    ZipEntry ze = null;
    while ((ze = _stream.getNextEntry()) != null) {
      String entryName = ze.getName();
      _entries.add(entryName);
    }
    _stream.close();
  }

  public Set<String> listFiles(StringFilter filter) {
    Set<String> matches = new LinkedHashSet<>();
    for (String entry : _entries) {
      if (filter.accept(entry)) {
        matches.add(entry);
      }
    }
    return matches;
  }

  public void openFile(String name) throws IOException {
    _stream = new ZipInputStream(_url.openStream());
    ZipEntry ze = null;
    while ((ze = _stream.getNextEntry()) != null) {
      String entryName = ze.getName();
      if (entryName.equals(name)) {
        return;
      }
    }
    throw new FileNotFoundException(name);
  }
}
