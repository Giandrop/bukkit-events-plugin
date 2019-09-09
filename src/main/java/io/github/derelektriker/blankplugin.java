package io.github.derelektriker;

// package {$TopLevelDomain}.{$Domain}.{$PluginName};
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.bukkit.event.Listener;

public final class blankplugin extends JavaPlugin {
    
    
    public class EventScheduler {
        List<Player> queue = new Vector<Player>();
        // int timeOut = 1800; //1:30m
        int timeOut = 600; //30s
        boolean started = false;
        boolean listening = false;

        public void BroadCast() {
            int minutes = timeOut / (60 * 20);
            int seconds = (timeOut / 20) % 60;
            String str = String.format("El evento comenzará en %d:%02d minutos", minutes, seconds);
            Bukkit.broadcastMessage(str);
            Bukkit.broadcastMessage("/enter para sumarse, /exit para bajarse");
            Bukkit.getScheduler().scheduleSyncDelayedTask(blankplugin.this, this.createLauncher(), timeOut);
            Bukkit.getScheduler().scheduleSyncDelayedTask(blankplugin.this, new Runnable() {
                public void run() {
                    Bukkit.broadcastMessage("5 segundos para que comience el evento");
                }
            }, timeOut - (20 * 5));

            listening = true;
        }

        private Runnable createLauncher() {
            return new Runnable() {
                public void run() {
                    Launch();
                }
            };
        }

        public void Launch() {
            listening = false;
            if (queue.size() < 1) {
                End("Pocos jugadores");
            } else {
                //Mueve los players al evento
                for (Player player:queue) {
                    if(player.getWorld().getName().equals("event")){
                        // player.sendMessage("Ya estas en Event... Rata"); 
                    } else {
                        player.sendMessage("Moviendo al evento");
                        ItemStack _armor[] = player.getInventory().getArmorContents();
                        ItemStack _cont[] = player.getInventory().getContents();
                        Vector<ItemStack[]> all = new Vector<ItemStack[]>();
                        all.add(_armor);
                        all.add( _cont);
                        blankplugin.this.playerInvMap.put(player.getName(), all);
                        blankplugin.this.playerLocMap.put(player.getName(), player.getLocation());
                        player.getInventory().clear();
                        player.teleport(new Location(Bukkit.getWorld("event"), 6, 6, 6));
                        // player.setGameMode(GameMode.CREATIVE);
                        blankplugin.this.myLis.update(playerInvMap, playerLocMap);
                    }
                }
                //Pequeño timeout para comenzar el evento
                Bukkit.getScheduler().scheduleSyncDelayedTask(blankplugin.this, new Runnable() {
                    public void run() {
                        Start();
                    };
                }, 10*3 );
            }
        }

        public void Start() {
            started = true;
            for (Player player:queue) {
                player.sendMessage("Que los juegos del hambre comiencen!");
            }
        }

        public void End(String reason) {
            if (reason.equalsIgnoreCase("winner")) {
                Player winner = event.queue.get(0);
                Bukkit.broadcastMessage(String.format("El ganador del evento es %s! Felicidades", winner.getName()));
                winner.damage(100000); //Forma rápida de transportarlo al mundo
            } else if (reason.equalsIgnoreCase("Pocos jugadores")) {
                for (Player player:queue) {
                    player.sendMessage("El evento se suspendió por falta de jugadores");
                }
            }
            queue.clear();
            started = false;
        }

    }

    EventScheduler event = new EventScheduler();
    
    public class MyListener implements Listener
    {
        
        Map<String,Vector<ItemStack[]>> playerInvMap = new HashMap<>();
        Map<String,Location> playerLocMap = new HashMap<>();

        public void update(Map<String,Vector<ItemStack[]>> a, Map<String,Location> b){
            playerInvMap = a;
            playerLocMap = b;
        }

