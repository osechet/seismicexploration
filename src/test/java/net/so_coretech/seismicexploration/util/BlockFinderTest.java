package net.so_coretech.seismicexploration.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.minecraft.core.Vec3i;
import org.junit.jupiter.api.Test;

public class BlockFinderTest {

  @Test
  public void testListBlocks_StraightLineX() {
    Vec3i start = new Vec3i(0, 0, 0);
    Vec3i end = new Vec3i(3, 0, 0);
    List<Vec3i> result = BlockFinder.listBlocks(start, end);
    assertEquals(
        List.of(new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(2, 0, 0), new Vec3i(3, 0, 0)),
        result);
  }

  @Test
  public void testListBlocks_StraightLineY() {
    Vec3i start = new Vec3i(0, 0, 0);
    Vec3i end = new Vec3i(0, 3, 0);
    List<Vec3i> result = BlockFinder.listBlocks(start, end);
    assertEquals(
        List.of(new Vec3i(0, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, 2, 0), new Vec3i(0, 3, 0)),
        result);
  }

  @Test
  public void testListBlocks_StraightLineZ() {
    Vec3i start = new Vec3i(0, 0, 0);
    Vec3i end = new Vec3i(0, 0, 3);
    List<Vec3i> result = BlockFinder.listBlocks(start, end);
    assertEquals(
        List.of(new Vec3i(0, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, 2), new Vec3i(0, 0, 3)),
        result);
  }

  @Test
  public void testListBlocks_Diagonal() {
    Vec3i start = new Vec3i(0, 0, 0);
    Vec3i end = new Vec3i(2, 2, 2);
    List<Vec3i> result = BlockFinder.listBlocks(start, end);
    assertTrue(result.contains(new Vec3i(0, 0, 0)));
    assertTrue(result.contains(new Vec3i(1, 1, 1)));
    assertTrue(result.contains(new Vec3i(2, 2, 2)));
    assertEquals(result.get(0), start);
    assertEquals(result.get(result.size() - 1), end);
  }

  @Test
  public void testListBlocks_SamePoint() {
    Vec3i start = new Vec3i(1, 2, 3);
    Vec3i end = new Vec3i(1, 2, 3);
    List<Vec3i> result = BlockFinder.listBlocks(start, end);
    assertEquals(List.of(new Vec3i(1, 2, 3)), result);
  }
}
