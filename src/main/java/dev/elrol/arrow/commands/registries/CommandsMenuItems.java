package dev.elrol.arrow.commands.registries;

import dev.elrol.arrow.api.registries.IItemRegistry;
import net.minecraft.item.Item;

public class CommandsMenuItems implements IItemRegistry {
    
    public static Item GRAY_BUTTON;
    public static Item GRAY_BUTTON_LEFT;
    public static Item GRAY_BUTTON_MIDDLE;
    public static Item GRAY_BUTTON_RIGHT;
    
    public static Item LIME_BUTTON;
    public static Item LIME_BUTTON_1;
    public static Item LIME_BUTTON_2;
    public static Item LIME_BUTTON_3;
    public static Item LIME_BUTTON_4;
    public static Item LIME_BUTTON_LEFT;
    public static Item LIME_BUTTON_MIDDLE;
    public static Item LIME_BUTTON_RIGHT;

    public static Item RED_BUTTON;
    public static Item RED_BUTTON_1;
    public static Item RED_BUTTON_2;
    public static Item RED_BUTTON_3;
    public static Item RED_BUTTON_4;
    public static Item RED_BUTTON_LEFT;
    public static Item RED_BUTTON_MIDDLE;
    public static Item RED_BUTTON_RIGHT;
    
    //Customizer
    public static Item ABILITY_BUTTON;
    public static Item BALL_BUTTON;
    public static Item EVS_BUTTON;
    public static Item GENDER_BUTTON;
    public static Item IVS_BUTTON;
    public static Item NATURE_BUTTON;
    public static Item SHINY_BUTTON;

    //Daycare
    public static Item BREED_BUTTON;
    public static Item EGG_BUTTON;
    public static Item NEST_BUTTON;
    public static Item REMOVE_BUTTON;
    public static Item SLOT_ONE_BUTTON;
    public static Item SLOT_TWO_BUTTON;

    //Main
    public static Item CUSTOMIZER_BUTTON;
    public static Item DAYCARE_BUTTON;
    public static Item SETTINGS_BUTTON;
    public static Item SHOP_BUTTON;

    //Shop
    public static Item CART_BUTTON;

    //Crates
    public static Item VOTE_CRATE;
    public static Item VOTE_KEY;

    public void register(){

        GRAY_BUTTON                 = get("gray_button");
        GRAY_BUTTON_LEFT            = get("gray_button_left");
        GRAY_BUTTON_MIDDLE          = get("gray_button_middle");
        GRAY_BUTTON_RIGHT           = get("gray_button_right");

        LIME_BUTTON                 = get("lime_button");
        LIME_BUTTON_1               = get("lime_button_1");
        LIME_BUTTON_2               = get("lime_button_2");
        LIME_BUTTON_3               = get("lime_button_3");
        LIME_BUTTON_4               = get("lime_button_4");
        LIME_BUTTON_LEFT            = get("lime_button_left");
        LIME_BUTTON_MIDDLE          = get("lime_button_middle");
        LIME_BUTTON_RIGHT           = get("lime_button_right");

        RED_BUTTON                  = get("red_button");
        RED_BUTTON_1                = get("red_button_1");
        RED_BUTTON_2                = get("red_button_2");
        RED_BUTTON_3                = get("red_button_3");
        RED_BUTTON_4                = get("red_button_4");
        RED_BUTTON_LEFT             = get("red_button_left");
        RED_BUTTON_MIDDLE           = get("red_button_middle");
        RED_BUTTON_RIGHT            = get("red_button_right");

        //Customizer
        ABILITY_BUTTON              = get("ability_button");
        BALL_BUTTON                 = get("ball_button");
        EVS_BUTTON                  = get("evs_button");
        GENDER_BUTTON               = get("gender_button");
        IVS_BUTTON                  = get("ivs_button");
        NATURE_BUTTON               = get("nature_button");
        SHINY_BUTTON                = get("shiny_button");

        //Daycare
        BREED_BUTTON                = get("breed_button");
        EGG_BUTTON                  = get("egg_button");
        NEST_BUTTON                 = get("nest_button");
        REMOVE_BUTTON               = get("remove_button");
        SLOT_ONE_BUTTON             = get("slot_one_button");
        SLOT_TWO_BUTTON             = get("slot_two_button");

        //Main
        CUSTOMIZER_BUTTON           = get("customizer_button");
        DAYCARE_BUTTON              = get("daycare_button");
        SETTINGS_BUTTON             = get("settings_button");
        SHOP_BUTTON                 = get("shop_button");

        //Shop
        CART_BUTTON                 = get("cart_button");

        //Crates
        VOTE_CRATE                  = get("vote_crate");
        VOTE_KEY                    = get("vote_key");
    }

    @Override
    public String getModID() {
        return "arrowcommands";
    }

}
