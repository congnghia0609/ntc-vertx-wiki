/*
 * Copyright 2019 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.starter.http;

import com.github.rjeschke.txtmark.Processor;
//import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import io.vertx.starter.database.WikiDatabaseService;
import io.vertx.starter.DatabaseConstants;
import static io.vertx.starter.database.WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_DRIVER_CLASS;
import static io.vertx.starter.database.WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE;
import static io.vertx.starter.database.WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_URL;

//import io.vertx.starter.database.reactivex.WikiDatabaseService;
//import io.vertx.reactivex.core.AbstractVerticle;
//import io.vertx.reactivex.core.http.HttpServer;
//import io.vertx.reactivex.ext.auth.User;
//import io.vertx.reactivex.ext.auth.jdbc.JDBCAuth;
//import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
//import io.vertx.reactivex.ext.jdbc.JDBCClient;
//import io.vertx.reactivex.ext.web.Router;
//import io.vertx.reactivex.ext.web.RoutingContext;
//import io.vertx.reactivex.ext.web.client.HttpResponse;
//import io.vertx.reactivex.ext.web.client.WebClient;
//import io.vertx.reactivex.ext.web.codec.BodyCodec;
//import io.vertx.reactivex.ext.web.handler.*;
//import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
//import io.vertx.reactivex.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Oct 9, 2019
 * 
 * // Gen Key for HTTPS
 * keytool -genkey -alias test -keyalg RSA -keystore server-keystore.jks -keysize 2048 -validity 360 -dname CN=localhost -keypass secret4321 -storepass secret4321
 * 
 * // Gen Key for JWT
 * keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret321jwt -keyalg HMacSHA256 -keysize 2048 -alias HS256 -keypass secret321jwt
 * keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA384 -keysize 2048 -alias HS384 -keypass secret
 * keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA512 -keysize 2048 -alias HS512 -keypass secret
 * keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS256 -keypass secret -sigalg SHA256withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
 * keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS384 -keypass secret -sigalg SHA384withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
 * keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS512 -keypass secret -sigalg SHA512withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
 * keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES256 -keypass secret -sigalg SHA256withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
 * keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES384 -keypass secret -sigalg SHA384withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
 * keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES512 -keypass secret -sigalg SHA512withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
 * 
 */
public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
    public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

    //private String wikiDbQueue = "wikidb.queue";
    private FreeMarkerTemplateEngine templateEngine;
    private WikiDatabaseService dbService;

    @Override
    public void start(Promise<Void> promise) throws Exception {
        String wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");
//        dbService = (WikiDatabaseService) io.vertx.starter.database.WikiDatabaseService.createProxy(vertx.getDelegate(), wikiDbQueue);
        dbService = WikiDatabaseService.createProxy(vertx, wikiDbQueue);

        HttpServer server = vertx.createHttpServer();
//        HttpServer server = vertx.createHttpServer(new HttpServerOptions()
//                .setSsl(true)
//                .setKeyStoreOptions(new JksOptions()
//                        .setPath("server-keystore.jks")
//                        .setPassword("secret4321")));

        JDBCClient dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", config().getString(CONFIG_WIKIDB_JDBC_URL, DatabaseConstants.DEFAULT_WIKIDB_JDBC_URL)) // "jdbc:hsqldb:file:db/wiki"
                .put("driver_class", config().getString(CONFIG_WIKIDB_JDBC_DRIVER_CLASS, DatabaseConstants.DEFAULT_WIKIDB_JDBC_DRIVER_CLASS)) // "org.hsqldb.jdbcDriver"
                .put("max_pool_size", config().getInteger(CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, DatabaseConstants.DEFAULT_JDBC_MAX_POOL_SIZE))); // 30

        JDBCAuth auth = JDBCAuth.create(vertx, dbClient);

        Router router = Router.router(vertx);
        
        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(UserSessionHandler.create(auth));

        AuthHandler authHandler = RedirectAuthHandler.create(auth, "/login");
        router.route("/").handler(authHandler);
        router.route("/wiki/*").handler(authHandler);
        router.route("/action/*").handler(authHandler);

