package dev.elrol.arrow.commands.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.arrow.codecs.ArrowCodecs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OnTimeData {

    public static final Codec<OnTimeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("totalOnTime").forGetter(data -> Optional.of(data.totalOnTime)),
            ArrowCodecs.DATE_TIME_CODEC.optionalFieldOf("lastOnline").forGetter(data -> Optional.ofNullable(data.lastOnline)),
            ArrowCodecs.DATE_TIME_CODEC.optionalFieldOf("afkStart").forGetter(data -> Optional.ofNullable(data.startAFK)),
            Codec.LONG.listOf().fieldOf("dailyOnTimeHistory").forGetter(data -> data.dailyOnTimeHistory)
    ).apply(instance, (totalOnTime, lastOnline, afkStart, dailyOnTimeHistory) -> {
        OnTimeData data = new OnTimeData();

        lastOnline.ifPresent(a -> data.lastOnline = a);
        afkStart.ifPresent(b -> data.startAFK = b);
        totalOnTime.ifPresent(c -> data.totalOnTime = c);

        data.dailyOnTimeHistory.addAll(dailyOnTimeHistory);

        return data;
    }));

    long totalOnTime = 0;
    LocalDateTime lastOnline;
    LocalDateTime startAFK;
    List<Long> dailyOnTimeHistory = new ArrayList<>();

    public long getTotalOnTime() { return totalOnTime; }

    public LocalDateTime getLastOnline() { return lastOnline; }

    public void startAFK() {
        if(startAFK != null) return;
        startAFK = LocalDateTime.now();
        logTime();
    }

    public void endAFK() {
        startAFK = null;
        updateLastOnline();
    }

    public void updateLastOnline() {
        lastOnline = LocalDateTime.now();
    }

    public void logTime() {
        long secondsOnline = 0;
        if(lastOnline != null) {
            secondsOnline = lastOnline.until(Objects.requireNonNullElseGet(startAFK, LocalDateTime::now), ChronoUnit.SECONDS);
            addOnTime(secondsOnline);
        } else {
            updateLastOnline();
        }
    }

    public void addOnTime(long seconds) {
        LocalDateTime now = LocalDateTime.now();
        boolean isSameDate = lastOnline != null && lastOnline.toLocalDate().isEqual(now.toLocalDate());

        if(isSameDate) {
            if(dailyOnTimeHistory.isEmpty()) {
                dailyOnTimeHistory.add(seconds);
            } else {
                dailyOnTimeHistory.set(0, dailyOnTimeHistory.getFirst() + seconds);
            }
        } else {
            dailyOnTimeHistory.addFirst(seconds);
            while(dailyOnTimeHistory.size() > 30) {
                dailyOnTimeHistory.removeLast();
            }
        }
        totalOnTime += seconds;
        updateLastOnline();
    }

    public long getDayOnTime(){
        if(lastOnline == null) return 0;
        boolean isToday = lastOnline.toLocalDate().isEqual(LocalDate.now());
        return isToday ? dailyOnTimeHistory.getFirst() : 0;
    }

    public long getWeekOnTime(){
        long weekTime = 0;
        if(dailyOnTimeHistory.isEmpty()) return 0;

        for(int i = 0; i < 7; i++) {
            if(dailyOnTimeHistory.size() > i)
                weekTime += dailyOnTimeHistory.get(i);
        }

        return weekTime;
    }

    public long getMonthOnTime() {
        long monthTime = 0;
        for (Long dailyOnTime : dailyOnTimeHistory) monthTime += dailyOnTime;
        return monthTime;
    }

}
