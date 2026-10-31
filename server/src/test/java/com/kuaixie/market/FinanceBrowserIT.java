package com.kuaixie.market;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Opt-in real browser integration; kept separate from the 52 core+HTTP cases. */
class FinanceBrowserIT {
  @Test
  void desktopMobileContractAndDiagnostics() throws Exception {
    FinanceHttpTest backend = new FinanceHttpTest();
    Process vite = null;
    Path project = Path.of("..").toAbsolutePath().normalize();
    Path evidence = project.resolve("verification/v7-batch5");
    Files.createDirectories(evidence);
    try {
      backend.setup();
      int port;
      try (var available = new java.net.ServerSocket(0)) {
        port = available.getLocalPort();
      }
      var start = new ProcessBuilder("node", "scripts/vite.mjs", "dev").directory(project.toFile());
      start.environment().put("HOST", "127.0.0.1");
      start.environment().put("PORT", String.valueOf(port));
      start
          .environment()
          .put("API_PROXY_TARGET", backend.base.substring(0, backend.base.length() - 4));
      vite =
          start
              .redirectErrorStream(true)
              .redirectOutput(evidence.resolve("browser-vite.log").toFile())
              .start();
      String url = "http://127.0.0.1:" + port;
      boolean ready = false;
      for (int i = 0; i < 100; i++) {
        if (!vite.isAlive()) break;
        try {
          if (Files.readString(evidence.resolve("browser-vite.log"))
              .contains("http://127.0.0.1:" + port)) {
            ready = true;
            break;
          }
        } catch (Exception ignored) {
        }
        Thread.sleep(100);
      }
      assertTrue(ready, "Vite startup");
      var browser =
          new ProcessBuilder("node", System.getProperty("finance.browser.script"))
              .directory(project.toFile());
      var env = browser.environment();
      env.put("FINANCE_BROWSER_BASE", url);
      env.put("FINANCE_BROWSER_BUYER", backend.tokens.get("buyer"));
      env.put("FINANCE_BROWSER_ADMIN", backend.tokens.get("admin"));
      env.put("FINANCE_BROWSER_RESULT", evidence.resolve("browser-results.json").toString());
      env.put("PLAYWRIGHT_MODULE", System.getProperty("finance.browser.module"));
      env.put("FINANCE_BROWSER_SHOTS", System.getProperty("finance.browser.shots"));
      var process =
          browser
              .redirectErrorStream(true)
              .redirectOutput(evidence.resolve("browser-run.log").toFile())
              .start();
      assertTrue(process.waitFor(55, TimeUnit.SECONDS), "browser completion");
      assertEquals(0, process.exitValue(), Files.readString(evidence.resolve("browser-run.log")));
      assertEquals(
          1,
          backend.fixture.db.queryForObject(
              "SELECT COUNT(*) FROM fin_confirmation", Integer.class));
      assertEquals(0, backend.fixture.value("revision"));
      assertEquals(0, backend.fixture.value("refunded"));
    } finally {
      if (vite != null) {
        vite.destroy();
        vite.waitFor(5, TimeUnit.SECONDS);
      }
      backend.close();
    }
  }
}
