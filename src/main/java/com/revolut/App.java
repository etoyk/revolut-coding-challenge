package com.revolut;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import com.revolut.db.InMemoryDB;
import com.revolut.repo.impl.InMemoryAccountRepo;
import com.revolut.repo.impl.InMemoryTransactionHelper;
import com.revolut.repo.impl.InMemoryTransactionRepo;
import com.revolut.service.AccountService;
import com.revolut.service.Lock;
import com.revolut.service.TransactionService;
import com.revolut.service.TransferService;
import com.revolut.web.handler.AccountHandler;
import com.revolut.web.handler.ExceptionHandler;
import com.revolut.web.handler.TransactionHandler;
import com.revolut.web.reqresp.resp.ErrorResponse;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  public static void main(String args[]) {
    var db = new InMemoryDB();
    var lock = new Lock();
    var accountRepo = new InMemoryAccountRepo(db);
    var transactionHelper = new InMemoryTransactionHelper(db);
    var transactionRepo = new InMemoryTransactionRepo(db);
    var transactionService = new TransactionService(transactionRepo);
    var accountService = new AccountService(accountRepo, lock, transactionHelper,
        transactionService);
    var transferService = new TransferService(accountRepo, lock, transactionHelper,
        transactionService);
    var accountHandler = new AccountHandler(accountService, transferService);
    var transactionHandler = new TransactionHandler(transactionService);
    var exceptionHandler = new ExceptionHandler();

    Javalin.create(config -> {
      config.registerPlugin(getConfiguredOpenApiPlugin());
      config.defaultContentType = "application/json";
    }).routes(() -> {
      path("accounts", () -> {
        path("transfer", () -> path("from", () ->
            path(":fromId", () ->
                path("to", () ->
                    path(":toId", () ->
                        patch(accountHandler::transfer))))));
        path("deposit", () ->
            path(":id", () ->
                patch(accountHandler::deposit)));
        path("withdraw", () ->
            path(":id", () ->
                patch(accountHandler::withdraw)));
        get(accountHandler::getAll);
        post(accountHandler::create);
        path(":id", () -> {
          get(accountHandler::getOne);
          put(accountHandler::update);
          delete(accountHandler::delete);
        });
      });
      path("transactions", () -> {
        get(transactionHandler::getAll);
        post(transactionHandler::generateTxId);
      });
    }).exception(RuntimeException.class, (e, context) -> exceptionHandler.handle(e, context))
        .start(7002);

    System.out.println("Check out Swagger UI docs at http://localhost:7002/swagger-ui");
  }

  private static OpenApiPlugin getConfiguredOpenApiPlugin() {
    Info info = new Info().version("1.0").description("Accounts API");
    OpenApiOptions options = new OpenApiOptions(info)
        .activateAnnotationScanningFor("com.revolut.challenge")
        .path("/swagger-docs")
        .swagger(new SwaggerOptions("/swagger-ui"))
        .reDoc(new ReDocOptions("/redoc"))
        .defaultDocumentation(doc -> {
          doc.json("500", ErrorResponse.class);
          doc.json("503", ErrorResponse.class);
        });
    return new OpenApiPlugin(options);
  }
}