        @EventHandler
        public void onDamage(EntityDamageEvent e) {
            Entity ent = e.getEntity();
            if (ent instanceof Player){
                Player player = (Player) ent; 
                Double dmg = e.getDamage();
                if (player.getWorld().getName().equals("event") && player.getHealth()- dmg <= 0){
                    e.setCancelled(true);
                    if(this.playerLocMap.containsKey(player.getName())){
                        player.sendMessage("Returning your items...");
                        Vector<ItemStack[]> all = this.playerInvMap.get(player.getName());
                        player.getInventory().setArmorContents(all.get(0));
                        player.getInventory().setContents(all.get(1));
                        player.teleport(this.playerLocMap.get(player.getName()));
                        this.playerInvMap.remove(player.getName());
                        this.playerLocMap.remove(player.getName());
                    }
                    else{
                        player.teleport(getServer().getWorld("world").getSpawnLocation());
                        player.getInventory().clear();
                    }
                    player.setFoodLevel(10);
                    player.setHealth(10);
                    player.setFallDistance(0);
                    player.setGameMode(GameMode.SURVIVAL);
                }//end if hp<0

                if (event.started && event.queue.contains(player)) {
                    event.queue.remove(player);
                    if (event.queue.size() < 2) {
                        event.End("Winner");
                    }
                }
                
            }//end if instance player
        }



        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event){
            Player player = event.getEntity().getPlayer();
            
            if(player.getWorld().getName().equals("event")){
                player.setHealth(10);
                player.setFoodLevel(10);
                if(this.playerLocMap.containsKey(player.getName())){
                    player.sendMessage("Returning your items...");
                    Vector<ItemStack[]> all = this.playerInvMap.get(player.getName());
                    player.getInventory().setArmorContents(all.get(0));
                    player.getInventory().setContents(all.get(1));
                    player.teleport(this.playerLocMap.get(player.getName()));
                    this.playerInvMap.remove(player.getName());
                    this.playerLocMap.remove(player.getName());
                }
                else{
                    player.teleport(getServer().getWorld("world").getSpawnLocation());
                    player.getInventory().clear();
                }
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent event){
            Player player = event.getPlayer();
            player.sendMessage("Taking you where you belong...");
            if(this.playerLocMap.containsKey(player.getName())){
                player.sendMessage("Returning your items...");
                Vector<ItemStack[]> all = this.playerInvMap.get(player.getName());
                player.getInventory().setArmorContents(all.get(0));
                player.getInventory().setContents(all.get(1));
                
                player.teleport(this.playerLocMap.get(player.getName()));
                this.playerInvMap.remove(player.getName());
                this.playerLocMap.remove(player.getName());
            }
            else{
                player.teleport(getServer().getWorld("world").getSpawnLocation());
                player.getInventory().clear();
            }
            player.setGameMode(GameMode.SURVIVAL);
            
        }
    }
    
    Map<String,Vector<ItemStack[]>> playerInvMap = new HashMap<>();
    Map<String,Location> playerLocMap = new HashMap<>();
    MyListener myLis = new MyListener();
   
    public void deleteMap(String mapname){
        World world = Bukkit.getServer().getWorld(mapname);
        getLogger().info("unloading " + mapname);
        getLogger().info(String.valueOf(Bukkit.getServer().unloadWorld(world, false)));
        getLogger().info("deleting " + mapname);
        getLogger().info(String.valueOf(deleteFolder(world.getWorldFolder())));
    }

    public static boolean deleteFolder(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFolder(files[i]);
                } else {
                        files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    //Unloading maps, to rollback maps. Will delete all player builds until last server save
    public static void unloadMap(String mapname){
        if(Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(mapname), false)){
        
        }else{
        
        }
    }
    //Loading maps (MUST BE CALLED AFTER UNLOAD MAPS TO FINISH THE ROLLBACK PROCESS)
    public static void loadMap(String mapname){
        Bukkit.getServer().createWorld(new WorldCreator(mapname));
        World world = Bukkit.getWorld(mapname);
        world.setAutoSave(false);
    }
 
    //Maprollback method, because were too lazy to type 2 lines
    public static void rollback(String mapname){
        unloadMap(mapname);
        loadMap(mapname);
    }

    @Override
    public void onEnable() {
    // TODO Insert logic to be performed when the plugin is enabled
        // this.inventories = new Map<String,Vector<ItemStack[]>>();
        Bukkit.getServer().createWorld(new WorldCreator("event"));
        getServer().getPluginManager().registerEvents(myLis, this);
    }

