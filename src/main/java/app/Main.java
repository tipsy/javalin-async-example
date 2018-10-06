package app;

import io.javalin.Javalin;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class Main {

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {

        Javalin app = Javalin.create()
            .server(() -> ServerUtil.createHttp2Server(new QueuedThreadPool(10, 2, 60_000)))
            .enableStaticFiles("/public")
            .start();

        app.get("/async", ctx -> {
            long taskTime = ctx.validatedQueryParam("task-time").asLong().getOrThrow();
            ctx.result(getFuture(taskTime));
        });

        app.get("/blocking", ctx -> {
            long taskTime = ctx.validatedQueryParam("task-time").asLong().getOrThrow();
            Thread.sleep(taskTime);
            ctx.result("done");
        });

    }

    private static CompletableFuture<String> getFuture(long taskTime) {
        CompletableFuture<String> future = new CompletableFuture<>();
        executorService.schedule(() -> future.complete("done"), taskTime, TimeUnit.MILLISECONDS);
        return future;
    }

}
