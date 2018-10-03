package cloud.zeroprox.railgun;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "railgun", name = "RailGun", description = "RailGun", url = "https://zeroprox.cloud", authors = { "ewoutvs_", "Alagild" })
public class RailGun {

    private ItemStack s_item;
    private TextTemplate t_recharging, t_recharged;
    private int s_cooldown, s_range;
    private boolean s_cancel_event, s_fireworks;

    @Inject
    private Logger logger;
    private static RailGun instance;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private ConfigurationNode rootNode;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        Sponge.getEventManager().registerListeners(this, new Listeners());

        instance = this;
        configManager = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        try {
            rootNode = configManager.load();
            loadConfig();
        } catch (IOException e) {
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onGameReload(GameReloadEvent event) {
        try {
            loadConfig();
        } catch (IOException e) {
        } catch (ObjectMappingException e) {
        }
    }

    public static RailGun getInstance() {
        return instance;
    }

    public TextTemplate getRechargedTemplate() {
        return this.t_recharged;
    }

    public TextTemplate getRechargingTemplate() {
        return this.t_recharging;
    }

    public ItemStack getItem() {
        return this.s_item;
    }

    public int getRange() {
        return this.s_range;
    }

    public int getCooldown() {
        return this.s_cooldown;
    }

    public boolean shouldCancelEvent() {
        return this.s_cancel_event;
    }

    public boolean shouldFireworks() {
        return this.s_fireworks;
    }

    private void loadConfig() throws IOException, ObjectMappingException {
        if (rootNode.getNode("settings", "item").isVirtual()) {
            logger.info("Creating configuration");

            rootNode.getNode("settings", "item").setValue(TypeToken.of(ItemStack.class), ItemStack.builder().itemType(ItemTypes.DIAMOND_HOE).build());
            rootNode.getNode("settings", "cooldown").setValue(TypeToken.of(Integer.class), 6);
            rootNode.getNode("settings", "range").setValue(TypeToken.of(Integer.class), 100);
            rootNode.getNode("settings", "cancel_event").setValue(TypeToken.of(Boolean.class), false);
            rootNode.getNode("settings", "fireworks").setValue(TypeToken.of(Boolean.class), false);
            rootNode.getNode("messages", "message_recharging").setValue(TypeToken.of(TextTemplate.class), TextTemplate.of(TextColors.RED, "■■■■ RailGun is recharging ■■■■"));
            rootNode.getNode("messages", "message_recharged").setValue(TypeToken.of(TextTemplate.class), TextTemplate.of(TextColors.GREEN, "■■■■ RailGun is recharged ■■■■"));
            configManager.save(rootNode);
            loadConfig();
        } else {
            this.s_item = rootNode.getNode("settings", "item").getValue(TypeToken.of(ItemStack.class));
            this.s_cooldown = rootNode.getNode("settings", "cooldown").getValue(TypeToken.of(Integer.class));
            this.s_range = rootNode.getNode("settings", "range").getValue(TypeToken.of(Integer.class));
            this.s_cancel_event = rootNode.getNode("settings", "cancel_event").getValue(TypeToken.of(Boolean.class));
            this.s_fireworks = rootNode.getNode("settings", "fireworks").getValue(TypeToken.of(Boolean.class));
            this.t_recharging = rootNode.getNode("messages", "message_recharging").getValue(TypeToken.of(TextTemplate.class));
            this.t_recharged = rootNode.getNode("messages", "message_recharged").getValue(TypeToken.of(TextTemplate.class));

        }
    }
}
