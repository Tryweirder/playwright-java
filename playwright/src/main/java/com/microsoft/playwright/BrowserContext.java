/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.options.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * BrowserContexts provide a way to operate multiple independent browser sessions.
 *
 * <p> If a page opens another page, e.g. with a {@code window.open} call, the popup will belong to the parent page's browser
 * context.
 *
 * <p> Playwright allows creation of "incognito" browser contexts with {@code browser.newContext()} method. "Incognito" browser
 * contexts don't write any browsing data to disk.
 * <pre>{@code
 * // Create a new incognito browser context
 * BrowserContext context = browser.newContext();
 * // Create a new page inside context.
 * Page page = context.newPage();
 * page.navigate("https://example.com");
 * // Dispose context once it"s no longer needed.
 * context.close();
 * }</pre>
 */
public interface BrowserContext extends AutoCloseable {

  /**
   * Emitted when Browser context gets closed. This might happen because of one of the following:
   * <ul>
   * <li> Browser context is closed.</li>
   * <li> Browser application is closed or crashed.</li>
   * <li> The {@link Browser#close Browser.close()} method was called.</li>
   * </ul>
   */
  void onClose(Consumer<BrowserContext> handler);
  /**
   * Removes handler that was previously added with {@link #onClose onClose(handler)}.
   */
  void offClose(Consumer<BrowserContext> handler);

  /**
   * The event is emitted when a new Page is created in the BrowserContext. The page may still be loading. The event will
   * also fire for popup pages. See also {@link Page#onPopup Page.onPopup()} to receive events about popups relevant to a
   * specific page.
   *
   * <p> The earliest moment that page is available is when it has navigated to the initial url. For example, when opening a
   * popup with {@code window.open('http://example.com')}, this event will fire when the network request to "http://example.com" is
   * done and its response has started loading in the popup.
   * <pre>{@code
   * Page newPage = context.waitForPage(() -> {
   *   page.click("a[target=_blank]");
   * });
   * System.out.println(newPage.evaluate("location.href"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Use {@link Page#waitForLoadState Page.waitForLoadState()} to wait until the page gets to a particular state (you should
   * not need it in most cases).
   */
  void onPage(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onPage onPage(handler)}.
   */
  void offPage(Consumer<Page> handler);

  class ExposeBindingOptions {
    /**
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is
     * supported. When passing by value, multiple arguments are supported.
     */
    public Boolean handle;

    public ExposeBindingOptions withHandle(boolean handle) {
      this.handle = handle;
      return this;
    }
  }
  class GrantPermissionsOptions {
    /**
     * The [origin] to grant permissions to, e.g. "https://example.com".
     */
    public String origin;

    public GrantPermissionsOptions withOrigin(String origin) {
      this.origin = origin;
      return this;
    }
  }
  class StorageStateOptions {
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public Path path;

