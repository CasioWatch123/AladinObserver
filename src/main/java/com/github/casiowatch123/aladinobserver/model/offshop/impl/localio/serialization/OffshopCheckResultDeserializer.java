package com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.serialization;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

public class OffshopCheckResultDeserializer
        implements JsonDeserializer<OffshopCheckResult> {

    @Override
    public OffshopCheckResult deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        String itemId = obj.get("itemId").getAsString();

        LocalDateTime timestamp =
                context.deserialize(obj.get("timestamp"), LocalDateTime.class);

        JsonElement listElem = obj.get("offshopList");
        
        List<String> offshopList =
                (listElem == null || listElem.isJsonNull())
                        ? OffshopCheckResult.EMPTY_ARRAY
                        : context.deserialize(
                        listElem,
                        new TypeToken<List<String>>() {}.getType()
                );

        return new OffshopCheckResult(itemId, offshopList, timestamp);
    }
}
