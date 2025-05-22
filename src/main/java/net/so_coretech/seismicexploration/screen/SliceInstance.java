package net.so_coretech.seismicexploration.screen;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.spread.SliceData;

public class SliceInstance implements AutoCloseable {

    private final DynamicTexture texture;
    final ResourceLocation location;

    public SliceInstance() {
        this.texture = new DynamicTexture("slice", 320, 320, true);
        this.location =
                ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "slice/unique");
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    public void update(final SliceData data) {
        final NativeImage nativeimage = texture.getPixels();
        if (nativeimage != null) {
            for (int i = 0; i < 320; i++) {
                for (int j = 0; j < 320; j++) {
                    final int k = i * 320 + j;
                    nativeimage.setPixel(i, j, MapColor.getColorFromPackedId(data.colors[k]));
                }
            }
        }
        texture.upload();
    }

    @Override
    public void close() {
        this.texture.close();
    }
}
