package io.letsrolldrew.feud.effects.fastmoney;

import io.letsrolldrew.feud.fastmoney.FastMoneyService;
import java.util.Objects;
import java.util.UUID;

public final class FastMoneyPlayerBindService {
    private final FastMoneyService fastMoneyService;
    private UUID armedHostId;
    private Target target;

    public FastMoneyPlayerBindService(FastMoneyService fastMoneyService) {
        this.fastMoneyService = Objects.requireNonNull(fastMoneyService, "fastMoneyService");
    }

    public void armPlayer1(UUID hostId) {
        arm(hostId, Target.PLAYER1);
    }

    public void armPlayer2(UUID hostId) {
        arm(hostId, Target.PLAYER2);
    }

    public void clear() {
        armedHostId = null;
        target = null;
    }

    public boolean isArmed(UUID hostId) {
        return target != null && armedHostId != null && armedHostId.equals(hostId);
    }

    public boolean bindIfArmed(UUID hostId, UUID playerId, String playerName) {
        if (!isArmed(hostId)) {
            return false;
        }
        if (target == Target.PLAYER1) {
            fastMoneyService.bindPlayer1(playerId, playerName);
        } else {
            fastMoneyService.bindPlayer2(playerId, playerName);
        }
        clear();
        return true;
    }

    private void arm(UUID hostId, Target target) {
        this.armedHostId = Objects.requireNonNull(hostId, "hostId");
        this.target = Objects.requireNonNull(target, "target");
    }

    private enum Target {
        PLAYER1,
        PLAYER2
    }
}
