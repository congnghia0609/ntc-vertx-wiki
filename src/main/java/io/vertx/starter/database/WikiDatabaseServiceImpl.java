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

package io.vertx.starter.database;

//import io.reactivex.Flowable;
//import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
//import io.vertx.reactivex.SingleHelper;
//import io.vertx.reactivex.ext.jdbc.JDBCClient;
//import io.vertx.reactivex.ext.sql.SQLClientHelper;
import io.vertx.starter.database.WikiDatabaseVerticle.SqlQuery;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Oct 9, 2019
 */
public class WikiDatabaseServiceImpl implements WikiDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikiDatabaseServiceImpl.class);

    private final HashMap<SqlQuery, String> sqlQueries;
    private final JDBCClient dbClient;

    WikiDatabaseServiceImpl(JDBCClient dbClient, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<WikiDatabaseService>> readyHandler) {
        this.dbClient = dbClient;
        this.sqlQueries = sqlQueries;

//        SQLClientHelper.usingConnectionSingle(this.dbClient, conn -> conn
//                .rxExecute(sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE))
//                .andThen(Single.just(this))
//        );
        
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                readyHandler.handle(Future.failedFuture(ar.cause()));
            } else {
                SQLConnection connection = ar.result();
                connection.execute(sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE), create -> {
                    connection.close();
                    if (create.failed()) {
                        LOGGER.error("Database preparation error", create.cause());
                        readyHandler.handle(Future.failedFuture(create.cause()));
                    } else {
                        readyHandler.handle(Future.succeededFuture(this));
                    }
                });
            }
        });
    }
    
    //<editor-fold defaultstate="collapsed" desc="Code Step8">
//    @Override
//    public WikiDatabaseService fetchAllPages(Handler<AsyncResult<JsonArray>> resultHandler) {
//        dbClient.rxQuery(sqlQueries.get(SqlQuery.ALL_PAGES))
//                .flatMapPublisher(res -> {
//                    List<JsonArray> results = res.getResults();
//                    return Flowable.fromIterable(results);
//                })
//                .map(json -> json.getString(0))
//                .sorted()
//                .collect(JsonArray::new, JsonArray::add)
//                .subscribe(SingleHelper.toObserver(resultHandler));
//        return this;
//    }
    //</editor-fold>
    
    @Override
    public WikiDatabaseService fetchAllPages(Handler<AsyncResult<JsonArray>> resultHandler) {
        dbClient.query(sqlQueries.get(SqlQuery.ALL_PAGES), res -> {
            if (res.succeeded()) {
                JsonArray pages = new JsonArray(res.result()
                        .getResults()
                        .stream()
                        .map(json -> json.getString(0))
                        .sorted()
                        .collect(Collectors.toList()));
                resultHandler.handle(Future.succeededFuture(pages));
            } else {
                LOGGER.error("Database query error", res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public WikiDatabaseService fetchPage(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
        dbClient.queryWithParams(sqlQueries.get(SqlQuery.GET_PAGE), new JsonArray().add(name), fetch -> {
            if (fetch.succeeded()) {
                JsonObject response = new JsonObject();
                ResultSet resultSet = fetch.result();
                if (resultSet.getNumRows() == 0) {
                    response.put("found", false);
                } else {
                    response.put("found", true);
                    JsonArray row = resultSet.getResults().get(0);
                    response.put("id", row.getInteger(0));
                    response.put("rawContent", row.getString(1));
                }
                resultHandler.handle(Future.succeededFuture(response));
            } else {
                LOGGER.error("Database query error", fetch.cause());
                resultHandler.handle(Future.failedFuture(fetch.cause()));
            }
        });
        return this;
    }

    @Override
    public WikiDatabaseService createPage(String title, String markdown, Handler<AsyncResult<Void>> resultHandler) {
        JsonArray data = new JsonArray().add(title).add(markdown);
        dbClient.updateWithParams(sqlQueries.get(SqlQuery.CREATE_PAGE), data, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public WikiDatabaseService savePage(int id, String markdown, Handler<AsyncResult<Void>> resultHandler) {
        JsonArray data = new JsonArray().add(markdown).add(id);
        dbClient.updateWithParams(sqlQueries.get(SqlQuery.SAVE_PAGE), data, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public WikiDatabaseService deletePage(int id, Handler<AsyncResult<Void>> resultHandler) {
        JsonArray data = new JsonArray().add(id);
        dbClient.updateWithParams(sqlQueries.get(SqlQuery.DELETE_PAGE), data, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Code Step8">
//    @Override
//    public WikiDatabaseService fetchAllPagesData(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
//        dbClient.rxQuery(sqlQueries.get(SqlQuery.ALL_PAGES_DATA))
//                .map(ResultSet::getRows)
//                .subscribe(SingleHelper.toObserver(resultHandler));
//        return this;
//    }
    //</editor-fold>
    
    @Override
    public WikiDatabaseService fetchAllPagesData(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        dbClient.query(sqlQueries.get(SqlQuery.ALL_PAGES_DATA), queryResult -> {
            if (queryResult.succeeded()) {
                resultHandler.handle(Future.succeededFuture(queryResult.result().getRows()));
            } else {
                LOGGER.error("Database query error", queryResult.cause());
                resultHandler.handle(Future.failedFuture(queryResult.cause()));
            }
        });
        return this;
    }
    
    @Override
    public WikiDatabaseService fetchPageById(int id, Handler<AsyncResult<JsonObject>> resultHandler) {
        dbClient.queryWithParams(sqlQueries.get(SqlQuery.GET_PAGE_BY_ID), new JsonArray().add(id), fetch -> {
            if (fetch.succeeded()) {
                JsonObject response = new JsonObject();
                ResultSet resultSet = fetch.result();
                if (resultSet.getNumRows() == 0) {
                    response.put("found", false);
                } else {
                    response.put("found", true);
                    JsonArray row = resultSet.getResults().get(0);
                    response.put("id", row.getInteger(0));
                    response.put("name", row.getString(1));
                    response.put("content", row.getString(2));
                }
                resultHandler.handle(Future.succeededFuture(response));
            } else {
                LOGGER.error("Database query error", fetch.cause());
                resultHandler.handle(Future.failedFuture(fetch.cause()));
            }
        });
        return this;
    }

}
