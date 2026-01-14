package io.letsrolldrew.feud.ui;

public enum HostRemoteKind {
    MAP(1),
    DISPLAY(2),
    SELECTOR(3),
    CLEANUP(4);

    private final int tag;

    HostRemoteKind(int tag) {
        this.tag = tag;
    }

    public int tag() {
        return tag;
    }
}
