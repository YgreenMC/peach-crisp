package net.ygreenmc.mods.peachcrisp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeachCrisp implements ModInitializer {

    public static final String MOD_ID = "peach-crisp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item PEACH_CRISP = new Item(new Item.Properties()
        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "peach_crisp")))
        .food(
            new FoodProperties(4, 0.6f, false),
            Consumable.builder()
                .consumeSeconds(1.6f)
                .animation(ItemUseAnimation.EAT)
                .sound(SoundEvents.GENERIC_EAT)
                .hasConsumeParticles(true)
                .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.REGENERATION, 60, 1), 1.0f
                ))
                .build()
        )
    );

    public static final Item GOLDEN_PEACH_CRISP = new Item(new Item.Properties()
        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "golden_peach_crisp")))
        .food(
            new FoodProperties(8, 12.0f, true),
            Consumable.builder()
                .consumeSeconds(1.6f)
                .animation(ItemUseAnimation.EAT)
                .sound(SoundEvents.GENERIC_EAT)
                .hasConsumeParticles(true)
                .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.HASTE, 3600, 1), 1.0f
                ))
                .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0), 1.0f
                ))
                .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0f
                ))
                .build()
        )
    );

    public static void initialize() {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "peach_crisp"), PEACH_CRISP);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "golden_peach_crisp"), GOLDEN_PEACH_CRISP);
    }

    @Override
    public void onInitialize() {
        initialize();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(
            creativeTab -> {
                creativeTab.accept(PEACH_CRISP);
                creativeTab.accept(GOLDEN_PEACH_CRISP);
            }
        );

        ResourceLoader.registerBuiltinPack(
            Identifier.fromNamespaceAndPath(MOD_ID, "peach_crisp_trades"),
            FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
            PackActivationType.ALWAYS_ENABLED
        );

        registerPeachCrispLoot();

        LOGGER.info("Peach Crisp mod initialized!");
    }

    private static void registerPeachCrispLoot() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, holder) -> {
            if (source != LootTableSource.VANILLA) {
                return;
            }

            switch (key.identifier().getPath()) {
                case "chests/simple_dungeon" -> addPeachCrisp(tableBuilder, 0, 10, 1, 2);
                case "chests/ancient_city" -> addPeachCrisp(tableBuilder, 0, 5, 2, 4);
                case "chests/abandoned_mineshaft" -> addPeachCrisp(tableBuilder, 1, 10, 1, 2);
                case "chests/desert_pyramid" -> addPeachCrisp(tableBuilder, 0, 15, 1, 3);
                case "chests/village/village_desert_house" -> addPeachCrisp(tableBuilder, 0, 8, 2, 3);
                case "chests/village/village_plains_house" -> addPeachCrisp(tableBuilder, 0, 8, 2, 3);
                case "chests/village/village_savanna_house" -> addPeachCrisp(tableBuilder, 0, 8, 2, 3);
                case "chests/village/village_snowy_house" -> addPeachCrisp(tableBuilder, 0, 8, 2, 3);
                case "chests/village/village_taiga_house" -> addPeachCrisp(tableBuilder, 0, 8, 2, 3);
                case "chests/village/village_temple" -> addPeachCrisp(tableBuilder, 0, 5, 2, 3);
                case "chests/ruined_portal" -> addGoldenPeachCrisp(tableBuilder, 0, 15);
                default -> {
                }
            }
        });
    }

    private static void addPeachCrisp(LootTable.Builder tableBuilder, int poolIndex, int weight, float minCount, float maxCount) {
        addItem(tableBuilder, poolIndex, weight, minCount, maxCount, PEACH_CRISP);
    }

    private static void addGoldenPeachCrisp(LootTable.Builder tableBuilder, int poolIndex, int weight) {
        addItem(tableBuilder, poolIndex, weight, 1.0f, 1.0f, GOLDEN_PEACH_CRISP);
    }

    private static void addItem(LootTable.Builder tableBuilder, int poolIndex, int weight, float minCount, float maxCount, Item item) {
        int[] index = {0};
        ((FabricLootTableBuilder) tableBuilder).modifyPools(poolBuilder -> {
            if (index[0] == poolIndex) {
                poolBuilder.add(
                    LootItem.lootTableItem(item)
                        .setWeight(weight)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount), false))
                );
            }
            index[0]++;
        });
    }
}
