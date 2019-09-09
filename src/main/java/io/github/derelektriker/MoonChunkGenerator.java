// package com.dinnerbone.bukkit.moon;
// https://github.com/Dinnerbone/BukkitFullOfMoon

package io.github.derelektriker;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
// import java.util.Vector;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
// import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.util.Vector;
import org.bukkit.util.BlockVector;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

public class MoonChunkGenerator extends ChunkGenerator {
    private NoiseGenerator generator;

    private NoiseGenerator getGenerator(World world) {
        if (generator == null) {
            generator = new SimplexNoiseGenerator(world);
        }

        return generator;
    }

    private int getHeight(World world, double x, double y, int variance) {
        NoiseGenerator gen = getGenerator(world);

        double result = gen.noise(x, y);
        result *= variance;
        return NoiseGenerator.floor(result);
    }
    
    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biome) {
        // byte[] result = new byte[32768];
        ChunkData chunk = createChunkData(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = getHeight(world, cx + x * 0.0625, cz + z * 0.0625, 2) + 60;
                for (int y = 0; y < height; y++) {
                    chunk.setBlock(x, y, z, Material.SPONGE);
                    // result[(x * 16 + z) * 128 + y] = (byte)Material.SPONGE.getId();
                }
            }
        }

        return chunk;
        // return result;
    }

    // public byte[] generate(World world, Random random, int cx, int cz) {
    //     byte[] result = new byte[32768];

    //     for (int x = 0; x < 16; x++) {
    //         for (int z = 0; z < 16; z++) {
    //             int height = getHeight(world, cx + x * 0.0625, cz + z * 0.0625, 2) + 60;
    //             for (int y = 0; y < height; y++) {
    //                 result[(x * 16 + z) * 128 + y] = (byte)Material.SPONGE.getId();
    //             }
    //         }
    //     }

    //     return result;
    // }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList((BlockPopulator)new MoonCraterPopulator(), new FlagPopulator());
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        int x = random.nextInt(200) - 100;
        int z = random.nextInt(200) - 100;
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x, y, z);
    }

    /**
     * Populators
     */
    
    public class MoonCraterPopulator extends BlockPopulator {
        private static final int CRATER_CHANCE = 45; // Out of 100
        private static final int MIN_CRATER_SIZE = 3;
        private static final int SMALL_CRATER_SIZE = 8;
        private static final int BIG_CRATER_SIZE = 16;
        private static final int BIG_CRATER_CHANCE = 10; // Out of 100
    
        public void populate(World world, Random random, Chunk source) {
            if (random.nextInt(100) <= CRATER_CHANCE) {
                int centerX = (source.getX() << 4) + random.nextInt(16);
                int centerZ = (source.getZ() << 4) + random.nextInt(16);
                int centerY = world.getHighestBlockYAt(centerX, centerZ);
                Vector center = new BlockVector(centerX, centerY, centerZ);
                int radius = 0;
    
                if (random.nextInt(100) <= BIG_CRATER_CHANCE) {
                    radius = random.nextInt(BIG_CRATER_SIZE - MIN_CRATER_SIZE + 1) + MIN_CRATER_SIZE;
                } else {
                    radius = random.nextInt(SMALL_CRATER_SIZE - MIN_CRATER_SIZE + 1) + MIN_CRATER_SIZE;
                }
    
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Vector position = center.clone().add(new Vector(x, y, z));
    
                            if (center.distance(position) <= radius + 0.5) {
                                world.getBlockAt(position.toLocation(world)).setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    public class FlagPopulator extends BlockPopulator {
        private static final int FLAG_CHANCE = 1; // Out of 200
        private static final int FLAG_HEIGHT = 3;
    
        public void populate(World world, Random random, Chunk source) {
            if (random.nextInt(200) <= FLAG_CHANCE) {
                int centerX = (source.getX() << 4) + random.nextInt(16);
                int centerZ = (source.getZ() << 4) + random.nextInt(16);
                int centerY = world.getHighestBlockYAt(centerX, centerZ);
                BlockFace direction = null;
                Block top = null;
                int dir = random.nextInt(4);
    
                if (dir == 0) {
                    direction = BlockFace.NORTH;
                } else if (dir == 1) {
                    direction = BlockFace.EAST;
                } else if (dir == 2) {
                    direction = BlockFace.SOUTH;
                } else {
                    direction = BlockFace.WEST;
                }
    
                for (int y = centerY; y < centerY + FLAG_HEIGHT; y++) {
                    top = world.getBlockAt(centerX, y, centerZ);
                    top.setType(Material.BIRCH_FENCE);
                }
    
                Block signBlock = top.getRelative(direction);
                signBlock.setType(Material.BIRCH_WALL_SIGN);
                BlockState state = signBlock.getState();
    
                if (state instanceof Sign) {
                    Sign sign = (Sign)state;
                    Directional data = (Directional) state.getData();
                    // org.bukkit.material.Sign data = (org.bukkit.material.Sign)state.getData();
                    data.setFacing(direction);
                    // data.setFacingDirection(direction);
                    sign.setLine(0, "---------|*****");
                    sign.setLine(1, "---------|*****");
                    sign.setLine(2, "-------------");
                    sign.setLine(3, "-------------");
                    sign.update(true);
                }
            }
        }
    }

}