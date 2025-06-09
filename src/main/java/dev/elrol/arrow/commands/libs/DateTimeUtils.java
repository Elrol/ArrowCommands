package dev.elrol.arrow.commands.libs;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtils {

    public static String formatDateTime(long until) {
        StringBuilder string = new StringBuilder();

        final long sec = until % 60;
        final long min = ((until - sec) % 3600) / 60;
        final long hour = ((until - min - sec) % 86400) / 3600;
        final long day = (until - hour - min - sec) / 86400;

        if(day > 0) {
            string.append(day).append(day > 1 ? " Days" : " Day");
        }
        if(hour > 0) {
            if(!string.isEmpty()) string.append(", ");
            string.append(hour).append(hour > 1 ? " Hours" : " Hour");
        }
        if(min > 0) {
            if(!string.isEmpty()) string.append(", ");
            string.append(min).append(min > 1 ? " Minutes" : " Minute");
        }
        if(sec > 0) {
            if(!string.isEmpty()) string.append(", ");
            string.append(sec).append(sec > 1 ? " Seconds" : " Second");
        }

        return string.toString();
    }

}
