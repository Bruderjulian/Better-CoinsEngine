package su.nightexpress.excellenteconomy.data.serialize;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import su.nightexpress.excellenteconomy.data.impl.CurrencySettings;

public class CurrencySettingsSerializer
        implements JsonSerializer<CurrencySettings>, JsonDeserializer<CurrencySettings> {

    @Override
    public CurrencySettings deserialize(JsonElement element, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        boolean paymentsEnabled = object.get("paymentsEnabled").getAsBoolean();

        return new CurrencySettings(paymentsEnabled);
    }

    @Override
    public JsonElement serialize(CurrencySettings data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("paymentsEnabled", data.isPaymentsEnabled());
        return object;
    }
}
