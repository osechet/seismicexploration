package net.so_coretech.seismicexploration.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class MemsItem extends Item {
  public MemsItem(Item.Properties properties) {
    super(properties.rarity(Rarity.COMMON).stacksTo(64));
  }
}