    @Override
    public void onDisable() {
    // TODO Insert logic to be performed when the plugin is disabled
        // Bukkit.getServer().unloadWorld("event", true);
        // Bukkit.getServer().reload();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sss")) { // If the player typed /basic then do the following...
            // do something...
            // Bukkit.broadcastMessage("Niro es Puto.");

            // this.rollback("event");
            // Player target = (Bukkit.getServer().getPlayer(args[0]));
        } else if (cmd.getName().equalsIgnoreCase("enter")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
            } else {
                Player player = (Player) sender;
                if (!event.listening) {
                    player.sendMessage("No hay ningun evento que aceptar");
                } else {
                    event.queue.add(player);
                    player.sendMessage("Te suscribiste al evento");
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("exit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
            } else {
                Player player = (Player) sender;
                if (!event.listening) {
                    player.sendMessage("No hay ningun evento del que salir");
                } else if (event.queue.contains(player)) {
                    event.queue.remove(player);
                    player.sendMessage("Te desuscribiste del evento");
                } else {
                    player.sendMessage("No estás registrado en el evento");
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("broadcast")) {
            event.BroadCast();
        } else if (cmd.getName().equalsIgnoreCase("delete_world")) {
            if(args.length != 1){
                sender.sendMessage("Número incorrecto de argumentos");
            } else {
                deleteMap(args[0]);
            }
            for(World world : Bukkit.getServer().getWorlds()) {
                sender.sendMessage(world.getName());
            }

        } else if (cmd.getName().equalsIgnoreCase("world_list")) {

            for(World world : Bukkit.getServer().getWorlds()) {
                sender.sendMessage(world.getName());
            }

        } else if (cmd.getName().equalsIgnoreCase("create_world")) {
            if(args.length == 0){
                sender.sendMessage("Faltan argumentos");
            } else { //Se ejecuta el comando
                World newWorld;
                WorldCreator wCreator = new WorldCreator(args[0]);
                if (args.length == 1) {
                    wCreator.generator(new CustomChunkGenerator());
                } else {
                    String generator = args[1];
                    if (generator.equalsIgnoreCase("moon")) {
                        sender.sendMessage("Creando la luna");
                        wCreator.generator(new MoonChunkGenerator());
                    } else if (generator.equalsIgnoreCase("craters")) {
                        sender.sendMessage("Creando crateres");
                        wCreator.generator(new AnotherCustomChunkGenerator());
                    }
                }
                newWorld = this.getServer().createWorld(wCreator);

                Player player = (Player) sender;
                player.teleport(newWorld.getSpawnLocation());
            }
        } else if (cmd.getName().equalsIgnoreCase("move")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
            } else {
                Player player = (Player) sender;

                if(args.length != 1){
                    sender.sendMessage("Estas haciendo las cosas mal papa, es '/move nombredemapa'.");
                }
                else{
                    sender.sendMessage("'"+args[0]+"'");
                    if (args[0].equals("event")){
                        if(player.getWorld().getName().equals("event")){
                            sender.sendMessage("Ya estas en Event... Rata"); 
                            return true;
                        }
                        sender.sendMessage("Moviendo al mapa Event.");
                        ItemStack _armor[] = player.getInventory().getArmorContents();
                        ItemStack _cont[] = player.getInventory().getContents();
                        Vector<ItemStack[]> all = new Vector<ItemStack[]>();
                        all.add(_armor);
                        all.add( _cont);
                        this.playerInvMap.put(player.getName(), all);
                        this.playerLocMap.put(player.getName(), player.getLocation());
                        player.getInventory().clear();
                        player.teleport(new Location(Bukkit.getWorld("event"), 6, 6, 6));
                        player.setGameMode(GameMode.CREATIVE);
                        this.myLis.update(playerInvMap, playerLocMap);
                    }
                    else if(args[0].equals("world")){
                        if(player.getWorld().getName().equals("event")){
                            if (this.playerInvMap.containsKey(player.getName())){
                                Vector<ItemStack[]> all = this.playerInvMap.get(player.getName());
                                player.getInventory().setArmorContents(all.get(0));
                                player.getInventory().setContents(all.get(1));
                                player.teleport(this.playerLocMap.get(player.getName()));
                                this.playerInvMap.remove(player.getName());
                                this.playerLocMap.remove(player.getName());
                                this.myLis.update(playerInvMap, playerLocMap);
                            }
                            else{
                                player.teleport(getServer().getWorld("world").getSpawnLocation());
                                player.getInventory().clear();
                            }
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                        else{
                            sender.sendMessage("Solo podes volver, si te habias ido... Y vos no fuiste a ningun lado hombre verga.");
                        }
                    }
                    else{
                        sender.sendMessage("A donde queres ir papa? Vas a terminar en la villa 31 si seguis asi... Intenta con otro mundo.");
                    }
                }
                // do something
            }
            return true;
        }
        return false;
    }

    public class CustomChunkGenerator extends ChunkGenerator {
        int currentHeight = 50;
    
        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
            ChunkData chunk = createChunkData(world);
            generator.setScale(0.005D);
    
            for (int X = 0; X < 16; X++)
                for (int Z = 0; Z < 16; Z++) {
                    currentHeight = (int) (generator.noise(chunkX*16+X, chunkZ*16+Z, 0.5D, 0.5D)*15D+50D);
                    chunk.setBlock(X, currentHeight, Z, Material.GRASS);
                    chunk.setBlock(X, currentHeight-1, Z, Material.DIRT);
                    for (int i = currentHeight-2; i > 0; i--)
                        chunk.setBlock(X, i, Z, Material.STONE);
                    chunk.setBlock(X, 0, Z, Material.BEDROCK);
                }
            return chunk;
        }
    }

    public class AnotherCustomChunkGenerator extends ChunkGenerator {
        int currentHeight = 50;
    


        @Override
        public List<BlockPopulator> getDefaultPopulators(World world) {
            return Arrays.asList((BlockPopulator)new CraterPopulator());
        }

        public class CraterPopulator extends BlockPopulator {
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
                    org.bukkit.util.Vector center = new BlockVector(centerX, centerY, centerZ);
                    int radius = 0;
        
                    if (random.nextInt(100) <= BIG_CRATER_CHANCE) {
                        radius = random.nextInt(BIG_CRATER_SIZE - MIN_CRATER_SIZE + 1) + MIN_CRATER_SIZE;
                    } else {
                        radius = random.nextInt(SMALL_CRATER_SIZE - MIN_CRATER_SIZE + 1) + MIN_CRATER_SIZE;
                    }
        
                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -radius; y <= radius; y++) {
                            for (int z = -radius; z <= radius; z++) {
                                org.bukkit.util.Vector position = center.clone().add(new org.bukkit.util.Vector(x, y, z));
        
                                if (center.distance(position) <= radius + 0.5) {
                                    world.getBlockAt(position.toLocation(world)).setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
        }
    

    }
}   