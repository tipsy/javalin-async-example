package app;

import io.javalin.Javalin;
import java.util.concurrent.CompletableFuture;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

public class Main {

    public static void main(String[] args) {

        AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();

        Javalin app = Javalin.create()
            .embeddedServer(ServerUtil.createHttp2Server())
            .enableStaticFiles("/public")
            .start();

        app.get("/proxy-request-async", ctx -> {
            ctx.result(getStringCompletableFuture(asyncHttpClient, ctx.queryParam("url")));
        });

        app.get("/proxy-request-blocking", ctx -> {
            CompletableFuture<String> resultFuture = getStringCompletableFuture(asyncHttpClient, ctx.queryParam("url"));
            String result = resultFuture.get(); // block
            ctx.result(result);
        });

    }

    private static CompletableFuture<String> getStringCompletableFuture(AsyncHttpClient asyncHttpClient, String url) {
        return asyncHttpClient
            .prepareGet(url)
            .execute()
            .toCompletableFuture()
            .thenApply(Response::getResponseBody);
    }

}