//        router.get("/").handler(this::indexHandler);
        router.get("/wiki/:page").handler(this::pageRenderingHandler);
        router.post("/action/save").handler(this::pageUpdateHandler);
        router.post("/action/create").handler(this::pageCreateHandler);
        router.get("/action/backup").handler(this::backupHandler);
        router.post("/action/delete").handler(this::pageDeletionHandler);
        
        router.get("/login").handler(this::loginHandler);
        router.post("/login-auth").handler(FormLoginHandler.create(auth));

        router.get("/logout").handler(context -> {
            context.clearUser();
            context.response()
                    .setStatusCode(302)
                    .putHeader("Location", "/")
                    .end();
        });
        
        
//        router.get("/").handler(this::indexHandler);
//        router.get("/wiki/:page").handler(this::pageRenderingHandler);
//        router.post().handler(BodyHandler.create());
//        router.post("/save").handler(this::pageUpdateHandler);
//        router.post("/create").handler(this::pageCreateHandler);
//        router.post("/delete").handler(this::pageDeletionHandler);
//        router.get("/backup").handler(this::backupHandler);
        
        Router apiRouter = Router.router(vertx);
        JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("keystore.jceks")
                        .setType("jceks")
                        .setPassword("secret321jwt")));

        apiRouter.route().handler(JWTAuthHandler.create(jwtAuth, "/api/token"));
        apiRouter.get("/token").handler(context -> {
            JsonObject creds = new JsonObject()
                    .put("username", context.request().getHeader("login"))
                    .put("password", context.request().getHeader("password"));
            //<editor-fold defaultstate="collapsed" desc="Code Step8">
//            auth.rxAuthenticate(creds).flatMap(user -> {
//                Single<Boolean> create = user.rxIsAuthorized("create");
//                Single<Boolean> delete = user.rxIsAuthorized("delete");
//                Single<Boolean> update = user.rxIsAuthorized("update");
//
//                return Single.zip(create, delete, update, (canCreate, canDelete, canUpdate) -> {
//                    return jwtAuth.generateToken(
//                            new JsonObject()
//                                    .put("username", context.request().getHeader("login"))
//                                    .put("canCreate", canCreate)
//                                    .put("canDelete", canDelete)
//                                    .put("canUpdate", canUpdate),
//                            new JWTOptions()
//                                    .setSubject("Wiki API")
//                                    .setIssuer("Vert.x"));
//                });
//            }).subscribe(token -> {
//                context.response().putHeader("Content-Type", "text/plain").end(token);
//            }, t -> context.fail(401));
            //</editor-fold>
            
            auth.authenticate(creds, authResult -> {
                if (authResult.succeeded()) {
                    User user = authResult.result();
                    user.isAuthorized("create", canCreate -> {
                        user.isAuthorized("delete", canDelete -> {
                            user.isAuthorized("update", canUpdate -> {
                                String token = jwtAuth.generateToken(
                                        new JsonObject()
                                                .put("username", context.request().getHeader("login"))
                                                .put("canCreate", canCreate.succeeded() && canCreate.result())
                                                .put("canDelete", canDelete.succeeded() && canDelete.result())
                                                .put("canUpdate", canUpdate.succeeded() && canUpdate.result()),
                                        new JWTOptions()
                                                .setSubject("Wiki API")
                                                .setIssuer("Vert.x"));
                                context.response().putHeader("Content-Type", "text/plain").end(token);
                            });
                        });
                    });
                } else {
                    context.fail(401);
                }
            });
        });
        apiRouter.get("/pages").handler(this::apiRoot);
        apiRouter.get("/pages/:id").handler(this::apiGetPage);
        apiRouter.post().handler(BodyHandler.create());
        apiRouter.post("/pages").handler(this::apiCreatePage);
        apiRouter.put().handler(BodyHandler.create());
        apiRouter.put("/pages/:id").handler(this::apiUpdatePage);
        apiRouter.delete("/pages/:id").handler(this::apiDeletePage);
        router.mountSubRouter("/api", apiRouter);
        
        // Static file
        router.get("/app/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.get("/").handler(context -> context.reroute("/app/index.html"));
        
        router.post("/app/markdown").handler(context -> {
            String html = Processor.process(context.getBodyAsString());
            context.response()
                    .putHeader("Content-Type", "text/html")
                    .setStatusCode(200)
                    .end(html);
        });
        
        // WebSocket
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("app.markdown"))
                .addOutboundPermitted(new PermittedOptions().setAddress("page.saved"));
        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);
        
        vertx.eventBus().<String>consumer("app.markdown", msg -> {
            String html = Processor.process(msg.body());
            msg.reply(html);
        });

        
        templateEngine = FreeMarkerTemplateEngine.create(vertx);

        int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
        server.requestHandler(router)
                .listen(portNumber, ar -> {
                    if (ar.succeeded()) {
                        LOGGER.info("HTTP server running on port " + portNumber);
                        promise.complete();
                    } else {
                        LOGGER.error("Could not start a HTTP server", ar.cause());
                        promise.fail(ar.cause());
                    }
                });
    }
    
    private void loginHandler(RoutingContext context) {
        context.put("title", "Login");
        templateEngine.render(context.data(), "templates/login.ftl", ar -> {
            if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
            } else {
                context.fail(ar.cause());
            }
        });
    }
    
    private void indexHandler(RoutingContext context) {
        context.user().isAuthorized("create", res -> {
            boolean canCreatePage = res.succeeded() && res.result();
            dbService.fetchAllPages(reply -> {
                if (reply.succeeded()) {
                    context.put("title", "Wiki home");
                    context.put("pages", reply.result().getList());
                    context.put("canCreatePage", canCreatePage);
                    context.put("username", context.user().principal().getString("username"));
                    templateEngine.render(context.data(), "templates/index.ftl", ar -> {
                        if (ar.succeeded()) {
                            context.response().putHeader("Content-Type", "text/html");
                            context.response().end(ar.result());
                        } else {
                            context.fail(ar.cause());
                        }
                    });
                } else {
                    context.fail(reply.cause());
                }
            });
        });
    }
    
    //<editor-fold defaultstate="collapsed" desc="Code Step6">
