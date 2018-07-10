package cloud.zeroprox.railgun;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Listeners {

    ParticleEffect effectKill = ParticleEffect.builder()
            .type(ParticleTypes.DRAGON_BREATH)
            .quantity(8)
            .offset(new Vector3d(0.7, 0.7, 0.7))
            .build();

    ParticleEffect effectSpark = ParticleEffect.builder()
            .type(ParticleTypes.FIREWORKS_SPARK)
            .quantity(1)
            .build();

    ParticleEffect effectSpell = ParticleEffect.builder()
            .type(ParticleTypes.SPELL)
            .quantity(1)
            .build();

    Cooldown cooldown = new Cooldown();

    @Listener
    public void onInteract(InteractItemEvent event, @First Player player) {
        if (player == null) {
            return;
        }
        if (!player.hasPermission("railgun.use")) {
            return;
        }
        if (!player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty()).equalTo(RailGun.getInstance().s_item)) {
            return;
        }
        if (cooldown.hasCooldown(player)) {
            player.sendMessage(ChatTypes.ACTION_BAR, RailGun.getInstance().t_recharging.apply().build());
            return;
        }

        Optional<EyeLocationProperty> eye = player.getProperty(EyeLocationProperty.class);
        Transform loc = player.getTransform();

        double px = eye.get().getValue().getX();
        double py = eye.get().getValue().getY();
        double pz = eye.get().getValue().getZ();
        double yaw = Math.toRadians(loc.getYaw() + 90.0F);
        double pitch = Math.toRadians(loc.getPitch() + 90.0F);

        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);

        int complete = 0;
        int last = 0;
        for (int i = 1; i <= RailGun.getInstance().s_range; i++) {
            Vector3d newLoc = new Vector3d(px + i * x, py + i * z, pz + i * y);
            Transform<World> transform = new Transform(player.getWorld(), newLoc);


            complete += shotplayer(player, transform, 1.5D);
            if (complete != last) {
            }
            last = complete;

            transform.getExtent().spawnParticles(effectSpell, transform.getPosition());
        }

        cooldown.setCoolmap(player, RailGun.getInstance().s_cooldown);

        if (RailGun.getInstance().s_cancel_event) {
            event.setCancelled(true);
        }
    }

    public int shotplayer(Player shooter, Transform<World> loc, Double focus) {
        AtomicInteger compte = new AtomicInteger();

        loc.getExtent().getEntities(entity -> distance(entity.getTransform(), loc) < focus && entity.getType() != EntityTypes.FIREWORK).forEach(entity -> {
            if (entity == shooter) return;
            loc.getExtent().spawnParticles(effectKill, loc.getPosition());
            if (RailGun.getInstance().s_fireworks) {
                spawnFirework(FireworkEffect.builder().colors(Color.BLUE).shape(FireworkShapes.BURST).build(), loc);
            } else {
                loc.getExtent().spawnParticles(effectKill, loc.getPosition());
            }
            loc.getExtent().playSound(SoundTypes.ENTITY_VILLAGER_HURT, loc.getPosition(), 1000d, 10d);
            compte.getAndIncrement();
            entity.setVelocity(new Vector3d(0, 1.2, 0));
        });

        return compte.get();
    }

    public double distance(Transform<World> origin, Transform<World> check) {
        if (origin.getExtent() != check.getExtent()) return Double.MAX_VALUE;
        return origin.getPosition().distance(check.getPosition());
    }

    public static void spawnFirework(FireworkEffect effect, Transform<World> location) {
        Entity firework = location.getExtent().createEntity(EntityTypes.FIREWORK, location.getPosition());
        firework.offer(Keys.FIREWORK_EFFECTS, Arrays.asList(effect));
        location.getExtent().spawnEntity(firework);
    }
}
