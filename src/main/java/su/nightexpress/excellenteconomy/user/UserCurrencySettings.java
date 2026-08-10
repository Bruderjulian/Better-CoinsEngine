package su.nightexpress.excellenteconomy.user;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import su.nightexpress.excellenteconomy.api.currency.Currency;

public class UserCurrencySettings {

    private boolean paymentsEnabled;

    public UserCurrencySettings(boolean paymentsEnabled) {
        this.setPaymentsEnabled(paymentsEnabled);
    }

    public static UserCurrencySettings create(Currency currency) {
        return new UserCurrencySettings(true);
    }

    public boolean isPaymentsEnabled() {
        return paymentsEnabled;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsEnabled = paymentsEnabled;
    }

    public static class CurrencySettingsSerializer
            implements JsonSerializer<UserCurrencySettings>, JsonDeserializer<UserCurrencySettings> {

        @Override
        public UserCurrencySettings deserialize(JsonElement element, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject object = element.getAsJsonObject();

            boolean paymentsEnabled = object.get("paymentsEnabled").getAsBoolean();

            return new UserCurrencySettings(paymentsEnabled);
        }

        @Override
        public JsonElement serialize(UserCurrencySettings data, Type type, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("paymentsEnabled", data.isPaymentsEnabled());
            return object;
        }
    }

}
