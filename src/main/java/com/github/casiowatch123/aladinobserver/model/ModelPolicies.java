package com.github.casiowatch123.aladinobserver.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public final class ModelPolicies {
    public static final int TIMEOUT_SEC = 5;
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public static final Path LOCAL_ROOT_DIR = Path.of(System.getenv("APPDATA"), "AladinObserver");
    
    static {
        if (!Files.exists(LOCAL_ROOT_DIR)) {
            try {
                Files.createDirectories(LOCAL_ROOT_DIR);
            } catch (IOException e) {
                //로그 시스템 구축 실패. 프로그램 종료
                System.err.println(e);
            }
        }
    }
    private ModelPolicies() {}
}