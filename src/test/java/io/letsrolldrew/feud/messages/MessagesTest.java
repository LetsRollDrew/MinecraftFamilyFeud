package io.letsrolldrew.feud.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

public final class MessagesTest {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    void renderLines_appliesPrefixAndPlaceholder() {
        Messages messages = new Messages("FamilyFeud");

        List<String> lines =
                messages.renderLines(MsgTone.INFO, Msg.NEED_PERMISSION, Placeholder.of("permission", "p1")).stream()
                        .map(PLAIN::serialize)
                        .toList();

        assertEquals(List.of("[FamilyFeud] You need permission: p1"), lines);
    }

    @Test
    void renderLines_splitsOnNewlines() {
        Messages messages = new Messages("FamilyFeud");

        List<String> lines = messages.renderLines(MsgTone.USAGE, Msg.TIMER_HELP).stream()
                .map(PLAIN::serialize)
                .toList();

        assertEquals(
                List.of(
                        "[FamilyFeud] Timer commands:",
                        "[FamilyFeud] /feud timer start [seconds]",
                        "[FamilyFeud] /feud timer stop",
                        "[FamilyFeud] /feud timer reset [seconds]",
                        "[FamilyFeud] /feud timer status"),
                lines);
    }
}
