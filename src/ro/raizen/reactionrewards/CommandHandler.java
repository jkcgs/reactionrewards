package ro.raizen.reactionrewards;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommandHandler implements CommandExecutor {
    private ReactionRewards plugin;

    public CommandHandler(ReactionRewards plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        String subCommand = (args.length < 1) ? "" : args[0];
        switch (subCommand) {
        case "leaderboard" :
            if (sender.hasPermission("reactionrewards.leaderboard")) {
                int limit = 5;

                if (plugin.getCfg("main").contains("leaderboardLimit")) {
                    limit = plugin.getCfg("main").getInt("leaderboardLimit");
                }

                if (!plugin.getDb().isEmpty()) {
                    ResultSet leaders = plugin.getDb().getTop(limit);
                    int count = 1;

                    try {
                        while (leaders.next()) {
                            sender.sendMessage(
                                String.format(
                                    plugin.getCfg("lang").getString("leaderboardTemplate"), count,
                                    leaders.getString("playername"), leaders.getInt("wins")));
                            count++;
                        }
                    } catch (SQLException e) {
                        plugin.log.info(plugin.parseString(e.getMessage()));
                    }
                } else {
                    sender.sendMessage(plugin.getLang("leaderboardEmpty"));
                }
            } else {
                sender.sendMessage(plugin.getLang("noPermission"));
            }

            break;

        case "stop" :
            if (sender.hasPermission("reactionrewards.stop")) {
                if (plugin.getCfg("main").getBoolean("enabled") == false) {
                    sender.sendMessage(plugin.getLang("notRunning"));
                } else {
                    plugin.getCfg("main").set("enabled", false);
                    plugin.saveCfg("main");
                    plugin.stopGen();
                    sender.sendMessage(plugin.getLang("genStopped"));
                }
            } else {
                sender.sendMessage(plugin.getLang("noPermission"));
            }

            break;

        case "start" :
            if (sender.hasPermission("reactionrewards.start")) {
                if (plugin.getCfg("main").getBoolean("enabled") == true) {
                    sender.sendMessage(plugin.getLang("alreadyRunning"));
                } else {
                    plugin.getCfg("main").set("enabled", true);
                    plugin.saveCfg("main");
                    plugin.startGen();
                    sender.sendMessage(plugin.getLang("genStarted"));
                }
            } else {
                sender.sendMessage(plugin.getLang("noPermission"));
            }

            break;

        case "reload" :
            if (sender.hasPermission("reactionrewards.reload")) {
                plugin.reloadConfigs();
                sender.sendMessage(plugin.getLang("configsReloaded"));
            } else {
                sender.sendMessage(plugin.getLang("noPermission"));
            }

            break;
        case "interval" :
            if (sender.hasPermission("reactionrewards.interval")) {
                if (args.length != 2) {
                    sender.sendMessage(plugin.getLang("intSyntaxError"));
                } else {
                    Boolean i = true; // is int?
                    int interval = 300;
                    try {
                        interval = Integer.parseInt(args[1]);
                    } catch(NumberFormatException nFE) {
                        i = false;
                        sender.sendMessage(plugin.getLang("intNotIntError"));
                    }
                    if (i){
                        if (interval < 1){
                            sender.sendMessage(plugin.getLang("intNotPositive"));
                        } else{
                            plugin.getCfg("main").set("generatorPeriod", interval);
                            plugin.saveConfig();
                            sender.sendMessage(plugin.getLang("intText") + interval);
                            if (plugin.getCfg("main").getBoolean("enabled") == false) {
                                sender.sendMessage(plugin.getLang("doItEffective"));
                            } else {
                                // making this setting effective
                                plugin.stopGen();
                                plugin.startGen();
                            }
                        }
                    }
                }
            } else {
                sender.sendMessage(plugin.getLang("noPermission"));
            }

            break;
        default :
            sender.sendMessage(plugin.parseString("Version " + plugin.getDescription().getVersion()));
            if (sender.hasPermission("reactionrewards.leaderboard")) {
                sender.sendMessage(plugin.getLang("infoLeaderboard"));
            }

            if (sender.hasPermission("reactionrewards.reload")) {
                sender.sendMessage(plugin.getLang("infoReload"));
            }
            
            if (sender.hasPermission("reactionrewards.start")) {
                sender.sendMessage(plugin.getLang("infoStart"));
            }
            
            if (sender.hasPermission("reactionrewards.stop")) {
                sender.sendMessage(plugin.getLang("infoStop"));
            }
            
            if (sender.hasPermission("reactionrewards.interval")) {
                sender.sendMessage(plugin.getLang("infoInterval"));
            }
        }

        return true;
    }
}
