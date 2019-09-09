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
    
    //Structure used to save all the valuable information regarding to the player at the moment of warping between worlds
    public class PlayerSnapshot{
        String name;
        ItemStack[] storage,armor;
        Double health;
        int food;
        Location pos;
        public PlayerSnapshot(String name, ItemStack[] storage, ItemStack[] armor, Double health, int food, Location pos){
            this.name = name;
            this.storage = storage;
            this.armor = armor;
            this.health = health;
            this.food = food;
            this.pos = pos;
        }
    }
    
    //Map used for storing player's information.
    Map<String,PlayerSnapshot> playerMap = new HashMap<>();
    

    //In the near future, this method should load the PlayerMap from a file.
    public Map<String,PlayerSnapshot> getPlayersMap(){
        return this.playerMap;
    }
    
    public void savePlayersMap(Map<String,PlayerSnapshot> playerMap){
        this.playerMap = playerMap;
    }


    // Method used to send players to their Homeworld from the event
    //    This means that we need to give all their items back y place them where they were before the event

    public void sendPlayerHome(String name){
        //We ask the server to give us the Player object matching the name
        Player player = getServer().getPlayer(name);

        //Checks if the player is online. This shouldn't be necessary ... but we check it just in case.
        if(player == null){
            return;
        }

        //If we had previous information about where the player was, and what did he/she had  ... we have to return his/her belongings.
        if(this.playerMap.containsKey(name)){
            //Just for logging purposes
            PlayerSnapshot playerData = this.playerMap.get(name);
            player.sendMessage("Returning your items...");
            player.getInventory().setArmorContents(playerData.armor);
            player.getInventory().setContents(playerData.storage);
            player.setFoodLevel(playerData.food);
            player.setHealth(playerData.health);
            player.teleport(playerData.pos);
            this.playerMap.remove(name);
        }
        else{
            if (player.getBedSpawnLocation() == null){
                player.teleport(getServer().getWorld("world").getSpawnLocation());
            }
            else{
                player.teleport(player.getBedLocation());
            }
            player.getInventory().clear();
            player.setFoodLevel(10);
            player.setHealth(10);
        }
        player.setFallDistance(0);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public class MyListener implements Listener
    {
        
        @EventHandler
        public void onDamage(EntityDamageEvent e) {
            Entity ent = e.getEntity();
            if (ent instanceof Player){
                Player player = (Player) ent; 
                Double dmg = e.getDamage();
                if (player.getWorld().getName().equals("event") && player.getHealth()- dmg <= 0){
                    e.setCancelled(true);
                    blankplugin.this.sendPlayerHome(player.getName());
                }//end if hp<0
            }//end if instance player
        }
    }


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
        // this.inventories = new Map<String,Vector<ItemStack[]>>();
        Bukkit.getServer().createWorld(new WorldCreator("event"));
        getServer().getPluginManager().registerEvents(myLis, this);
    }

    @Override
    public void onDisable() {
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
                        this.playerMap.put(player.getName(),new PlayerSnapshot(player.getName(), player.getInventory().getContents(), player.getInventory().getArmorContents(), player.getHealth(), player.getFoodLevel(), player.getLocation()));
                        player.getInventory().clear();
                        player.teleport(new Location(Bukkit.getWorld("event"), 6, 6, 6));
                        player.setGameMode(GameMode.CREATIVE);
                    }
                    else if(args[0].equals("world")){
                        if(player.getWorld().getName().equals("event")){
                            this.sendPlayerHome(player.getName());
                        }
                        else{
                            sender.sendMessage("Solo podes volver, si te habias ido... Y vos no fuiste a ningun lado hombre verga.");
                        }
                    }
                    else{
                        sender.sendMessage("A donde queres ir papa? Vas a terminar en la villa 31 si seguis asi... Intenta con otro mundo.");
                    }
                }
            }
            return true;
        }
        return false;
    }
}   