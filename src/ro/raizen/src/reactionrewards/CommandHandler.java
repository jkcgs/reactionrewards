package ro.raizen.src.reactionrewards;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {
	private ReactionRewards plugin;
	
	public CommandHandler(ReactionRewards plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) 
	{
		if(args.length < 1) {
			if(sender.hasPermission("reactionrewards.leaderboard")) {
				sender.sendMessage(plugin.parseString("/rr leaderboard - See players who won the most times"));	
			}
			if(sender.hasPermission("tranker.reload")) {
				sender.sendMessage(plugin.parseString("/rr reload - Reload config"));
			}
		} else {
			String subCommand = args[0];
			switch(subCommand) {
				case "leaderboard": 
					
					if(sender.hasPermission("reactionrewards.leaderboard")) {
						int limit = 5;
						if(plugin.getCfg("main").contains("leaderboardLimit")) {
							limit = plugin.getCfg("main").getInt("leaderboardLimit");
						}
						if(!plugin.getDb().isEmpty()) {
							ResultSet leaders = plugin.getDb().getTop(limit);
							int count = 1;
							try {
								while(leaders.next()) {
									sender.sendMessage(String.format(plugin.getCfg("lang").getString("leaderboardTemplate"), count, leaders.getString("playername"), leaders.getInt("wins")));
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
				case "stop":
					if(sender.hasPermission("reactionrewards.stop")) {
						if(plugin.getCfg("main").getBoolean("enabled") == false) {
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
				case "start":
					if(sender.hasPermission("reactionrewards.start")) {
						if(plugin.getCfg("main").getBoolean("enabled") == true) {
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
				case "reload": 
					
					if(sender.hasPermission("reactionrewards.reload")) {
						plugin.reloadConfigs();
						sender.sendMessage(plugin.getLang("configsReloaded"));
					} else {
						sender.sendMessage(plugin.getLang("noPermission"));
					}
					
					break;
				default:
					if(sender.hasPermission("reactionrewards.leaderboard")) {
						sender.sendMessage(plugin.parseString("/rr leaderboard - See players who won the most times"));	
					}
					if(sender.hasPermission("tranker.reload")) {
						sender.sendMessage(plugin.parseString("/rr reload - Reload config"));
					}					
			}
		}
		return true;
	}
	
}
