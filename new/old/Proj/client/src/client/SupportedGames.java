package client;

import java.util.Arrays;

public enum SupportedGames {
    TICTAKTOE,
    TICTAKTOERED,
    CHECKERS;

    public static String[] games() {
        return Arrays.stream(SupportedGames.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
}
