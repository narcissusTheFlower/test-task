package com.example.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class CrptApi {

    /**
     * Лимит запросов
     */
    private static final int REQUEST_LIMIT = 5;

    /**
     * За промежуток времени
     */
    private static final long TIME_UNIT = TimeUnit.MILLISECONDS.convert(Duration.ofMillis(10000));

    private static AtomicInteger currentRequests;

    @PostConstruct
    private void reset(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("reset");
                currentRequests = new AtomicInteger(0);
            }
        };
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(timerTask,0,TIME_UNIT);
    }

    public static void main(String[] args) {
        SpringApplication.run(CrptApi.class, args);
    }

    private Document buildDoc(String doc, String sig){
        return new Document(doc,sig);
    }

    @Controller
    @RequestMapping("/api/v3/lk/documents/")
    class MainController {

        @PostMapping("create")
        public ResponseEntity<?> document(@RequestBody String str) {
            if (currentRequests.get() < REQUEST_LIMIT){
                currentRequests.incrementAndGet();
                System.out.println(currentRequests.get());
                Document doc = buildDoc(str,"Signature");
                return ResponseEntity.ok(doc.toString());
            }
            System.out.println("API request limit exceeded!");
            return ResponseEntity.status(429).body("Too many requests!");
        }

    }

    class Document{

        String doc;
        String sig;

        public Document(String doc, String sig) {
            this.doc = doc;
            this.sig = sig;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                .append("Document is: ")
                .append(doc)
                .append("\n")
                .append("Signature is: ")
                .append(sig).toString();
        }
    }

}
