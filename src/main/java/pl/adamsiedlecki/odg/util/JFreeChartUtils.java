package pl.adamsiedlecki.odg.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jfree.data.time.Minute;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JFreeChartUtils {

    public static Minute convert(LocalDateTime localDateTime) {
        return new Minute(localDateTime.getMinute(),
                          localDateTime.getHour(),
                          localDateTime.getDayOfMonth(),
                          localDateTime.getMonthValue(),
                          localDateTime.getYear());
    }
}