//    private void indexHandler(RoutingContext context) {
//        dbService.fetchAllPages(reply -> {
//            if (reply.succeeded()) {
//                context.put("title", "Wiki home");
//                context.put("pages", reply.result().getList());
//                String url = context.get("backup_gist_url");
//                if (url == null || url.isEmpty()) {
//                    context.put("backup_gist_url", "");
//                }
//                templateEngine.render(context.data(), "templates/index.ftl", ar -> {
//                    if (ar.succeeded()) {
//                        context.response().putHeader("Content-Type", "text/html");
//                        context.response().end(ar.result());
//                    } else {
//                        context.fail(ar.cause());
//                    }
//                });
//            } else {
//                context.fail(reply.cause());
//            }
//        });
//    }
    //</editor-fold>

    private static final String EMPTY_PAGE_MARKDOWN
            = "# A new page\n"
            + "\n"
            + "Feel-free to write in Markdown!\n";

    // PAGE
    private void pageRenderingHandler(RoutingContext context) {
        String requestedPage = context.request().getParam("page");
        dbService.fetchPage(requestedPage, reply -> {
            if (reply.succeeded()) {

                JsonObject payLoad = reply.result();
                boolean found = payLoad.getBoolean("found");
                String rawContent = payLoad.getString("rawContent", EMPTY_PAGE_MARKDOWN);
                context.put("title", requestedPage);
                context.put("id", payLoad.getInteger("id", -1));
                context.put("newPage", found ? "no" : "yes");
                context.put("rawContent", rawContent);
                context.put("content", Processor.process(rawContent));
                context.put("timestamp", new Date().toString());

                templateEngine.render(context.data(), "templates/page.ftl", ar -> {
                    if (ar.succeeded()) {
                        context.response().putHeader("Content-Type", "text/html");
                        context.response().end(ar.result());
                    } else {
                        context.fail(ar.cause());
                    }
                });

            } else {
                context.fail(reply.cause());
            }
        });
    }

    private void pageUpdateHandler(RoutingContext context) {
        String title = context.request().getParam("title");

        Handler<AsyncResult<Void>> handler = reply -> {
            if (reply.succeeded()) {
                context.response().setStatusCode(303);
                context.response().putHeader("Location", "/wiki/" + title);
                context.response().end();
            } else {
                context.fail(reply.cause());
            }
        };

        String markdown = context.request().getParam("markdown");
        if ("yes".equals(context.request().getParam("newPage"))) {
            dbService.createPage(title, markdown, handler);
        } else {
            dbService.savePage(Integer.valueOf(context.request().getParam("id")), markdown, handler);
        }
    }

    private void pageCreateHandler(RoutingContext context) {
        String pageName = context.request().getParam("name");
        String location = "/wiki/" + pageName;
        if (pageName == null || pageName.isEmpty()) {
            location = "/";
        }
        context.response().setStatusCode(303);
        context.response().putHeader("Location", location);
        context.response().end();
    }

    private void pageDeletionHandler(RoutingContext context) {
        context.user().isAuthorized("delete", res -> {
            if (res.succeeded() && res.result()) {
                // Original code:
                dbService.deletePage(Integer.valueOf(context.request().getParam("id")), reply -> {
                    if (reply.succeeded()) {
                        context.response().setStatusCode(303);
                        context.response().putHeader("Location", "/");
                        context.response().end();
                    } else {
                        context.fail(reply.cause());
                    }
                });

            } else {
                context.response().setStatusCode(403).end();
            }
        });
    }
    
    //<editor-fold defaultstate="collapsed" desc="Code Step6">
