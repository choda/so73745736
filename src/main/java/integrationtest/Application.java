package integrationtest;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;

import org.aopalliance.aop.Advice;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public IntegrationFlow myFlow() {
        return IntegrationFlows
                .from(Files.inboundAdapter(new File("./")).patternFilter("dummy.txt")
                           .autoCreateDirectory(true).recursive(false)
                           .useWatchService(true).watchEvents(FileReadingMessageSource.WatchEventType.CREATE))
                .handle(Files.outboundGateway(new File("./out"))
                             .deleteSourceFiles(true)
                             .fileExistsMode(FileExistsMode.REPLACE)
                             .autoCreateDirectory(false),
                        c -> c.advice(advice())
                )
                .<Object, Boolean>transform(p -> !Boolean.FALSE.equals(p))
                .log(LoggingHandler.Level.INFO, m -> m.getHeaders().getId() + ": " + m.getPayload())
                .get();
    }
    
    @Bean
    public Advice advice() {
        var advice = new ExpressionEvaluatingRequestHandlerAdvice();
        advice.setOnFailureExpressionString("false");

        return advice;
    }
    
}
