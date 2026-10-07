package com.wadema.loggedout.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.wadema.loggedout.LoggedOutConfig;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;

import net.minecraft.network.chat.Component;

public class LoggedOutModMenu implements ModMenuApi {

    private static final LoggedOutConfig CONFIG = new LoggedOutConfig();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        CONFIG.load();

        return parent -> YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Logged Out"))

            .category(
                ConfigCategory.createBuilder()
                    .name(Component.literal("General"))

                    // Scale
                    .option(
                        Option.<Float>createBuilder()
                            .name(Component.literal("Scale"))
                            .description(
                                OptionDescription.createBuilder()
                                    .text(Component.literal("Changes the scale of the info box and its contents."))
                                    .build()
                            )
                            .binding(
                                1.0f,
                                () -> CONFIG.scale,
                                value -> CONFIG.scale = value
                            )
                            .controller(option ->
                                FloatSliderControllerBuilder
                                    .create(option)
                                    .range(0.25f, 3.0f)
                                    .step(0.05f)
                            )
                            .build()
                    )

                    // Hide coords
                        .option(
                            Option.<Boolean>createBuilder()
                                .name(Component.literal("Hide Coords"))
                                .description(
                                    OptionDescription.createBuilder()
                                        .text(Component.literal("You can still click on the info box to copy the coordinates."))
                                        .build()
                                )
                                .binding(
                                    false,
                                    () -> CONFIG.hideCoords,
                                    value -> CONFIG.hideCoords = value
                                )
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        )

                    .build()
            )

            .save(CONFIG::save)

            .build()
            .generateScreen(parent);
    }
}