//    private void pageDeletionHandler(RoutingContext context) {
//        dbService.deletePage(Integer.valueOf(context.request().getParam("id")), reply -> {
//            if (reply.succeeded()) {
//                context.response().setStatusCode(303);
//                context.response().putHeader("Location", "/");
//                context.response().end();
//            } else {
//                context.fail(reply.cause());
//            }
//        });
//    }
    //</editor-fold>
    
    private void backupHandler(RoutingContext context) {
        dbService.fetchAllPagesData(reply -> {
            if (reply.succeeded()) {
                JsonArray filesObject = new JsonArray();
                JsonObject payload = new JsonObject()
                        .put("files", filesObject)
                        .put("language", "plaintext")
                        .put("title", "vertx-wiki-backup")
                        .put("public", true);

                reply.result()
                        .forEach(page -> {
                            JsonObject fileObject = new JsonObject();
                            fileObject.put("name", page.getString("NAME"));
                            fileObject.put("content", page.getString("CONTENT"));
                            filesObject.add(fileObject);
                        });

                WebClient webClient = WebClient.create(vertx, new WebClientOptions().setSsl(true).setUserAgent("vert-x3"));
                webClient.post(443, "snippets.glot.io", "/snippets")
                        .putHeader("Content-Type", "application/json")
                        .as(BodyCodec.jsonObject())
                        .sendJsonObject(payload, ar -> {
                            if (ar.succeeded()) {
                                HttpResponse<JsonObject> response = ar.result();
                                if (response.statusCode() == 200) {
                                    String url = "https://glot.io/snippets/" + response.body().getString("id");
                                    context.put("backup_gist_url", url); // https://glot.io/snippets/fgqiqs2j5p
                                    indexHandler(context);
                                } else {
                                    StringBuilder message = new StringBuilder()
                                            .append("Could not backup the wiki: ")
                                            .append(response.statusMessage());
                                    JsonObject body = response.body();
                                    if (body != null) {
                                        message.append(System.getProperty("line.separator"))
                                                .append(body.encodePrettily());
                                    }
                                    LOGGER.error(message.toString());
                                    context.fail(502);
                                }
                            } else {
                                Throwable err = ar.cause();
                                LOGGER.error("HTTP Client error", err);
                                context.fail(err);
                            }
                        });
            } else {
                context.fail(reply.cause());
            }
        });
    }
    
    // API
    private void apiRoot(RoutingContext context) {
        dbService.fetchAllPagesData(reply -> {
            JsonObject response = new JsonObject();
            if (reply.succeeded()) {
                List<JsonObject> pages = reply.result()
                        .stream()
                        .map(obj -> new JsonObject()
                        .put("id", obj.getInteger("ID"))
                        .put("name", obj.getString("NAME")))
                        .collect(Collectors.toList());
                response
                        .put("success", true)
                        .put("pages", pages);
                context.response().setStatusCode(200);
                context.response().putHeader("Content-Type", "application/json");
                context.response().end(response.encode());
            } else {
                response
                        .put("success", false)
                        .put("error", reply.cause().getMessage());
                context.response().setStatusCode(500);
                context.response().putHeader("Content-Type", "application/json");
                context.response().end(response.encode());
            }
        });
    }
    
    private void apiGetPage(RoutingContext context) {
        int id = Integer.valueOf(context.request().getParam("id"));
        dbService.fetchPageById(id, reply -> {
            JsonObject response = new JsonObject();
            if (reply.succeeded()) {
                JsonObject dbObject = reply.result();
                if (dbObject.getBoolean("found")) {
                    JsonObject payload = new JsonObject()
                            .put("name", dbObject.getString("name"))
                            .put("id", dbObject.getInteger("id"))
                            .put("markdown", dbObject.getString("content"))
                            .put("html", Processor.process(dbObject.getString("content")));
                    response
                            .put("success", true)
                            .put("page", payload);
                    context.response().setStatusCode(200);
                } else {
                    context.response().setStatusCode(404);
                    response
                            .put("success", false)
                            .put("error", "There is no page with ID " + id);
                }
            } else {
                response
                        .put("success", false)
                        .put("error", reply.cause().getMessage());
                context.response().setStatusCode(500);
            }
            context.response().putHeader("Content-Type", "application/json");
            context.response().end(response.encode());
        });
    }
    
    private void apiCreatePage(RoutingContext context) {
        JsonObject page = context.getBodyAsJson();
        if (!validateJsonPageDocument(context, page, "name", "markdown")) {
            return;
        }
        dbService.createPage(page.getString("name"), page.getString("markdown"), reply -> {
            if (reply.succeeded()) {
                context.response().setStatusCode(201);
                context.response().putHeader("Content-Type", "application/json");
                context.response().end(new JsonObject().put("success", true).encode());
            } else {
                context.response().setStatusCode(500);
                context.response().putHeader("Content-Type", "application/json");
                context.response().end(new JsonObject()
                        .put("success", false)
                        .put("error", reply.cause().getMessage()).encode());
            }
        });
    }
    
    private boolean validateJsonPageDocument(RoutingContext context, JsonObject page, String... expectedKeys) {
        if (!Arrays.stream(expectedKeys).allMatch(page::containsKey)) {
            LOGGER.error("Bad page creation JSON payload: " + page.encodePrettily() + " from " + context.request().remoteAddress());
            context.response().setStatusCode(400);
            context.response().putHeader("Content-Type", "application/json");
            context.response().end(new JsonObject()
                    .put("success", false)
                    .put("error", "Bad request payload").encode());
            return false;
        }
        return true;
    }
    
    private void apiUpdatePage(RoutingContext context) {
        int id = Integer.valueOf(context.request().getParam("id"));
        JsonObject page = context.getBodyAsJson();
        if (!validateJsonPageDocument(context, page, "markdown")) {
            return;
        }
        dbService.savePage(id, page.getString("markdown"), reply -> {
            handleSimpleDbReply(context, reply);
            if (reply.succeeded()) {
                JsonObject event = new JsonObject()
                        .put("id", id)
                        .put("client", page.getString("client"));
                vertx.eventBus().publish("page.saved", event);
            }
        });
    }
    
    private void handleSimpleDbReply(RoutingContext context, AsyncResult<Void> reply) {
        if (reply.succeeded()) {
            context.response().setStatusCode(200);
            context.response().putHeader("Content-Type", "application/json");
            context.response().end(new JsonObject().put("success", true).encode());
        } else {
            context.response().setStatusCode(500);
            context.response().putHeader("Content-Type", "application/json");
            context.response().end(new JsonObject()
                    .put("success", false)
                    .put("error", reply.cause().getMessage()).encode());
        }
    }
    
