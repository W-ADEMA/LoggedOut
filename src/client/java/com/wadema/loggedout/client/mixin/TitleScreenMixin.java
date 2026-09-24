package com.wadema.loggedout.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.Minecraft;

import net.fabricmc.loader.api.FabricLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private String type;
    private String world;
    private String address;
    private String dimension;
    private String x;
    private String y;
    private String z;

    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private String line5;
    private String line6;
    private String[] lines;

    private boolean hasLocationData = false;

    private int textWidth;

    private void loadLastLocation() {
        Path file = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("loggedout.json");

        if (!Files.exists(file)) {
            lines = new String[] {
                    "No logout location has been detected yet"
            };

            Minecraft minecraft = Minecraft.getInstance();
            textWidth = minecraft.font.width(lines[0]);

            return;
        }

        try {
            String jsonString = Files.readString(file);
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

            type = json.get("Type").getAsString();

            if ("Singleplayer".equals(type)) {
                world = json.get("World").getAsString();
            } else {
                address = json.get("Address").getAsString();
            }

            dimension = json.get("Dimension").getAsString();
            x = json.get("X").getAsString();
            y = json.get("Y").getAsString();
            z = json.get("Z").getAsString();

            hasLocationData = true;

            Minecraft minecraft = Minecraft.getInstance();

            line1 = "Type: " + type;

            if ("Singleplayer".equals(type)) {
                line2 = "World: " + world;
            } else {
                line2 = "Address: " + address;
            }

            line3 = "Dimension: " + dimension;
            line4 = "X: " + x;
            line5 = "Y: " + y;
            line6 = "Z: " + z;

            lines = new String[] {
                    line1,
                    line2,
                    line3,
                    line4,
                    line5,
                    line6
            };

            int tempTextWidth = minecraft.font.width(line1);

            tempTextWidth = Math.max(tempTextWidth, minecraft.font.width(line2));
            tempTextWidth = Math.max(tempTextWidth, minecraft.font.width(line3));
            tempTextWidth = Math.max(tempTextWidth, minecraft.font.width(line4));
            tempTextWidth = Math.max(tempTextWidth, minecraft.font.width(line5));
            tempTextWidth = Math.max(tempTextWidth, minecraft.font.width(line6));

            textWidth = tempTextWidth;

        } catch (IOException e) {
            e.printStackTrace();
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

    @Inject(method = "init", at = @At("TAIL"))
    private void loadLocation(CallbackInfo ci) {
        loadLastLocation();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Only respond to left mouse button
        if (event.button() != 1) {
            return;
        }

        if (line4 == null || line5 == null || line6 == null) {
            return;
        }

        int panelX = 5;
        int panelY = 5;

        int padding = 5;
        int lineHeight = 12;
        int lineCount = hasLocationData ? 6 : 1;

        int panelWidth = textWidth + padding * 2;
        int panelHeight = padding + lineCount * lineHeight;

        double mouseX = event.x();
        double mouseY = event.y();

        // Check whether the mouse is inside the panel
        if (mouseX >= panelX
                && mouseX < panelX + panelWidth
                && mouseY >= panelY
                && mouseY < panelY + panelHeight) {

            Minecraft minecraft = Minecraft.getInstance();

            minecraft.keyboardHandler.setClipboard(line3 + " \n" + line4 + " \n" + line5 + " \n" + line6);

            minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            SoundEvents.UI_BUTTON_CLICK,
                            1.0F
                    )
            );

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void renderloggedoutValues(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        int panelX = 5;
        int panelY = 5;

        int padding = 5;
        int lineHeight = 12;
        int lineCount = hasLocationData ? 6 : 1;

        int panelWidth = textWidth + padding * 2;
        int panelHeight = padding + lineCount * lineHeight;

        // Check if the mouse is hovering over the panel
        boolean isHovered = hasLocationData
                && mouseX >= panelX
                && mouseX < panelX + panelWidth
                && mouseY >= panelY
                && mouseY < panelY + panelHeight;


        int backgroundColor = 0x80000000;

        if (isHovered) {
            backgroundColor = 0xA0202020;
        }

        // Render background
        graphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                backgroundColor
        );

        // Render top border
        graphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + 1,
                0x80FFFFFF
        );

        // Render bottom border
        graphics.fill(
                panelX,
                panelY + panelHeight - 1,
                panelX + panelWidth,
                panelY + panelHeight,
                0x80FFFFFF
        );

        // Render left border
        graphics.fill(
                panelX,
                panelY,
                panelX + 1,
                panelY + panelHeight,
                0x80FFFFFF
        );

        // Render right border
        graphics.fill(
                panelX + panelWidth - 1,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                0x80FFFFFF
        );

        // Render text
        for (int i = 0; i < lines.length; i++) {
            drawText(
                    graphics,
                    lines[i],
                    panelX + padding,
                    panelY + padding + lineHeight * i
            );
        }
    }
}