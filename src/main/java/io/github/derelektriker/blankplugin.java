package io.github.derelektriker;
// package {$TopLevelDomain}.{$Domain}.{$PluginName};
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.Listener;

public final class blankplugin extends JavaPlugin {
    
    
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
   
    //Unloading maps, to rollback maps. Will delete all player builds until last server save
    public static void unloadMap(String mapname){
        if(Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(mapname), false)){
        
        }else{
        }
    }
    //Loading maps (MUST BE CALLED AFTER UNLOAD MAPS TO FINISH THE ROLLBACK PROCESS)
    public static void loadMap(String mapname){
        Bukkit.getServer().createWorld(new WorldCreator(mapname));
        World world= Bukkit.getWorld(mapname);
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
            
            

            // return true;
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
}   