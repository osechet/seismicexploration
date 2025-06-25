package net.so_coretech.seismicexploration.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.so_coretech.seismicexploration.ModFluids;

public class PetroleumItem extends BucketItem {
  public PetroleumItem(Item.Properties properties) {
    super(
        ModFluids.PETROLEUM.get(),
        properties.craftRemainder(Items.BUCKET).stacksTo(64).rarity(Rarity.COMMON));
  }
}
