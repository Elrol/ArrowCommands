package dev.elrol.arrow.commands;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.arrow.ArrowCore;
import dev.elrol.arrow.api.events.*;
import dev.elrol.arrow.api.registries.*;
import dev.elrol.arrow.commands.commands.*;
import dev.elrol.arrow.commands.data.KitData;
import dev.elrol.arrow.commands.data.OldPlayerData;
import dev.elrol.arrow.commands.data.PlayerDataCommands;
import dev.elrol.arrow.commands.libs.CommandsConstants;
import dev.elrol.arrow.commands.libs.SilkTouchUtils;
import dev.elrol.arrow.commands.libs.SpawnerUtils;
import dev.elrol.arrow.commands.menus.*;
import dev.elrol.arrow.commands.menus.daycare.PokeSelect1;
import dev.elrol.arrow.commands.menus.daycare.PokeSelect2;
import dev.elrol.arrow.commands.menus.shops.ItemSelectMenu;
import dev.elrol.arrow.commands.menus.shops.ItemShopMenu;
import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.commands.registries.KitRegistry;
import dev.elrol.arrow.data.PlayerData;
import dev.elrol.arrow.data.PlayerDataCore;
import dev.elrol.arrow.libs.*;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import kotlin.Unit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArrowCommands implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(CommandsConstants.MODID);
    public static CommandConfig CONFIG = new CommandConfig();
    public Timer timer;

    static {
    }

    @Override
    public void onInitialize() {
        if(FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) return;
        CONFIG = CONFIG.load();

        registerEvents();
        registerCommands();
        registerPlaceholders();
        ArrowCore.registerMod(CommandsConstants.MODID);
    }

    private void registerPlaceholders() {
        LOGGER.warn("Registering Placeholders");
        Placeholders.register(Identifier.of("arrow","name"), (ctx, arg) -> {
            assert ctx.player() != null;
            return PlaceholderResult.value(PermUtils.getMetaData(ctx.player()).getMetaValue("name", (s -> s)).orElse(ctx.player().getName().getString()));
        });
        Placeholders.register(Identifier.of("arrow","staff_prefix"), (ctx, arg) -> PlaceholderResult.value(PermUtils.getMetaData(ctx.player()).getMetaValue("staff_prefix", (s -> s)).orElse("")));
        Placeholders.register(Identifier.of("arrow","donor_prefix"), (ctx, arg) -> PlaceholderResult.value(PermUtils.getMetaData(ctx.player()).getMetaValue("donor_prefix", (s -> s)).orElse("")));
    }

    private void registerEvents() {
        IEventRegistry eventRegistry = ArrowCore.INSTANCE.getEventRegistry();

        eventRegistry.registerEvent(() -> ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            CommandsMenuItems.register();
            registerMenus();
        }));

        eventRegistry.registerEvent(() -> CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, (event)-> {
            int totalWon = 0;
            IEconomyRegistry economyRegistry = ArrowCore.INSTANCE.getEconomyRegistry();

            List<ServerPlayerEntity> players = new ArrayList<>();

            AtomicBoolean hasAmuletCoin = new AtomicBoolean(false);

            event.getWinners().forEach(winner -> {
                if(winner instanceof PlayerBattleActor playerBattleActor) {
                    ServerPlayerEntity player = playerBattleActor.getEntity();
                    players.add(player);

                    playerBattleActor.getPokemonList().forEach(pokemon -> {
                        if(!pokemon.getFacedOpponents().isEmpty()) {

                            Pokemon mon = pokemon.getOriginalPokemon();

                            ItemStack stack = mon.heldItem();
                            Item amuletCoin = Registries.ITEM.get(Identifier.of("arrowlegends", "amulet_coin"));
                            if(!amuletCoin.equals(Items.AIR) && stack.isOf(amuletCoin)) hasAmuletCoin.set(true);
                        }
                    });
                }
            });

            AtomicBoolean usedPayday = new AtomicBoolean(false);
            event.getBattle().getChatLog().forEach(line -> {
                String lineString = line.getString();
                if(lineString != null && lineString.contains(" used Pay Day on ")) usedPayday.set(true);
            });

            for(BattleActor loser : event.getLosers()){
                for(BattlePokemon pokemon : loser.getPokemonList()) {
                    int money = CONFIG.economySettings.calcMoneyFromPokemon(pokemon.getOriginalPokemon(), usedPayday.get(), hasAmuletCoin.get());
                    if(ArrowCore.CONFIG.isDebug)
                        ArrowCommands.LOGGER.warn("Pokemon: {}", money);
                    totalWon += money;
                }
            }

            int finalTotalWon = totalWon;

            players.forEach(player -> {
                assert player != null;
                economyRegistry.deposit(player, BigDecimal.valueOf(finalTotalWon));
                player.sendMessage(ModTranslations.msg("got_money_1")
                        .append(economyRegistry.getAmount(finalTotalWon))
                        .append(ModTranslations.msg("got_money_2"))
                        .append(economyRegistry.getAmount(economyRegistry.getBal(player.getUuid())))
                        .append(ModTranslations.msg("got_money_3"))
                );
            });

            return Unit.INSTANCE;
        }));

        eventRegistry.registerEvent(() -> ServerLivingEntityEvents.ALLOW_DEATH.register(((entity, damageSource, damageAmount) -> {
            if(entity instanceof ServerPlayerEntity player) {
                PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
                PlayerDataCore coreData = data.get(new PlayerDataCore());
                coreData.logTeleport(player);
                data.put(coreData);
                player.sendMessage(ModTranslations.msg("back_on_death"));
            }
            return true;
        })));

        eventRegistry.registerEvent(() -> BlockPlaceCallback.PRE.register((world, player, pos, state, stack, entity) -> {
            if(state.getBlock().equals(Blocks.SPAWNER) && ArrowCore.CONFIG.isDebug) {
                ArrowCommands.LOGGER.warn("Placing Spawner");
            }
            return true;
        }));

        eventRegistry.registerEvent(() -> BlockPlaceCallback.POST.register((world, player, pos, state, stack, entity) -> {
            if(entity instanceof MobSpawnerBlockEntity spawnerEntity) {
                player.sendMessage(ModTranslations.msg("placed_spawner"));
                SpawnerUtils.fromItemStack(spawnerEntity, stack);
            }
        }));

        eventRegistry.registerEvent(() -> PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) ->{
            Map<String, Block> validBlocks = new HashMap<>();

            validBlocks.put("spawner", Blocks.SPAWNER);
            validBlocks.put("amethyst", Blocks.BUDDING_AMETHYST);

            for(Map.Entry<String, Block> entry : validBlocks.entrySet()) {
                Block keyBlock = entry.getValue();
                boolean allowed = ArrowCommands.CONFIG.silkTouchSettings.get(entry.getKey());
                if(keyBlock.equals(state.getBlock()) && allowed) {
                    return SilkTouchUtils.attemptSilkTouch(player, state, pos, keyBlock);
                }
            }
            return true;
        }));

        eventRegistry.registerEvent(() -> RefreshCallback.EVENT.register(() -> {
            CONFIG = CONFIG.load();
            KitRegistry.load();
            return ActionResult.PASS;
        }));

        eventRegistry.registerEvent(() -> ArrowEvents.PLAYER_DATA_LOADED_EVENT.register((player, data) -> {
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());

            if(ArrowCore.CONFIG.isDebug)
                ArrowCommands.LOGGER.warn("Current Loaded Player Data: {}", ArrowCore.INSTANCE.getPlayerDataRegistry().getLoadedData().size());

            if(!DaycareMenu.daycareMenus.containsKey(player.getUuid())
                    && commandData.daycareData.isBreeding()
                    && !commandData.daycareData.isReadyToHatch()) {
                DaycareMenu.daycareMenus.put(player.getUuid(), null);
            }

            KitData starterKit = KitRegistry.get("starter");
            if(starterKit != null && !commandData.kitCooldownMap.containsKey("starter")) {
                starterKit.giveKit(player);

                if(starterKit.cooldown > 0 || starterKit.oneTimeUse) {
                    commandData.kitCooldownMap.put(starterKit.id, starterKit.cooldown);
                }
            }

            data.put(commandData);

            if(ArrowCore.CONFIG.isDebug)
                LOGGER.warn("Command Data Loaded for {}", player.getUuid());
        }));

        eventRegistry.registerEvent(() -> ArrowEvents.PLAYER_DATA_UNLOADING_EVENT.register((serverPlayerEntity, data) -> {
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            commandData.lastOnline = LocalDateTime.now();
            data.put(commandData);
        }));

        eventRegistry.registerEvent(() -> ArrowEvents.CONFIG_LOADED_EVENT.register(() -> {

            if(ArrowCore.CONFIG.loadOldData) {
                File oldDir = new File(Constants.ARROW_DATA_DIR, "/old_data");
                if (oldDir.mkdir()) LOGGER.warn("Old Data Folder Created");

                LOGGER.warn("Loading Old Data");
                File[] files = oldDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        UUID uuid = UUID.fromString(file.getName().replace(".dat", ""));
                        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(uuid);
                        PlayerDataCommands commandData = data.get(new PlayerDataCommands());

                        JsonElement old = JsonUtils.loadFromJson(oldDir, file.getName(), JsonParser.parseString("{}"));
                        DataResult<Pair<OldPlayerData, JsonElement>> oldDataPair = OldPlayerData.CODEC.decode(JsonOps.INSTANCE, old);
                        OldPlayerData oldData = oldDataPair.getOrThrow().getFirst();

                        oldData.homes.forEach((home, location) -> {
                            commandData.setHome(home, location.toExact());
                        });

                        data.put(commandData, true);
                        LOGGER.warn("Converted Data for {}", uuid);
                    }
                } else {
                    LOGGER.warn("Files were null");
                }
            }
        }));

        eventRegistry.registerEvent(() ->ServerPlayConnectionEvents.JOIN.register((network, sender, server) -> {
            ServerPlayerEntity player = network.player;
            //PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
        }));

        eventRegistry.registerEvent(() -> ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            DaycareMenu.daycareMenus.remove(handler.player.getUuid());
        })));

        eventRegistry.registerEvent(() -> ServerLifecycleEvents.SERVER_STARTED.register((server)->{
            KitRegistry.load();

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    List<UUID> uuidsToRemove = new ArrayList<>();
                    DaycareMenu.daycareMenus.forEach((uuid, menu)-> {
                        TriState state;
                        if(menu == null) {
                            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(uuid);
                            PlayerDataCommands commandData = data.get(new PlayerDataCommands());

                            state = commandData.daycareData.tickTime();
                            data.put(commandData);
                        } else {
                            state = menu.tickEgg();
                        }

                        if(state.equals(TriState.TRUE)) {
                            uuidsToRemove.add(uuid);
                            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                            if(player != null) player.sendMessage(ModTranslations.msg("egg_ready"));
                        }
                    });

                    uuidsToRemove.forEach(DaycareMenu.daycareMenus::remove);
                }
            }, 1000, 1000);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    IPlayerDataRegistry playerDataRegistry = ArrowCore.INSTANCE.getPlayerDataRegistry();
                    server.getPlayerManager().getPlayerList().forEach(player -> {
                        PlayerData data = playerDataRegistry.getPlayerData(player);
                        PlayerDataCommands commandData = data.get(new PlayerDataCommands());

                        Map<String, Integer> tempMap = new HashMap<>(commandData.kitCooldownMap);
                        commandData.kitCooldownMap.forEach((id, cooldown) -> {
                            KitData kit = KitRegistry.get(id);
                            if(kit == null || kit.oneTimeUse) return;
                            int left = cooldown - 1;
                            if(left > 0) {
                                tempMap.put(id, left);
                            } else {
                                player.sendMessage(ModTranslations.msg("kit_ready", kit.name.getString()));
                                tempMap.remove(id);
                            }
                        });

                        if(ArrowCore.CONFIG.isDebug)
                            ArrowCommands.LOGGER.warn("Kit Timer Ran");

                        commandData.kitCooldownMap.clear();
                        commandData.kitCooldownMap.putAll(tempMap);
                        data.put(commandData);
                    });

                }
            }, 60000, 60000);
        }));

        eventRegistry.registerEvent(() -> ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            if(timer != null) timer.cancel();
        }));

        eventRegistry.registerEvent(() -> MenuCloseCallback.EVENT.register((player) -> {
            if(DaycareMenu.daycareMenus.containsKey(player.getUuid()))
                DaycareMenu.daycareMenus.put(player.getUuid(), null);
            return ActionResult.PASS;
        }));
    }

    private void registerMenus() {
        IMenuRegistry menuRegistry = ArrowCore.INSTANCE.getMenuRegistry();

        menuRegistry.registerMenu("daycare", DaycareMenu.class);
        menuRegistry.registerMenu("pokeselect_1", PokeSelect1.class);
        menuRegistry.registerMenu("pokeselect_2", PokeSelect2.class);
        menuRegistry.registerMenu("confirm", ConfirmMenu.class);
        menuRegistry.registerMenu("main", MainMenu.class);
        menuRegistry.registerMenu("settings", SettingsMenu.class);
        menuRegistry.registerMenu("shop", ShopMenu.class);
        menuRegistry.registerMenu("shopping_cart", ShoppingCartMenu.class);
        menuRegistry.registerMenu("item_select", ItemSelectMenu.class);
        menuRegistry.registerMenu("item_shop", ItemShopMenu.class);

    }

    private void registerCommands() {
        ICommandRegistry commandRegistry = ArrowCore.INSTANCE.getCommandRegistry();

        ArrowCommands.LOGGER.warn("Registering Commands");

        commandRegistry.registerCommand(new BackCommand());
        commandRegistry.registerCommand(new BalanceCommand());
        commandRegistry.registerCommand(new BalTopCommand());
        commandRegistry.registerCommand(new DaycareCommand());
        commandRegistry.registerCommand(new DelHomeCommand());
        commandRegistry.registerCommand(new DelWarpCommand());
        commandRegistry.registerCommand(new DiscordCommand());
        commandRegistry.registerCommand(new EcoCommand());
        commandRegistry.registerCommand(new HomeCommand());
        commandRegistry.registerCommand(new GiveKitCommand());
        commandRegistry.registerCommand(new KitCommand());
        commandRegistry.registerCommand(new ListWarpCommand());
        commandRegistry.registerCommand(new MenuCommand());
        commandRegistry.registerCommand(new RTPCommand());
        commandRegistry.registerCommand(new SetHomeCommand());
        commandRegistry.registerCommand(new SetSpawnCommand());
        commandRegistry.registerCommand(new SettingsCommand());
        commandRegistry.registerCommand(new SetWarpCommand());
        commandRegistry.registerCommand(new ShopCommand());
        commandRegistry.registerCommand(new SpawnCommand());
        commandRegistry.registerCommand(new TpaAcceptCommand());
        commandRegistry.registerCommand(new TpaCommand());
        commandRegistry.registerCommand(new TpaHereCommand());
        commandRegistry.registerCommand(new WarpCommand());
    }

}
