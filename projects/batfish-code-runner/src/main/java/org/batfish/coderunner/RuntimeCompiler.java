package org.batfish.coderunner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

class RuntimeCompiler {
  private final JavaCompiler compiler;

  public RuntimeCompiler() {
    this.compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException(
          "Java compiler not available. Ensure you're running with a JDK.");
    }
  }

  public Class<?> compile(String sourceCode) throws Exception {
    String className = extractClassName(sourceCode);
    JavaFileObject sourceFile = new StringSourceJavaFileObject(className, sourceCode);
    InMemoryClassFileManager fileManager =
        new InMemoryClassFileManager(compiler.getStandardFileManager(null, null, null));
    JavaCompiler.CompilationTask task =
        compiler.getTask(
            null, fileManager, null, null, null, Collections.singletonList(sourceFile));
    if (!task.call()) {
      throw new RuntimeException("Compilation failed");
    }
    byte[] bytecode = fileManager.getCompiledBytes(className);
    return new ByteClassLoader().defineClass(className, bytecode);
  }

  private String extractClassName(String sourceCode) {
    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:public\\s+)?class\\s+(\\w+)");
    java.util.regex.Matcher m = p.matcher(sourceCode);
    if (m.find()) {
      return m.group(1);
    }
    throw new IllegalArgumentException("Could not extract class name");
  }

  private static class StringSourceJavaFileObject extends SimpleJavaFileObject {
    private final String code;

    StringSourceJavaFileObject(String className, String code) {
      super(
          URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
          Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }

  private static class ByteJavaFileObject extends SimpleJavaFileObject {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    ByteJavaFileObject(String className) {
      super(
          URI.create("bytes:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
      return outputStream;
    }

    byte[] getBytes() {
      return outputStream.toByteArray();
    }
  }

  private static class InMemoryClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private ByteJavaFileObject compiledClass;

    InMemoryClassFileManager(JavaFileManager fileManager) {
      super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
        Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
      compiledClass = new ByteJavaFileObject(className);
      return compiledClass;
    }

    byte[] getCompiledBytes(String className) {
      if (compiledClass == null) {
        throw new IllegalStateException("No compiled class available");
      }
      return compiledClass.getBytes();
    }
  }

  private static class ByteClassLoader extends ClassLoader {
    Class<?> defineClass(String name, byte[] bytes) {
      return defineClass(name, bytes, 0, bytes.length);
    }
  }
}
