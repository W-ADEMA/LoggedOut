package com.wadema.loggedout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoggedOutConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("loggedout-config.json");

    public boolean hideInfoBox = false;
    public float scale = 1.0f;
    public boolean hideCoords = false;
    public boolean showServerIP = false;

    public void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }

        try {
            JsonObject json = JsonParser
                    .parseString(Files.readString(FILE))
                    .getAsJsonObject();

            if (json.has("hideInfoBox")) {
                hideInfoBox = json.get("hideInfoBox").getAsBoolean();
            }

            if (json.has("scale")) {
                scale = json.get("scale").getAsFloat();
            }

            if (json.has("hideCoords")) {
                hideCoords = json.get("hideCoords").getAsBoolean();
            }

            if (json.has("showServerIP")) {
                showServerIP = json.get("showServerIP").getAsBoolean();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("hideInfoBox", hideInfoBox);
            json.addProperty("scale", scale);
            json.addProperty("hideCoords", hideCoords);
            json.addProperty("showServerIP", showServerIP);

            Files.writeString(FILE, GSON.toJson(json));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}