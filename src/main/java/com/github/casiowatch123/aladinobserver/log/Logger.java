package com.github.casiowatch123.aladinobserver.log;

import com.github.casiowatch123.aladinobserver.model.ModelPolicies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class Logger {
    private static final Path dir = Path.of(System.getenv("LOCALAPPDATA"), "AladinObserver");
    private static final Path logFilePath = Path.of(System.getenv("LOCALAPPDATA"), "AladinObserver", "log.txt");
    
    private static final Logger logger = new Logger();

    static {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                //로그 시스템 구축 실패. 프로그램 종료
                System.err.println(e);
            }
        }

        if(!Files.exists(logFilePath)) {
            try {
                Files.createFile(logFilePath);
            } catch (IOException e) {
                //로그 시스템 구축 실패. 프로그램 종료
                System.err.println( e);
            }
        }
    }
    
    public static Logger getInstance() {
        return logger;
    }

    public synchronized void writeLog(String str) {
        String log = String.format("%s %s\n", now(), str);
        try {
            Files.writeString(
                    logFilePath,
                    log,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            //로그 작성 실패. 프로그램 종료
            System.err.println(e);
        }
    }
    
    public synchronized void writeLog(Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String detail = String.format("EXCEPTION locate: %s : %s, detail: %s"
                , stackTraceElements[0].getClassName()
                , stackTraceElements[0].getMethodName()
                , e.toString());
        writeLog(detail);
    }

    public synchronized void writeLog(Throwable e, String message) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String detail = String.format("EXCEPTION locate: %s : %s, detail: %s"
                , stackTraceElements[0].getClassName()
                , stackTraceElements[0].getMethodName()
                , e.toString());
        writeLog(detail + " message: " + message);
    }
    
    private static String now() {
        return LocalDateTime
                .now()
                .withNano(0)
                .format(ModelPolicies.DEFAULT_FORMATTER);
    }
    
    private Logger() {};
}
