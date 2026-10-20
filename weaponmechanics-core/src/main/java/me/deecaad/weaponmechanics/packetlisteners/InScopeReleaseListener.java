package me.deecaad.weaponmechanics.packetlisteners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag; // <-- Make sure to add this import!
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InScopeReleaseListener implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;

        WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

        // Action 5 = RELEASE_USE_ITEM
        if (wrapper.getAction() == DiggingAction.RELEASE_USE_ITEM) {
            Player player = (Player) event.getPlayer();

            WeaponMechanics.getInstance().getFoliaScheduler().entity(player).run(() -> {
                if (!player.isSneaking()) return;

                ItemStack mainStack = player.getInventory().getItemInMainHand();
                String weaponTitle = WeaponMechanics.getInstance().getWeaponHandler().getInfoHandler().getWeaponTitle(mainStack, false);

                if (weaponTitle != null) {

                    PlayerWrapper playerWrapper = WeaponMechanics.getInstance().getPlayerWrapper(player);
                    boolean dualWield = WeaponMechanics.getInstance().getWeaponHandler().getInfoHandler().getWeaponTitle(player.getInventory().getItemInOffHand(), false) != null;

                    WeaponMechanics.getInstance().getWeaponHandler().tryUses(
                            playerWrapper, weaponTitle, mainStack, EquipmentSlot.HAND, TriggerType.RIGHT_CLICK, dualWield, null
                    );
                }
            });
        }
    }
}