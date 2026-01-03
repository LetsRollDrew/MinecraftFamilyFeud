package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.board.display.LayoutStoreSupport;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

public final class TimerPanelStore {
    private final File file;
    private final YamlConfiguration config;

    public TimerPanelStore(File file) {
        this.file = file;
        this.config = new YamlConfiguration();
        if (file != null && file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        reload();
    }

    public Map<String, StoredTimerPanel> loadPanels() {
        Map<String, StoredTimerPanel> panels = new HashMap<>();
        if (file == null) {
            return panels;
        }
        reload();
        var section = config.getConfigurationSection("panels");
        if (section == null) {
            return panels;
        }
        for (String id : section.getKeys(false)) {
            String path = "panels." + id + ".";
            try {
                DynamicBoardLayout layout = LayoutStoreSupport.readLayout(config, path);
                if (layout == null) {
                    continue;
                }
                panels.put(id, new StoredTimerPanel(id, layout));
            } catch (Exception ignored) {
                // malformed entry
            }
        }
        return panels;
    }

    public void savePanel(String panelId, DynamicBoardLayout layout) {
        if (file == null || panelId == null || panelId.isBlank() || layout == null) {
            return;
        }
        String path = "panels." + panelId + ".";
        LayoutStoreSupport.writeLayout(config, path, layout);
        save();
    }

    public void removePanel(String panelId) {
        if (file == null || panelId == null || panelId.isBlank()) {
            return;
        }
        config.set("panels." + panelId, null);
        save();
    }

    public void clear() {
        if (file == null) {
            return;
        }
        config.set("panels", null);
        save();
    }

    private void reload() {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            config.load(file);
        } catch (Exception ignored) {
        }
    }

    private void save() {
        if (file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
        }
    }
}
