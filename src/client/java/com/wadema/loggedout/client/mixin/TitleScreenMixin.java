package com.wadema.loggedout.client.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private boolean hasLocationData = false;
    private String[] lines = {
            "No logout location has been detected yet"
    };

    private float scale = 1.0f;

    private Path config(String name) {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(name);
    }

    private void loadLastLocation() {
        Path file = config("loggedout.json");

        if (!Files.exists(file))
            return;

        try {
            JsonObject json = JsonParser.parseString(Files.readString(file))
                    .getAsJsonObject();

            String type = json.get("Type").getAsString();

            String location = type.equals("Singleplayer")
                    ? "World: " + json.get("World").getAsString()
                    : "Address: " + json.get("Address").getAsString();

            lines = new String[] {
                    "Type: " + type,
                    location,
                    "Dimension: " + json.get("Dimension").getAsString(),
                    "X: " + json.get("X").getAsString(),
                    "Y: " + json.get("Y").getAsString(),
                    "Z: " + json.get("Z").getAsString()
            };

            hasLocationData = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        Path file = config("loggedout-config.json");

        if (!Files.exists(file)) {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("scale", 1.0f);

                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();

                Files.writeString(file, gson.toJson(json));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        try {
            JsonObject json = JsonParser.parseString(Files.readString(file))
                    .getAsJsonObject();

            if (json.has("scale")) {
                scale = json.get("scale").getAsFloat();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void loadLocation(CallbackInfo ci) {
        loadLastLocation();
        loadConfig();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onMouseClicked(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!hasLocationData) {
            return;
        }

        double mouseX = event.x();
        double mouseY = event.y();

        float scaledMouseX = (float) (mouseX / scale);
        float scaledMouseY = (float) (mouseY / scale);

        int x = 10;
        int y = 10;
        int padding = 3;

        Minecraft minecraft = Minecraft.getInstance();

        int lineHeight = minecraft.font.lineHeight;

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(
                    maxWidth,
                    minecraft.font.width(line)
            );
        }

        int bottomRightX = x + maxWidth;
        int bottomRightY = y + lines.length * lineHeight;

        boolean isHovered =
                scaledMouseX >= x - padding
                        && scaledMouseX < bottomRightX + padding
                        && scaledMouseY >= y - padding
                        && scaledMouseY < bottomRightY + padding - 2;

        // Left mouse button
        if (isHovered && event.button() == 0) {
            String textToCopy = String.join("\n", Arrays.copyOfRange(lines, 2, lines.length));

            minecraft.keyboardHandler.setClipboard(textToCopy);

            minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            SoundEvents.UI_BUTTON_CLICK,
                            1.0F
                    )
            );
        }
    }

    private void drawText(
            GuiGraphicsExtractor graphics,
            String text,
            int x,
            int y
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                0xFFFFFFFF,
                true
        );
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void renderText(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        int x = 10;
        int y = 10;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        int lineHeight = minecraft.font.lineHeight;

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, minecraft.font.width(line));
        }

        int bottomRightX = x + maxWidth;
        int bottomRightY = y + lines.length * lineHeight;

        int padding = 3;

        // Check if the mouse is hovering over the panel
        float scaledMouseX = mouseX / scale;
        float scaledMouseY = mouseY / scale;

        boolean isHovered = hasLocationData
                && scaledMouseX >= x - padding
                && scaledMouseX < bottomRightX + padding
                && scaledMouseY >= y - padding
                && scaledMouseY < bottomRightY + padding - 2;

        // Set background color based on hover
        int backgroundColor = 0x80000000;

        if (isHovered) {
            backgroundColor = 0xA0202020;
        }

        // Render background
        graphics.fill(
                x - padding,
                y - padding,
                bottomRightX + padding,
                bottomRightY + padding - 2,
                backgroundColor
        );

        // Render text
        for (int i = 0; i < lines.length; i++) {
            drawText(
                    graphics,
                    lines[i],
                    x,
                    y + i * lineHeight
            );
        }

        graphics.pose().popMatrix();
    }
}