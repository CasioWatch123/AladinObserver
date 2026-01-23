package com.github.casiowatch123.aladinobserver.model.offshop.impl.localio;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.ModelPolicies;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.serialization.LocalDateTimeDeserializer;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.serialization.LocalDateTimeSerializer;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class OffshopCheckResultIO implements IO<OffshopCheckResult> {
    private static final Path DIR = ModelPolicies.LOCAL_ROOT_DIR;
    private static final Path FILE_PATH = Path.of(DIR.toString(), "execution history-off shop.txt");

    private static final Gson GSON_PARSER = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();


    static {
        if(!Files.exists(FILE_PATH)) {
            try {
                Files.createFile(FILE_PATH);
            } catch (IOException e) {
                //로그 시스템 구축 실패. 프로그램 종료
                System.err.println(e);
            }
        }
    }
    
    public OffshopCheckResultIO() {}
    
    @Override
    public synchronized HistoryObjectDeque<OffshopCheckResult> readLines(int n) {
        try (ReversedLinesFileReader reader = ReversedLinesFileReader
                .builder()
                .setPath(FILE_PATH)
                .get()) {

            HistoryObjectDeque<OffshopCheckResult> result = new HistoryObjectDeque<>();

            while (result.getDeque().size() < n) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (isValidLine(line)) {
                    Type type = new TypeToken<HistoryObject<OffshopCheckResult>>() {}.getType();
                    result.addHistoryLast(GSON_PARSER.fromJson(line, type));
                }
            }
            return result;
        } catch (IOException e) {
            System.err.println(e);
            Logger.getInstance().writeLog(e);
            return new HistoryObjectDeque<>();
        }
    }

    @Override
    public synchronized void write(HistoryObject<OffshopCheckResult> detail) {
        String str = GSON_PARSER.toJson(detail) + "\n";
        
        try {
            Files.writeString(
                    FILE_PATH,
                    str,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(e);
            Logger.getInstance().writeLog(e);
        }
    }
    
    private static boolean isValidLine(String line) {
        Type type = new TypeToken<HistoryObject<OffshopCheckResult>>() {}.getType();
        try {
            GSON_PARSER.fromJson(line, type);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}
