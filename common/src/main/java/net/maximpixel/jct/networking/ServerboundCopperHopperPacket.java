package net.maximpixel.jct.networking;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.maximpixel.jct.block.entity.FilterType;
import net.maximpixel.jct.block.entity.FilterTypes;
import net.maximpixel.jct.inventory.CopperHopperMenu;
import net.maximpixel.jct.JustCopperTools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class ServerboundCopperHopperPacket extends BaseC2SMessage {

    private final ItemStack stack;
    private final FilterType filterType;

    public ServerboundCopperHopperPacket(ItemStack stack, FilterType filterType) {
        this.stack = stack;
        this.filterType = filterType;
    }

    public ServerboundCopperHopperPacket(FriendlyByteBuf friendlyByteBuf) {
        this.stack = friendlyByteBuf.readItem();
        String filterTypeStr = friendlyByteBuf.readUtf();
        this.filterType = Arrays.stream(FilterTypes.values())
                .filter(f -> f.toString().equals(filterTypeStr))
                .findAny()
                .orElse(FilterTypes.NONE);
    }

    @Override
    public MessageType getType() {
        return JustCopperTools.TEST_MESSAGE;
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeItem(stack);
        friendlyByteBuf.writeUtf(filterType.toString());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof CopperHopperMenu) {
            ((CopperHopperMenu) player.containerMenu).getFilterContainer().setFilterItem(stack);
            ((CopperHopperMenu) player.containerMenu).getFilterContainer().setFilterType(filterType);
        }
    }
}
