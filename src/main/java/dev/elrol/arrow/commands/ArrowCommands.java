package dev.elrol.arrow.commands;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity;
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
import dev.elrol.arrow.commands.data.*;
import dev.elrol.arrow.commands.interfaces.IDisplayShop;
import dev.elrol.arrow.commands.interfaces.ILockable;
import dev.elrol.arrow.commands.libs.*;
import dev.elrol.arrow.commands.menus.*;
import dev.elrol.arrow.commands.menus.createshop.EditShopMenu;
import dev.elrol.arrow.commands.menus.createshop.item.ItemShopSetupMenu;
import dev.elrol.arrow.commands.menus.createshop.pokemon.PokemonShopSetupMenu;
import dev.elrol.arrow.commands.menus.daycare.PokeSelect1;
import dev.elrol.arrow.commands.menus.daycare.PokeSelect2;
import dev.elrol.arrow.commands.menus.shops.ItemSelectMenu;
import dev.elrol.arrow.commands.menus.shops.ItemShopMenu;
import dev.elrol.arrow.commands.menus.shops.ShoppingCartMenu;
import dev.elrol.arrow.commands.registries.*;
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
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArrowCommands implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(CommandsConstants.MODID);
    public static CommandConfig CONFIG = new CommandConfig();
    public static final CommandsMenuItems MENU_ITEMS = new CommandsMenuItems();
    public static final Timer TIMER = new Timer();

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

        Placeholders.register(Identifier.of("arrow","staff_prefix"), (ctx, arg) -> {
            CachedMetaData meta = PermUtils.getMetaData(ctx.player());
            MutableText prefix = Text.literal(meta.getMetaValue("staff_prefix", (s -> s)).orElse(""));
            HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(meta.getMetaValue("staff_name", (s) -> s).orElse("")));
            prefix.setStyle(prefix.getStyle().withHoverEvent(hover));
            return PlaceholderResult.value(prefix);
        });

        Placeholders.register(Identifier.of("arrow","donor_prefix"), (ctx, arg) -> {
            CachedMetaData meta = PermUtils.getMetaData(ctx.player());
            MutableText prefix = Text.literal(meta.getMetaValue("donor_prefix", (s -> s)).orElse(""));
            HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(meta.getMetaValue("donor_name", (s) -> s).orElse("")));
            prefix.setStyle(prefix.getStyle().withHoverEvent(hover));
            return PlaceholderResult.value(prefix);
        });

        Placeholders.register(Identifier.of("arrow","rank_prefix"), (ctx, arg) -> {
            CachedMetaData meta = PermUtils.getMetaData(ctx.player());
            MutableText prefix = Text.literal(meta.getMetaValue("rank_prefix", (s -> s)).orElse(""));
            HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(meta.getMetaValue("rank_name", (s) -> s).orElse("")));
            prefix.setStyle(prefix.getStyle().withHoverEvent(hover));
            return PlaceholderResult.value(prefix);
        });
    }

    private void registerEvents() {
        IEventRegistry eventRegistry = ArrowCore.INSTANCE.getEventRegistry();

        eventRegistry.registerEvent(() -> ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(sender);
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            TempShopData shopData = commandData.playerShopData.tempShop;
            if(shopData == null || !shopData.getStage().equals(TempShopData.ShopStage.price)) return true;

            boolean needsPrice = !shopData.hasPrice();

            if(needsPrice) {
                String string = message.getContent().getString();
                if(string.equalsIgnoreCase("cancel")) {
                    sender.sendMessage(ModTranslations.msg("price_canceled"));
                    shopData.resetStage();
                } else {
                    try {
                        int price = Integer.parseInt(string);
                        shopData.shop.setPrice(price);
                        shopData.resetStage();
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ModTranslations.err("invalid_price"));
                    }
                }
                commandData.playerShopData.tempShop = shopData;
                data.put(commandData);
                ArrowCore.INSTANCE.getMenuRegistry().createMenu("edit_shop_menu", sender).open();
                return false;
            } else {
                return true;
            }
        }));

        eventRegistry.registerEvent(() -> ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            MENU_ITEMS.register();
            KitRegistry.load(server);
            registerMenus();
            CrateRegistry.register(server);
            ServerShopRegistry.register(server);

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(ArrowCore.CONFIG.isDebug) LOGGER.warn("Daycare Timer Tick");
                    final Map<UUID, DaycareMenu> daycareMenus = DaycareMenu.daycareMenus;
                    daycareMenus.forEach((uuid, menu) -> {
                        try {
                            if(menu == null) return;
                            menu.setHatchery();
                        } catch(Exception e) {
                            LOGGER.error(e.getLocalizedMessage());
                        }
                    });

                    ArrowCore.INSTANCE.getPlayerDataRegistry().getLoadedData().forEach((uuid, data) -> {
                        try {
                            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
                            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                            if (player == null || commandData.daycareData.getTime() != 0)
                                return;
                            player.sendMessage(ModTranslations.msg("egg_ready"));
                            player.playSoundToPlayer(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.MASTER, 1.0f, 1.0f);
                        } catch(Exception e) {
                            LOGGER.error(e.getLocalizedMessage());
                        }
                    });
                }
            }, 1000, 1000);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT);

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    server.getPlayerManager().getPlayerList().forEach(player -> {
                        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
                        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
                        commandData.onTimeData.logTime();
                        data.put(commandData, true);
                    });
                }
            }, now.until(tomorrow, ChronoUnit.MILLIS), 86400000);
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
            } else if(entity instanceof DisplayCaseBlockEntity) {
                PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
                PlayerDataCommands commandData = data.get(new PlayerDataCommands());
                TempShopData shopData = commandData.playerShopData.tempShop;
                if(shopData != null && !shopData.hasDisplayCase() && shopData.getStage().equals(TempShopData.ShopStage.displayCase)) {
                    shopData.shop.setDisplayCase(pos);
                    if(entity instanceof IDisplayShop displayShop){
                        displayShop.arrowcommands$lock();
                        ArrowCommands.LOGGER.error("Case was Locked");
                    }
                    shopData.resetStage();
                    commandData.playerShopData.tempShop = shopData;
                    data.put(commandData);
                    ArrowCore.INSTANCE.getMenuRegistry().createMenu("edit_shop_menu", player).open();
                }
            }
        }));

        eventRegistry.registerEvent(() -> PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) ->{

            if(entity instanceof IDisplayShop displayShop && player instanceof ServerPlayerEntity serverPlayer) {
                ConfirmMenu confirm = (ConfirmMenu) ArrowCore.INSTANCE.getMenuRegistry().createMenu("confirm", serverPlayer);
                if(!displayShop.arrowcommands$isShop()) return true;
                if(player.isCreative()) {
                    confirm.init("Do you want to remove this shop?", () -> {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        PlayerShopUtils.removeShop(player, pos);
                        confirm.close();
                    }, confirm::close);
                    confirm.open();
                    return false;
                }

                UUID shopOwner = displayShop.arrowcommands$getOwner();
                if(shopOwner != null && player.getUuid().equals(shopOwner)) {
                    confirm.init("confirm_remove_shop", "confirm_yes", "confirm_no", () -> {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        player.giveItemStack(new ItemStack(CobblemonItems.DISPLAY_CASE, 1));
                        PlayerShopUtils.removeShop(player, pos);
                        confirm.close();
                    }, confirm::close);
                    confirm.open();
                    return false;
                }
                return !displayShop.arrowcommands$locked();
            } else {
                Map<String, Block> validBlocks = new HashMap<>();

                validBlocks.put("spawner", Blocks.SPAWNER);
                validBlocks.put("amethyst", Blocks.BUDDING_AMETHYST);

                for (Map.Entry<String, Block> entry : validBlocks.entrySet()) {
                    Block keyBlock = entry.getValue();
                    boolean allowed = ArrowCommands.CONFIG.silkTouchSettings.get(entry.getKey());
                    if (keyBlock.equals(state.getBlock()) && allowed) {
                        return SilkTouchUtils.attemptSilkTouch(player, state, pos, keyBlock);
                    }
                }
                return true;
            }
        }));

        eventRegistry.registerEvent(() -> UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity entity = world.getBlockEntity(pos);
            if(player instanceof ServerPlayerEntity serverPlayer) {
                if (entity instanceof DisplayCaseBlockEntity) {
                    IDisplayShop displayShop = BlockUtils.getDisplayShop(entity);
                    // TODO make this better
                    if (displayShop == null || !displayShop.arrowcommands$isShop()) {
                        PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
                        PlayerDataCommands commandData = data.get(new PlayerDataCommands());
                        TempShopData shopData = commandData.playerShopData.tempShop;

                        if (shopData != null && shopData.isValid(pos)) {
                            ItemStack inHand = player.getStackInHand(hand);
                            if (shopData.getStage().equals(TempShopData.ShopStage.saleData) && !inHand.isEmpty()) {
                                ((ItemShopSaleData) shopData.shop.saleData).setItem(inHand);
                                shopData.resetStage();
                                commandData.playerShopData.tempShop = shopData;
                                data.put(commandData);
                                ArrowCore.INSTANCE.getMenuRegistry().createMenu("edit_shop_menu", serverPlayer).open();
                                return ActionResult.FAIL;
                            }
                        }
                    }
                } else if (entity instanceof LockableContainerBlockEntity && player.getMainHandStack().isOf(Items.TRIPWIRE_HOOK) && player.isSneaking()) {
                    PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player.getUuid());
                    PlayerDataCommands commandData = data.get(new PlayerDataCommands());
                    TempShopData shopData = commandData.playerShopData.tempShop;
                    if (shopData != null && shopData.getStage().equals(TempShopData.ShopStage.stock)) {
                        ((ItemShopSaleData) shopData.shop.saleData).addStock(pos);
                        shopData.resetStage();
                        commandData.playerShopData.tempShop = shopData;
                        data.put(commandData);
                        ArrowCore.INSTANCE.getMenuRegistry().createMenu("edit_shop_menu", serverPlayer).open();
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        }));

        eventRegistry.registerEvent(() -> RefreshCallback.EVENT.register((server) -> {
            CONFIG = CONFIG.load();
            KitRegistry.load(server);
            CrateRegistry.register(server);
            ServerShopRegistry.register(server);
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
            if(starterKit != null && !commandData.kitTimeStamps.containsKey("starter")) {
                starterKit.giveKit(player);

                if(starterKit.cooldown > 0 || starterKit.oneTimeUse) {
                    final LocalDateTime now = LocalDateTime.now();
                    commandData.kitTimeStamps.put(starterKit.id, now);
                }
            }

            data.put(commandData);

            if(ArrowCore.CONFIG.isDebug)
                LOGGER.warn("Command Data Loaded for {}", player.getUuid());
        }));

        eventRegistry.registerEvent(() -> ArrowEvents.SERVER_DATA_LOADED_EVENT.register((serverData) -> {
            ServerDataCommands serverDataCommands = serverData.get(new ServerDataCommands());
        }));

        eventRegistry.registerEvent(() -> ArrowEvents.PLAYER_DATA_UNLOADING_EVENT.register((serverPlayerEntity, data) -> {}));

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

        eventRegistry.registerEvent(() -> ServerPlayConnectionEvents.JOIN.register((network, sender, server) -> {
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(network.player);
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            if(commandData.onTimeData == null) commandData.onTimeData = new OnTimeData();
            commandData.onTimeData.updateLastOnline();
            data.put(commandData);
        }));

        eventRegistry.registerEvent(() -> ArrowEvents.PLAYER_DATA_UNLOADING_EVENT.register((player, data) -> {
            DaycareMenu.daycareMenus.remove(player.getUuid());

            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            commandData.onTimeData.logTime();
            data.put(commandData);
        }));

        eventRegistry.registerEvent(() -> ServerPlayConnectionEvents.DISCONNECT.register(((network, server) -> {

        })));

        eventRegistry.registerEvent(() -> ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            TIMER.cancel();
        }));

        eventRegistry.registerEvent(() -> MenuCloseCallback.EVENT.register((menu) -> {
            ServerPlayerEntity player = menu.getPlayer();
            String menuName = menu.menuName;
            
            PlayerData data = ArrowCore.INSTANCE.getPlayerDataRegistry().getPlayerData(player);
            PlayerDataCommands commandData = data.get(new PlayerDataCommands());
            
            if(menuName.equalsIgnoreCase("daycare")) {
                if (DaycareMenu.daycareMenus.containsKey(player.getUuid())) {
                    commandData.daycareData.clearSlots();
                    data.put(commandData);
                    DaycareMenu.daycareMenus.remove(player.getUuid());
                }
            } else if (menuName.equalsIgnoreCase("crate")) {
                CrateRegistry.grantRewards(player);
            } else if (menuName.equalsIgnoreCase("edit_shop_menu")) {
                TempShopData tempShop = commandData.playerShopData.tempShop;
                if(tempShop == null) return ActionResult.PASS;

                boolean cancel = tempShop.getStage().equals(TempShopData.ShopStage.cancel);
                boolean hasDisplay = tempShop.hasDisplayCase();
                if(cancel && hasDisplay) {
                    ServerWorld world = player.getServerWorld();
                    BlockEntity entity = world.getBlockEntity(tempShop.shop.getDisplayCase());
                    if(tempShop.hasDisplayCase() && entity instanceof ILockable lockable) {
                        if(lockable.arrowcommands$locked()) {
                            lockable.arrowcommands$unlock();
                            debug("Display Case Unlocked");
                        }
                    }
                    commandData.playerShopData.tempShop = null;
                }
            }
            data.put(commandData);
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
        menuRegistry.registerMenu("crate", CrateMenu.class);
        menuRegistry.registerMenu("shop_setup", ShopSetupMenu.class);
        menuRegistry.registerMenu("edit_shop_menu", EditShopMenu.class);
        menuRegistry.registerMenu("item_shop_setup", ItemShopSetupMenu.class);
        menuRegistry.registerMenu("poke_shop_setup", PokemonShopSetupMenu.class);
        menuRegistry.registerMenu("poke_select", PokeSelectMenu.class);

    }

    private void registerCommands() {
        ICommandRegistry commandRegistry = ArrowCore.INSTANCE.getCommandRegistry();

        ArrowCommands.LOGGER.warn("Registering Commands");

        commandRegistry.registerCommand(new BackCommand());
        commandRegistry.registerCommand(new BalanceCommand());
        commandRegistry.registerCommand(new BalTopCommand());
        commandRegistry.registerCommand(new CrateCommand());
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

        commandRegistry.registerCommand(new PhotoCommand());
        commandRegistry.registerCommand(new PayCommand());

        commandRegistry.registerCommand(new AFKCommand());
        commandRegistry.registerCommand(new OnTimeCommand());
        commandRegistry.registerCommand(new SeenCommand());

        commandRegistry.registerCommand(new CreateShopCommand());
    }

    public static void debug(String message) {
        if(ArrowCore.CONFIG.isDebug) {
            ArrowCommands.LOGGER.warn(message);
        }
    }

}
