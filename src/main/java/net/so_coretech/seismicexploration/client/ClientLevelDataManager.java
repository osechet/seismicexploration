package net.so_coretech.seismicexploration.client;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.so_coretech.seismicexploration.spread.SliceData;
import org.slf4j.Logger;

public final class ClientLevelDataManager {

  private static final Logger LOGGER = LogUtils.getLogger();

  private static @Nullable ClientLevelDataManager instance = null;

  private SliceData sliceData = new SliceData();
  private @Nullable Integer centerX = null;
  private @Nullable Integer centerZ = null;
  private @Nullable Integer axis = null;

  private ClientLevelDataManager() {}

  public static ClientLevelDataManager get() {
    if (instance == null) {
      instance = new ClientLevelDataManager();
    }
    return instance;
  }

  public SliceData getSlice() {
    return sliceData;
  }

  public void setSlice(final SliceData sliceData) {
    LOGGER.debug("Storing new slice in ClientLevelDataManager");
    this.sliceData = sliceData;
  }

  public Optional<Integer> getCenterX() {
    return Optional.ofNullable(centerX);
  }

  public Optional<Integer> getCenterZ() {
    return Optional.ofNullable(centerZ);
  }

  public Optional<Integer> getAxis() {
    return Optional.ofNullable(axis);
  }

  public void setRecorderParameters(final int centerX, final int centerZ, final int axis) {
    this.centerX = centerX;
    this.centerZ = centerZ;
    this.axis = axis;
  }
}
