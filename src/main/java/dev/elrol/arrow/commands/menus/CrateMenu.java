package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.commands.data.CrateData;
import dev.elrol.arrow.commands.data.CrateReward;
import dev.elrol.arrow.commands.registries.CrateRegistry;
import dev.elrol.arrow.libs.MenuUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class CrateMenu extends _CommandMenuBase {

    String crateID = "";
    List<CrateReward> rewardList = new ArrayList<>();
    int index = 0;

    GuiElement gui1;
    GuiElement gui2;
    GuiElement gui3;
    GuiElement gui4;
    GuiElement gui5;
    GuiElement gui6;
    GuiElement gui7;

    public CrateMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X3);
    }

    public void setCrateID(String crateID) {
        this.crateID = crateID;

        CrateData crate = CrateRegistry.get(crateID);
        if(crate != null) {
            while(rewardList.size() < 56) {
                rewardList.add(crate.pull());
            }
            CrateRegistry.tempRewardMap.put(player.getUuid(), rewardList.get(50));
        }
    }

    @Override
    public void open() {
        super.open();
        tick();
    }

    private void tick() {
        index++;
        drawMenu();

        player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER,0.5f, 1.25f);

        if(index < 47) {
            int ms;
            if (index >= 25) {
                if (index >= 40) {
                    if (index >= 45) ms = 1000;
                    else ms = 500;
                } else ms = 100;
            } else ms = 50;

            menu.createTimer(new TimerTask() {
                @Override
                public void run() {
                    tick();
                }
            }, ms);
        } else {

            menu.createTimer(new TimerTask() {
                @Override
                public void run() {
                    gui1.setItemStack(ItemStack.EMPTY);
                    gui2.setItemStack(ItemStack.EMPTY);
                    gui3.setItemStack(ItemStack.EMPTY);
                    gui5.setItemStack(ItemStack.EMPTY);
                    gui6.setItemStack(ItemStack.EMPTY);
                    gui7.setItemStack(ItemStack.EMPTY);

                    player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.MASTER);

                    menu.createTimer(new TimerTask() {
                        @Override
                        public void run() {
                            close();
                        }
                    }, 3000);
                }
            }, 1000);
        }
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        CrateReward reward1 = rewardList.get(index);
        CrateReward reward2 = rewardList.get(index + 1);
        CrateReward reward3 = rewardList.get(index + 2);
        CrateReward reward4 = rewardList.get(index + 3);
        CrateReward reward5 = rewardList.get(index + 4);
        CrateReward reward6 = rewardList.get(index + 5);
        CrateReward reward7 = rewardList.get(index + 6);

        gui1 = MenuUtils.itemStack(reward1.icon, reward1.name).build();
        gui2 = MenuUtils.itemStack(reward2.icon, reward2.name).build();
        gui3 = MenuUtils.itemStack(reward3.icon, reward3.name).build();
        gui4 = MenuUtils.itemStack(reward4.icon, reward4.name).build();
        gui5 = MenuUtils.itemStack(reward5.icon, reward5.name).build();
        gui6 = MenuUtils.itemStack(reward6.icon, reward6.name).build();
        gui7 = MenuUtils.itemStack(reward7.icon, reward7.name).build();

        setSlot(10, gui1);
        setSlot(11, gui2);
        setSlot(12, gui3);
        setSlot(13, gui4);
        setSlot(14, gui5);
        setSlot(15, gui6);
        setSlot(16, gui7);
    }

    @Override
    public int getMenuID() {
        return 12;
    }

    @Override
    public String getMenuName() {
        return "crate";
    }

    @Override
    public char getMenuUnicode() {
        return 'ꔀ';
    }
}
