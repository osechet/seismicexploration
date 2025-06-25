package net.so_coretech.seismicexploration.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.so_coretech.seismicexploration.ModFluids;

public class PetroleItem extends BucketItem {
  public PetroleItem(Item.Properties properties) {
    super(
        ModFluids.PETROLE.get(),
        properties.craftRemainder(Items.BUCKET).stacksTo(64).rarity(Rarity.COMMON));
  }
}
