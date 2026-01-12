package io.letsrolldrew.feud.commands;

import java.util.Arrays;

public final class CommandArgs {
    private CommandArgs() {}

    public static String[] tail(String[] args, int start) {
        if (args == null || start >= args.length) {
            return new String[0];
        }
        String[] out = new String[args.length - start];
        System.arraycopy(args, start, out, 0, args.length - start);
        return out;
    }

    public static String joinTail(String[] args, int start) {
        if (args == null || start >= args.length) {
            return "";
        }
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
}