    public StorageStateOptions withPath(Path path) {
      this.path = path;
      return this;
    }
  }
  class WaitForPageOptions {
    /**
     * Receives the {@code Page} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<Page> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    public WaitForPageOptions withPredicate(Predicate<Page> predicate) {
      this.predicate = predicate;
      return this;
    }
    public WaitForPageOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Adds cookies into this browser context. All pages within this context will have these cookies installed. Cookies can be
   * obtained via {@link BrowserContext#cookies BrowserContext.cookies()}.
   * <pre>{@code
   * browserContext.addCookies(Arrays.asList(cookieObject1, cookieObject2));
   * }</pre>
   */
  void addCookies(List<Cookie> cookies);
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <ul>
   * <li> Whenever a page is created in the browser context or is navigated.</li>
   * <li> Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
   * evaluated in the context of the newly attached frame.</li>
   * </ul>
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> An example of overriding {@code Math.random} before the page loads:
   * <pre>{@code
   * // In your playwright script, assuming the preload.js file is in same directory.
   * browserContext.addInitScript(Paths.get("preload.js"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
   * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
   *
   * @param script Script to be evaluated in all pages in the browser context.
   */
  void addInitScript(String script);
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <ul>
   * <li> Whenever a page is created in the browser context or is navigated.</li>
   * <li> Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
   * evaluated in the context of the newly attached frame.</li>
   * </ul>
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> An example of overriding {@code Math.random} before the page loads:
   * <pre>{@code
   * // In your playwright script, assuming the preload.js file is in same directory.
   * browserContext.addInitScript(Paths.get("preload.js"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
   * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
   *
   * @param script Script to be evaluated in all pages in the browser context.
   */
  void addInitScript(Path script);
  /**
   * Returns the browser instance of the context. If it was launched as a persistent context null gets returned.
   */
  Browser browser();
  /**
   * Clears context cookies.
   */
  void clearCookies();
  /**
   * Clears all permission overrides for the browser context.
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.grantPermissions(Arrays.asList("clipboard-read"));
   * // do stuff ..
   * context.clearPermissions();
   * }</pre>
   */
  void clearPermissions();
  /**
   * Closes the browser context. All the pages that belong to the browser context will be closed.
   *
   * <p> <strong>NOTE:</strong> The default browser context cannot be closed.
   */
  void close();
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   */
  default List<Cookie> cookies() {
    return cookies((String) null);
  }
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @param urls Optional list of URLs.
   */
  List<Cookie> cookies(String urls);
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @param urls Optional list of URLs.
   */
  List<Cookie> cookies(List<String> urls);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context. When
   * called, the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext: BrowserContext,
   * page: Page, frame: Frame }}.
   *
   * <p> See {@link Page#exposeBinding Page.exposeBinding()} for page-only version.
   *
   * <p> An example of exposing page URL to all frames in all pages in the context:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit()
   *       Browser browser = webkit.launch(new BrowserType.LaunchOptions().withHeadless(false));
   *       BrowserContext context = browser.newContext();
   *       context.exposeBinding("pageURL", (source, args) -> source.page().url());
   *       Page page = context.newPage();
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>");
   *       page.click("button");
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> An example of passing an element handle:
   * <pre>{@code
   * context.exposeBinding("clicked", (source, args) -> {
   *   ElementHandle element = (ElementHandle) args[0];
   *   System.out.println(element.textContent());
   *   return null;
   * }, new BrowserContext.ExposeBindingOptions().withHandle(true));
   * page.setContent("" +
   *   "<script>\n" +
   *   "  document.addEventListener('click', event => window.clicked(event.target));\n" +
   *   "</script>\n" +
   *   "<div>Click me</div>\n" +
   *   "<div>Or click me</div>\n");
   * }</pre>
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  default void exposeBinding(String name, BindingCallback callback) {
    exposeBinding(name, callback, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context. When
   * called, the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext: BrowserContext,
   * page: Page, frame: Frame }}.
   *
   * <p> See {@link Page#exposeBinding Page.exposeBinding()} for page-only version.
   *
   * <p> An example of exposing page URL to all frames in all pages in the context:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit()
   *       Browser browser = webkit.launch(new BrowserType.LaunchOptions().withHeadless(false));
   *       BrowserContext context = browser.newContext();
   *       context.exposeBinding("pageURL", (source, args) -> source.page().url());
   *       Page page = context.newPage();
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>");
   *       page.click("button");
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> An example of passing an element handle:
   * <pre>{@code
   * context.exposeBinding("clicked", (source, args) -> {
   *   ElementHandle element = (ElementHandle) args[0];
   *   System.out.println(element.textContent());
   *   return null;
   * }, new BrowserContext.ExposeBindingOptions().withHandle(true));
   * page.setContent("" +
   *   "<script>\n" +
   *   "  document.addEventListener('click', event => window.clicked(event.target));\n" +
   *   "</script>\n" +
   *   "<div>Click me</div>\n" +
   *   "<div>Or click me</div>\n");
   * }</pre>
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  void exposeBinding(String name, BindingCallback callback, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context. When
   * called, the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}.
   *
   * <p> If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> See {@link Page#exposeFunction Page.exposeFunction()} for page-only version.
   *
   * <p> An example of adding an {@code md5} function to all pages in the context:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * import java.nio.charset.StandardCharsets;
   * import java.security.MessageDigest;
   * import java.security.NoSuchAlgorithmException;
   * import java.util.Base64;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit()
   *       Browser browser = webkit.launch(new BrowserType.LaunchOptions().withHeadless(false));
   *       context.exposeFunction("sha1", args -> {
   *         String text = (String) args[0];
   *         MessageDigest crypto;
   *         try {
   *           crypto = MessageDigest.getInstance("SHA-1");
   *         } catch (NoSuchAlgorithmException e) {
   *           return null;
   *         }
   *         byte[] token = crypto.digest(text.getBytes(StandardCharsets.UTF_8));
   *         return Base64.getEncoder().encodeToString(token);
   *       });
   *       Page page = context.newPage();
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.sha1('PLAYWRIGHT');\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>\n");
   *       page.click("button");
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  void exposeFunction(String name, FunctionCallback callback);
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if
   * specified.
   *
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   * <ul>
   * <li> {@code "geolocation"}</li>
   * <li> {@code "midi"}</li>
   * <li> {@code "midi-sysex"} (system-exclusive midi)</li>
   * <li> {@code "notifications"}</li>
   * <li> {@code "push"}</li>
   * <li> {@code "camera"}</li>
   * <li> {@code "microphone"}</li>
   * <li> {@code "background-sync"}</li>
   * <li> {@code "ambient-light-sensor"}</li>
   * <li> {@code "accelerometer"}</li>
   * <li> {@code "gyroscope"}</li>
   * <li> {@code "magnetometer"}</li>
   * <li> {@code "accessibility-events"}</li>
   * <li> {@code "clipboard-read"}</li>
   * <li> {@code "clipboard-write"}</li>
   * <li> {@code "payment-handler"}</li>
   * </ul>
   */
  default void grantPermissions(List<String> permissions) {
    grantPermissions(permissions, null);
  }
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if
   * specified.
   *
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   * <ul>
   * <li> {@code "geolocation"}</li>
   * <li> {@code "midi"}</li>
   * <li> {@code "midi-sysex"} (system-exclusive midi)</li>
   * <li> {@code "notifications"}</li>
   * <li> {@code "push"}</li>
   * <li> {@code "camera"}</li>
   * <li> {@code "microphone"}</li>
   * <li> {@code "background-sync"}</li>
   * <li> {@code "ambient-light-sensor"}</li>
   * <li> {@code "accelerometer"}</li>
   * <li> {@code "gyroscope"}</li>
   * <li> {@code "magnetometer"}</li>
   * <li> {@code "accessibility-events"}</li>
   * <li> {@code "clipboard-read"}</li>
   * <li> {@code "clipboard-write"}</li>
   * <li> {@code "payment-handler"}</li>
   * </ul>
   */
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  /**
   * Creates a new page in the browser context.
   */
  Page newPage();
  /**
   * Returns all open pages in the context.
   */
  List<Page> pages();
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> An example of a naïve handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(String url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> An example of a naïve handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Pattern url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> An example of a naïve handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Predicate<String> url, Consumer<Route> handler);
  /**
   * This setting will change the default maximum navigation time for the following methods and related shortcuts:
   * <ul>
   * <li> {@link Page#goBack Page.goBack()}</li>
   * <li> {@link Page#goForward Page.goForward()}</li>
   * <li> {@link Page#goto Page.goto()}</li>
   * <li> {@link Page#reload Page.reload()}</li>
   * <li> {@link Page#setContent Page.setContent()}</li>
   * <li> {@link Page#waitForNavigation Page.waitForNavigation()}</li>
   * </ul>
   *
   * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()} and {@link Page#setDefaultTimeout
   * Page.setDefaultTimeout()} take priority over {@link BrowserContext#setDefaultNavigationTimeout
   * BrowserContext.setDefaultNavigationTimeout()}.
   *
   * @param timeout Maximum navigation time in milliseconds
   */
  void setDefaultNavigationTimeout(double timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   *
   * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()}, {@link Page#setDefaultTimeout
   * Page.setDefaultTimeout()} and {@link BrowserContext#setDefaultNavigationTimeout
   * BrowserContext.setDefaultNavigationTimeout()} take priority over {@link BrowserContext#setDefaultTimeout
   * BrowserContext.setDefaultTimeout()}.
   *
   * @param timeout Maximum time in milliseconds
   */
  void setDefaultTimeout(double timeout);
  /**
   * The extra HTTP headers will be sent with every request initiated by any page in the context. These headers are merged
   * with page-specific extra HTTP headers set with {@link Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}. If page
   * overrides a particular header, page-specific header value will be used instead of the browser context header value.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#setExtraHTTPHeaders BrowserContext.setExtraHTTPHeaders()} does not guarantee the order of headers
   * in the outgoing requests.
   *
   * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
   */
  void setExtraHTTPHeaders(Map<String, String> headers);
  /**
   * Sets the context's geolocation. Passing {@code null} or {@code undefined} emulates position unavailable.
   * <pre>{@code
   * browserContext.setGeolocation(new Geolocation(59.95, 30.31667));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Consider using {@link BrowserContext#grantPermissions BrowserContext.grantPermissions()} to grant permissions for the
   * browser context pages to read its geolocation.
   */
  void setGeolocation(Geolocation geolocation);
  /**
   *
   *
   * @param offline Whether to emulate network being offline for the browser context.
   */
  void setOffline(boolean offline);
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   */
  default String storageState() {
    return storageState(null);
  }
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   */
  String storageState(StorageStateOptions options);
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   */
  default void unroute(String url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @param handler Optional handler function used to register a routing with {@link BrowserContext#route BrowserContext.route()}.
   */
  void unroute(String url, Consumer<Route> handler);
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   */
  default void unroute(Pattern url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @param handler Optional handler function used to register a routing with {@link BrowserContext#route BrowserContext.route()}.
   */
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   */
  default void unroute(Predicate<String> url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @param handler Optional handler function used to register a routing with {@link BrowserContext#route BrowserContext.route()}.
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes {@code Page}
   * value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value. Will throw an error if
   * the context closes before new {@code Page} is created.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  default Page waitForPage(Runnable callback) {
    return waitForPage(null, callback);
  }
  /**
   * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes {@code Page}
   * value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value. Will throw an error if
   * the context closes before new {@code Page} is created.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  Page waitForPage(WaitForPageOptions options, Runnable callback);
}

