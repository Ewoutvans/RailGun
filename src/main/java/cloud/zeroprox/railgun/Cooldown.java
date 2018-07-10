package cloud.zeroprox.railgun;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldown {

    private HashMap<UUID, Long> coolmap = new HashMap<>();

    public void setCoolmap(Player player, int seconds) {
        coolmap.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
        Sponge.getScheduler().createTaskBuilder().execute(task -> {
            coolmap.remove(player.getUniqueId());
            player.sendMessage(ChatTypes.ACTION_BAR, RailGun.getInstance().t_recharged.apply().build());
        }).delay(seconds, TimeUnit.SECONDS).submit(RailGun.getInstance());
    }

    public boolean hasCooldown(Player player) {
        return coolmap.containsKey(player.getUniqueId());
    }

    public Long getTimeLeft(Player player, int seconds) {
        long current = System.currentTimeMillis() / 1000L;
        return seconds - (current - coolmap.get(player.getUniqueId()));
    }

    public void resetCooldown(Player player) {
        coolmap.remove(player.getUniqueId());
    }
}
