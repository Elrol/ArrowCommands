package dev.elrol.arrow.commands.interfaces;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IDisplayShop extends ILockable{

    void arrowcommands$lock();

    @Nullable
    UUID arrowcommands$getOwner();
    void arrowcommands$setOwner(UUID uuid);
    boolean arrowcommands$isShop();

}
