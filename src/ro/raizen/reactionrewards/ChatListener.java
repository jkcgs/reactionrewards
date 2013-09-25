package ro.raizen.reactionrewards;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;

public class ChatListener implements Listener {
    private ReactionRewards plugin;

    public ChatListener(ReactionRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {

        // if the player has the permission to play
        if (e.getPlayer().hasPermission("reactionrewards.play")) {

            // If the question hasn't already been answered
            if (!plugin.getQuestionHandler().isExpired()) {

                // If the answer is correct
                if (isCorrect(e.getMessage())) {
                    String rewarded;

                    // Handle item and currency rewards differently
                    if (!plugin.getQuestionHandler().getReward().equalsIgnoreCase("money")) {
                        rewarded = plugin.getQuestionHandler().getReward();
                        rewarded = plugin.getQuestionHandler().getRewardAmount() + " " + rewarded;
                    } else {
                        rewarded =
                            ReactionRewards.getEconomy().format((double) plugin.getQuestionHandler().getRewardAmount());
                    }

                    int elapsedTime = (int) (plugin.getQuestionHandler().getTime()) / 1000;

                    // Handle item reward
                    if (!plugin.getQuestionHandler().getReward().equalsIgnoreCase("money")) {
                        String item = plugin.getQuestionHandler().getRewardId();
                        int    itemId;
                        short  damageValue;

                        plugin.getLogger().info(item);

                        // check if the item id has damage value
                        if (item.contains(":")) {
                            itemId      = Integer.parseInt(item.split(":")[0]);
                            damageValue = Short.parseShort(item.split(":")[1]);

                            ItemStack reward = new ItemStack(itemId, plugin.getQuestionHandler().getRewardAmount(),
                                                             damageValue);

                            e.getPlayer().getInventory().addItem(reward);
                            plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastWin"),
                                    e.getPlayer().getName(), elapsedTime));
                            e.getPlayer().sendMessage(String.format(plugin.getLang("sendWin"), rewarded));
                        } else {
                            itemId = Integer.parseInt(item);

                            ItemStack reward = new ItemStack(itemId, plugin.getQuestionHandler().getRewardAmount());

                            e.getPlayer().getInventory().addItem(reward);
                            plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastWin"),
                                    e.getPlayer().getName(), elapsedTime));
                            e.getPlayer().sendMessage(String.format(plugin.getLang("sendWin"), rewarded));
                        }
                    } else {    
                        // handle money reward
                        ReactionRewards.getEconomy().depositPlayer(e.getPlayer().getName(),
                                (double) plugin.getQuestionHandler().getRewardAmount());
                        plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastWin"),
                                e.getPlayer().getName(), elapsedTime));
                        e.getPlayer().sendMessage(String.format(plugin.getLang("sendWin"), rewarded));
                    }

                    // Add/update player entry in db with +1 wins
                    if (plugin.getDb().isSet(e.getPlayer().getName())) {
                        if(!plugin.getDb().updatePlayer(e.getPlayer().getName()))
                        	plugin.getLogger().severe("Could not update database");
                    } else {
                        if(!plugin.getDb().insertPlayer(e.getPlayer().getName()))
                        	plugin.getLogger().severe("Could not update database");
                    }
                }
            }
        }
    }

    private boolean isCorrect(String a) {
        String[] answers = plugin.getQuestionHandler().getAnswers();
        if(answers==null || a==null){
            return false;
        }
        String type = plugin.getQuestionHandler().getType();
        
        if (containsIgnoreCase(a, answers) || Arrays.asList(answers).contains(a)) {
            if(type.equals("alpha") // if not Case-Insensitive
                    && !plugin.getConfig().getBoolean("alphaCaseInsensitive")
                    && !a.equals(answers[0]))
                return false;
            plugin.getQuestionHandler().setExpired();
            return true;
        }

        return false;
    }

    private boolean containsIgnoreCase(String str, String[] list) {
        for (String i : list) {
            if (i.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }
}