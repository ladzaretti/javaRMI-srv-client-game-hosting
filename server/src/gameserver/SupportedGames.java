package gameserver;

import java.util.Arrays;
import java.util.stream.Stream;

public enum SupportedGames {
    TICTAKTOE,
    CHECKERS;

    public static String[] games() {
        return Arrays.stream(SupportedGames.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
}
