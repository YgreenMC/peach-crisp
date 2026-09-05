package net.ygreenmc.mods.peachcrisp.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import org.jspecify.annotations.NullMarked;

import net.ygreenmc.mods.peachcrisp.PeachCrisp;

@NullMarked
public class PeachCrispDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModAdvancementProvider::new);
        pack.addProvider(ModItemTagProvider::new);

        FabricDataGenerator.Pack tradesPack = fabricDataGenerator.createBuiltinResourcePack(
            Identifier.fromNamespaceAndPath(PeachCrisp.MOD_ID, "peach_crisp_trades"));
        tradesPack.addProvider(PeachCrispPackMetadataProvider::new);
        tradesPack.addProvider(VillagerTradeProvider::new);
        tradesPack.addProvider(VillagerTradeTagProvider::new);
        tradesPack.addProvider(TradeSetProvider::new);
    }

    private static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(PeachCrisp.MOD_ID, path);
    }

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, modId(path));
    }

    private static final class ModRecipeProvider extends FabricRecipeProvider {

        private ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeProvider(registries, output) {
                @Override
                public void buildRecipes() {
                    shapedPeachCrisp(output);
                    shapedGoldenPeachCrisp(output);
                }
            };
        }

        @Override
        public String getName() {
            return "Peach Crisp Recipes";
        }

        private static void shapedPeachCrisp(RecipeOutput output) {
            ResourceKey<Recipe<?>> key = recipeKey("peach_crisp");

            Map<Character, Ingredient> keyed = new LinkedHashMap<>();
            keyed.put('#', Ingredient.of(Items.PINK_PETALS));
            keyed.put('X', Ingredient.of(Items.COOKIE));

            ShapedRecipe recipe = new ShapedRecipe(
                RecipeBuilder.createCraftingCommonInfo(true),
                RecipeBuilder.createCraftingBookInfo(RecipeCategory.FOOD, null),
                ShapedRecipePattern.of(keyed, List.of(" # ", "#X#", " # ")),
                new ItemStackTemplate(PeachCrisp.PEACH_CRISP, 2)
            );

            AdvancementHolder advancement = output.advancement()
                .addCriterion("has_pink_petals", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PINK_PETALS))
                .addCriterion("has_cookie", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COOKIE))
                .addCriterion("has_peach_crisp", InventoryChangeTrigger.TriggerInstance.hasItems(PeachCrisp.PEACH_CRISP))
                .requirements(AdvancementRequirements.Strategy.AND)
                .rewards(AdvancementRewards.Builder.recipe(key).build())
                .build(modId("recipe/peach_crisp"));

            output.accept(key, recipe, advancement);
        }

        private static void shapedGoldenPeachCrisp(RecipeOutput output) {
            ResourceKey<Recipe<?>> key = recipeKey("golden_peach_crisp");

            Map<Character, Ingredient> keyed = new LinkedHashMap<>();
            keyed.put('#', Ingredient.of(Items.GOLD_INGOT));
            keyed.put('X', Ingredient.of(PeachCrisp.PEACH_CRISP));

            ShapedRecipe recipe = new ShapedRecipe(
                RecipeBuilder.createCraftingCommonInfo(true),
                RecipeBuilder.createCraftingBookInfo(RecipeCategory.FOOD, null),
                ShapedRecipePattern.of(keyed, List.of("###", "#X#", "###")),
                new ItemStackTemplate(PeachCrisp.GOLDEN_PEACH_CRISP, 1)
            );

            AdvancementHolder advancement = output.advancement()
                .addCriterion("has_gold_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
                .addCriterion("has_golden_peach_crisp", InventoryChangeTrigger.TriggerInstance.hasItems(PeachCrisp.GOLDEN_PEACH_CRISP))
                .addCriterion("has_peach_crisp", InventoryChangeTrigger.TriggerInstance.hasItems(PeachCrisp.PEACH_CRISP))
                .requirements(AdvancementRequirements.Strategy.AND)
                .rewards(AdvancementRewards.Builder.recipe(key).build())
                .build(modId("recipe/golden_peach_crisp"));

            output.accept(key, recipe, advancement);
        }
    }

    private static final class ModAdvancementProvider extends FabricAdvancementProvider {

        private ModAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
            consumer.accept(
                Advancement.Builder.advancement()
                    .parent(createPlaceholder(Identifier.fromNamespaceAndPath("minecraft", "husbandry/root")))
                    .display(
                        PeachCrisp.PEACH_CRISP,
                        Component.translatable("advancement.peach-crisp.peach_crisp.title"),
                        Component.translatable("advancement.peach-crisp.peach_crisp.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                    )
                    .addCriterion("has_peach_crisp", InventoryChangeTrigger.TriggerInstance.hasItems(PeachCrisp.PEACH_CRISP))
                    .addCriterion("has_golden_peach_crisp", InventoryChangeTrigger.TriggerInstance.hasItems(PeachCrisp.GOLDEN_PEACH_CRISP))
                    .requirements(AdvancementRequirements.Strategy.OR)
                    .build(modId("peach_crisp"))
            );
        }
    }

    private static final class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

        private ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            builder(ItemTags.PIGLIN_LOVED)
                .add(ResourceKey.create(Registries.ITEM, modId("golden_peach_crisp")));
        }
    }

    private static final class VillagerTradeProvider extends FabricCodecDataProvider<VillagerTrade> {

        private VillagerTradeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture, Registries.VILLAGER_TRADE, VillagerTrade.CODEC);
        }

        @Override
        protected void configure(BiConsumer<Identifier, VillagerTrade> provider, HolderLookup.Provider registryLookup) {
            provider.accept(
                modId("farmer/5/emerald_peach_crisp"),
                new VillagerTrade(
                    new TradeCost(Items.EMERALD, 5),
                    new ItemStackTemplate(PeachCrisp.PEACH_CRISP, 8),
                    24,
                    30,
                    0.05f,
                    Optional.empty(),
                    List.of()
                )
            );
        }

        @Override
        public String getName() {
            return "Peach Crisp Villager Trades";
        }
    }

    private static final class VillagerTradeTagProvider extends FabricTagsProvider<VillagerTrade> {

        private VillagerTradeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.VILLAGER_TRADE, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            TagKey<VillagerTrade> farmerLevel5 = TagKey.create(
                Registries.VILLAGER_TRADE,
                Identifier.fromNamespaceAndPath("minecraft", "farmer/level_5")
            );

            builder(farmerLevel5)
                .add(ResourceKey.create(Registries.VILLAGER_TRADE,
                    Identifier.fromNamespaceAndPath("minecraft", "farmer/5/emerald_glistening_melon_slice")))
                .addOptional(ResourceKey.create(Registries.VILLAGER_TRADE,
                    modId("farmer/5/emerald_peach_crisp")));
        }
    }

    private static final class TradeSetProvider extends FabricCodecDataProvider<TradeSet> {

        private TradeSetProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture, Registries.TRADE_SET, TradeSet.CODEC);
        }

        @Override
        protected void configure(BiConsumer<Identifier, TradeSet> provider, HolderLookup.Provider registryLookup) {
            HolderLookup.RegistryLookup<VillagerTrade> tradesLookup = registryLookup.lookupOrThrow(Registries.VILLAGER_TRADE);
            HolderSet.Named<VillagerTrade> farmerLevel5 = tradesLookup.getOrThrow(
                TagKey.create(Registries.VILLAGER_TRADE,
                    Identifier.fromNamespaceAndPath("minecraft", "farmer/level_5"))
            );

            provider.accept(
                Identifier.fromNamespaceAndPath("minecraft", "farmer/level_5"),
                new TradeSet(
                    farmerLevel5,
                    ConstantValue.exactly(1),
                    false,
                    Optional.of(Identifier.fromNamespaceAndPath("minecraft", "trade_set/farmer/level_5"))
                )
            );
        }

        @Override
        public String getName() {
            return "Peach Crisp Trade Sets";
        }
    }

    private record PeachCrispPackMetadataProvider(FabricPackOutput output) implements DataProvider {

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            JsonObject packInfo = new JsonObject();
            packInfo.addProperty("description", "Peach Crisp Villager Trades");
            packInfo.addProperty("pack_format", 84);

            JsonObject root = new JsonObject();
            root.add("pack", packInfo);

            return DataProvider.saveStable(cache, root, output.getOutputFolder().resolve("pack.mcmeta"));
        }

        @Override
        public String getName() {
                return "Peach Crisp Pack Metadata";
            }
    }
}