//    private void apiDeletePage(RoutingContext context) {
//        if (context.user().principal().getBoolean("canDelete", false)) {
//            int id = Integer.valueOf(context.request().getParam("id"));
//            dbService.deletePage(id, reply -> {
//                handleSimpleDbReply(context, reply);
//            });
//        } else {
//            context.fail(401);
//        }
//    }
    
    //<editor-fold defaultstate="collapsed" desc="Code Step6">
    private void apiDeletePage(RoutingContext context) {
        int id = Integer.valueOf(context.request().getParam("id"));
        dbService.deletePage(id, reply -> {
            handleSimpleDbReply(context, reply);
        });
    }
    //</editor-fold>
    
    
    
    //<editor-fold defaultstate="collapsed" desc="Code Step 3">
//    @Override
//    public void start(Promise<Void> promise) throws Exception {
//        wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");
//
//        HttpServer server = vertx.createHttpServer();
//
//        Router router = Router.router(vertx);
//        router.get("/").handler(this::indexHandler);
//        router.get("/wiki/:page").handler(this::pageRenderingHandler);
//        router.post().handler(BodyHandler.create());
//        router.post("/save").handler(this::pageUpdateHandler);
//        router.post("/create").handler(this::pageCreateHandler);
//        router.post("/delete").handler(this::pageDeletionHandler);
//
//        templateEngine = FreeMarkerTemplateEngine.create(vertx);
//
//        int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
//        server.requestHandler(router)
//                .listen(portNumber, ar -> {
//                    if (ar.succeeded()) {
//                        LOGGER.info("HTTP server running on port " + portNumber);
//                        promise.complete();
//                    } else {
//                        LOGGER.error("Could not start a HTTP server", ar.cause());
//                        promise.fail(ar.cause());
//                    }
//                });
//    }
//    
//    private void indexHandler(RoutingContext context) {
//        DeliveryOptions options = new DeliveryOptions().addHeader("action", "all-pages");
//
//        vertx.eventBus().request(wikiDbQueue, new JsonObject(), options, reply -> {
//            if (reply.succeeded()) {
//                JsonObject body = (JsonObject) reply.result().body();
//                context.put("title", "Wiki home");
//                context.put("pages", body.getJsonArray("pages").getList());
//                templateEngine.render(context.data(), "templates/index.ftl", ar -> {
//                    if (ar.succeeded()) {
//                        context.response().putHeader("Content-Type", "text/html");
//                        context.response().end(ar.result());
//                    } else {
//                        context.fail(ar.cause());
//                    }
//                });
//            } else {
//                context.fail(reply.cause());
//            }
//        });
//    }
//    
//    private static final String EMPTY_PAGE_MARKDOWN
//            = "# A new page\n"
//            + "\n"
//            + "Feel-free to write in Markdown!\n";
//    
//    private void pageRenderingHandler(RoutingContext context) {
//        String requestedPage = context.request().getParam("page");
//        JsonObject request = new JsonObject().put("page", requestedPage);
//
//        DeliveryOptions options = new DeliveryOptions().addHeader("action", "get-page");
//        vertx.eventBus().request(wikiDbQueue, request, options, reply -> {
//            if (reply.succeeded()) {
//                JsonObject body = (JsonObject) reply.result().body();
//
//                boolean found = body.getBoolean("found");
//                String rawContent = body.getString("rawContent", EMPTY_PAGE_MARKDOWN);
//                context.put("title", requestedPage);
//                context.put("id", body.getInteger("id", -1));
//                context.put("newPage", found ? "no" : "yes");
//                context.put("rawContent", rawContent);
//                context.put("content", Processor.process(rawContent));
//                context.put("timestamp", new Date().toString());
//
//                templateEngine.render(context.data(), "templates/page.ftl", ar -> {
//                    if (ar.succeeded()) {
//                        context.response().putHeader("Content-Type", "text/html");
//                        context.response().end(ar.result());
//                    } else {
//                        context.fail(ar.cause());
//                    }
//                });
//
//            } else {
//                context.fail(reply.cause());
//            }
//        });
//    }
//    
//    private void pageCreateHandler(RoutingContext context) {
//        String pageName = context.request().getParam("name");
//        String location = "/wiki/" + pageName;
//        if (pageName == null || pageName.isEmpty()) {
//            location = "/";
//        }
//        context.response().setStatusCode(303);
//        context.response().putHeader("Location", location);
//        context.response().end();
//    }
//    
//    private void pageUpdateHandler(RoutingContext context) {
//        String title = context.request().getParam("title");
//        JsonObject request = new JsonObject()
//                .put("id", context.request().getParam("id"))
//                .put("title", title)
//                .put("markdown", context.request().getParam("markdown"));
//
//        DeliveryOptions options = new DeliveryOptions();
//        if ("yes".equals(context.request().getParam("newPage"))) {
//            options.addHeader("action", "create-page");
//        } else {
//            options.addHeader("action", "save-page");
//        }
//
//        vertx.eventBus().request(wikiDbQueue, request, options, reply -> {
//            if (reply.succeeded()) {
//                context.response().setStatusCode(303);
//                context.response().putHeader("Location", "/wiki/" + title);
//                context.response().end();
//            } else {
//                context.fail(reply.cause());
//            }
//        });
//    }
//    
//    private void pageDeletionHandler(RoutingContext context) {
//        String id = context.request().getParam("id");
//        JsonObject request = new JsonObject().put("id", id);
//        DeliveryOptions options = new DeliveryOptions().addHeader("action", "delete-page");
//        vertx.eventBus().request(wikiDbQueue, request, options, reply -> {
//            if (reply.succeeded()) {
//                context.response().setStatusCode(303);
//                context.response().putHeader("Location", "/");
//                context.response().end();
//            } else {
//                context.fail(reply.cause());
//            }
//        });
//    }
    //</editor-fold>
    
    
}
