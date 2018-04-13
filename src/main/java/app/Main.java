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
            .embeddedServer(ServerUtil.createHttp2Server(new QueuedThreadPool(10, 2, 60_000)))
            .enableStaticFiles("/public")
            .start();

        app.get("/request-async", ctx -> {
            long taskTime = Long.parseLong(ctx.queryParam("task-time"));
            ctx.result(getFuture(taskTime));
        });

        app.get("/request-blocking", ctx -> {
            long taskTime = Long.parseLong(ctx.queryParam("task-time"));
            Thread.sleep(taskTime);
            ctx.result("done");
        });

    }

    private static CompletableFuture<String> getFuture(long taskTime) {
        CompletableFuture<String> future = new CompletableFuture<>();
        executorService.scheduleWithFixedDelay(() -> future.complete("done"), taskTime, 1, TimeUnit.MILLISECONDS);
        return future;
    }

}
