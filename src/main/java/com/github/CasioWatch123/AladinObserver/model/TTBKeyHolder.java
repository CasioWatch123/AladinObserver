package com.github.CasioWatch123.AladinObserver.model;

import com.github.CasioWatch123.AladinObserver.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TTBKeyHolder implements TTBKeyService{
    private static Path DIR = ModelPolicies.LOCAL_REPO_DIR;
    private static Path FILE_PATH = Path.of(DIR.toString(), "TTBKey.txt");
    
    private static TTBKeyHolder instance = new TTBKeyHolder();
    
    private String TTBKey;
    
    static {
        if(!Files.exists(FILE_PATH)) {
            try {
                Files.createFile(FILE_PATH);
            } catch (IOException e) {
                System.err.println( e);
            }
        }
    }
    
    private TTBKeyHolder() {
        try {
            this.TTBKey = Files.readString(FILE_PATH);
        } catch (IOException e){
            this.TTBKey = "";
            Logger.getInstance().writeLog(e);
        }
    }
    
    public void setTTBKey(String newTTBKey) {
        this.TTBKey = newTTBKey;
        synchronized (this) {
            try {
                Files.writeString(FILE_PATH, newTTBKey, StandardOpenOption.WRITE);
            } catch (IOException e) {
                Logger.getInstance().writeLog(e);
            }
        }
    }
    @Override
    public String getTTBKey() {
        return TTBKey;
    }
    
    public static TTBKeyHolder getInstance() {
        return instance;
    }
}
