package com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.serialization;

import com.github.casiowatch123.aladinobserver.model.ModelPolicies;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(ModelPolicies.DEFAULT_FORMATTER)); // ISO-8601 문자열
    }
}