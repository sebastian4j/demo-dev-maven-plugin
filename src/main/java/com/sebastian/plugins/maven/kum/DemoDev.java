package com.sebastian.plugins.maven.kum;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * permite compilar con maven una aplicación para generar un jar y ejecutarlo con java.
 * <p>
 * la intención es utilizar un framework como KumuluzEE (https://ee.kumuluz.com/) y al modificar el
 * código fuente desde el IDE permitir volver a compilar y ejecutar la aplicación (DevTools SB?).
 *
 * El código para el registro de los escuchadores fue obtenido desde:
 * https://gist.github.com/fabriziofortino/83eb36c7b48e9b900c1da1d8508245cd#file-recursivewatcherservice-java
 *
 * @goal desa
 *
 * @phase process-sources
 */
@Mojo(name = "dev")
public class DemoDev extends AbstractMojo {
  /**
   * Location of the file.
   *
   * @parameter property="project.build.sourceDirectory"
   * @required
   */
  private File src;
  /**
   * Location of the file.
   *
   * @parameter property="project.build.directory"
   * @required
   */
  private File target;

  /**
   * Location of the file.
   *
   * @parameter property="project.build.finalName"
   * @required
   */
  private String finalName;
  /**
   * Location of the file.
   *
   * @parameter property="project.packaging"
   * @required
   */
  private String packaging;

  private WatchService watcher;
  private ExecutorService executor;

  private Process p;

  private void kill() {
    if (p != null && p.isAlive()) {
      p.destroyForcibly();
    }
  }

  private void compilar() {
    kill();
    getLog().info("compilar");
    try {
      p = new ProcessBuilder("mvn", "package").start();
      while (p.isAlive()) {
        //
      }
    } catch (final Exception e) {
      getLog().error("error al lanzar mvn", e);
    }
    getLog().info("compilado");
    kill();
  }

  private void lanzar() throws IOException {
    kill();
    final String nombre = target + File.separator + finalName + "." + packaging;
    p = new ProcessBuilder("java", "-jar", nombre).start();
    getLog().info("lanzado: " + nombre);
  }

  @Override
  public void execute() throws MojoExecutionException {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      kill();
      getLog().info("finalizado");
    }));
    getLog().info("src: " + src);
    getLog().info("target: " + target);
    getLog().info("finalName: " + finalName);
    getLog().info("packaging: " + packaging);
    try {
      watcher = FileSystems.getDefault().newWatchService();
      executor = Executors.newSingleThreadExecutor();
      escuchar();
    } catch (final Exception e) {
      getLog().info(e);
    }
  }

  private void escuchar() throws IOException {
    final Map<WatchKey, Path> keys = new HashMap<>();
    final Consumer<Path> register = p -> {
      if (!p.toFile().exists() || !p.toFile().isDirectory()) {
        throw new RuntimeException(p + "no es directorio");
      }
      try {
        Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
              throws IOException {
            getLog().info("registrado " + dir);
            final WatchKey watchKey = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            keys.put(watchKey, dir);
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (final IOException e) {
        throw new RuntimeException("error al registrar: " + p);
      }
    };
    register.accept(src.getParentFile().toPath());
    while (true) {
      compilar();
      lanzar();
      final WatchKey key;
      try {
        key = watcher.take();
      } catch (final InterruptedException ex) {
        getLog().error("take error", ex);
        return;
      }

      final Path dir = keys.get(key);
      if (dir == null) {
        getLog().error(key + " no reconocida");
        continue;
      }
      key.pollEvents().stream().filter(e -> (e.kind() != StandardWatchEventKinds.OVERFLOW))
          .forEach(e -> {
            getLog().info(e.kind().toString());
            final Path absPath = dir.resolve(((WatchEvent<Path>) e).context());
            if (absPath.toFile().isDirectory()) {
              register.accept(absPath);
            } else {
              final File f = absPath.toFile();
              getLog().info(f.getAbsolutePath());
            }
          });
      final boolean valid = key.reset();
      if (!valid) {
        break;
      }
    }
  }
}
