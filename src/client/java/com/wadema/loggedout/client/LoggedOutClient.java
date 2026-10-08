package com.wadema.loggedout.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoggedOutClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (client.player != null) {

				// Get variables
				String type;
				String worldName = "Unknown";
				String serverName = "Unknown";
				String serverAddress = "Unknown";

				if (client.isLocalServer()) {
					type = "Singleplayer";

					if (client.getSingleplayerServer() != null) {
						worldName = client.getSingleplayerServer()
								.getWorldData()
								.getLevelName();
					}
				} else {
					type = "Multiplayer";

					ServerData server = client.getCurrentServer();

					if (server != null) {
						serverName = server.name;
						serverAddress = server.ip;
					}
				}

				String dimension = getDimensionName(client.player.level().dimension());

				double x = client.player.getX();
				double y = client.player.getY();
				double z = client.player.getZ();

				// Write to JSON
				Path file = FabricLoader.getInstance()
						.getConfigDir()
						.resolve("loggedout.json");

				JsonObject json = new JsonObject();

				json.addProperty("Type", type);
				json.addProperty("WorldName", worldName);
				json.addProperty("ServerName", serverName);
				json.addProperty("ServerAddress", serverAddress);
				json.addProperty("Dimension", dimension);
				json.addProperty("X", Math.round(x * 100.0) / 100.0);
				json.addProperty("Y", Math.round(y * 100.0) / 100.0);
				json.addProperty("Z", Math.round(z * 100.0) / 100.0);

				Gson gson = new GsonBuilder()
						.setPrettyPrinting()
						.create();

				String data = gson.toJson(json);

				try {
					Files.writeString(file, data);

				} catch (IOException e) {
					System.err.println("Failed to save coordinates!");
					e.printStackTrace();
				}
			}
		});
	}

	private static String getDimensionName(ResourceKey<Level> dimension) {
		if (dimension.equals(Level.OVERWORLD)) {
			return "Overworld";
		}

		if (dimension.equals(Level.NETHER)) {
			return "Nether";
		}

		if (dimension.equals(Level.END)) {
			return "The End";
		}

		return dimension.identifier().toString();
	}
}
