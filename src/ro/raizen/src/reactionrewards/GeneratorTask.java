package ro.raizen.src.reactionrewards;

import java.util.Random;

public class GeneratorTask implements Runnable {
	
	private ReactionRewards plugin;
	public int taskId;
	
	public GeneratorTask(ReactionRewards plugin) {
		this.plugin = plugin;
	}
	
	public void setId(int n) {
		this.taskId = n;
	}
	
	@Override
	public void run() {
		int numPlayers = plugin.getServer().getOnlinePlayers().length;
		if(numPlayers >= plugin.getCfg("main").getInt("minPlayersOnline")) {
			//Generate new question
			plugin.getQuestionHandler().generateQuestion();
			String reward;
			//Handle item and currency rewards differently
			if(!plugin.getQuestionHandler().getReward().equalsIgnoreCase("money")) {
				reward = plugin.getQuestionHandler().getReward();
				reward = plugin.getQuestionHandler().getRewardAmount() + " " + reward; 
			}
			else {
				reward = ReactionRewards.getEconomy().format((double) plugin.getQuestionHandler().getRewardAmount());
			}
			
			//Broadcast the question to the server
			switch(plugin.getQuestionHandler().getType()) {
				case "trivia":
					plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastTrivia"), reward));
					plugin.getServer().broadcastMessage(plugin.getQuestionHandler().getQuestion());
					break;
				case "alpha":
					plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastAlpha"), plugin.getQuestionHandler().getQuestion(), reward));
					break;
				case "math":
					
					plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastMath"), plugin.getQuestionHandler().getQuestion(), reward));
					break;
			}
		}
		
		int period = 300;
		if(ReactionRewards.isNumeric(plugin.getCfg("main").getString("generatorPeriod"))) {
			if(plugin.getCfg("main").getInt("generatorPeriod") > 0)
				period = plugin.getCfg("main").getInt("generatorPeriod");			
		} else {
			if(plugin.getCfg("main").getString("generatorPeriod").contains("-")) {
				Random random = new Random();
				String[] minMax = plugin.getCfg("main").getString("generatorPeriod").split("-");
				int minP = Integer.parseInt(minMax[0].trim());
				int maxP = Integer.parseInt(minMax[1].trim());
				period = random.nextInt(maxP- minP + 1) + minP;
			}
		}
		
		if(plugin.getCfg("main").getBoolean("enabled") == true) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20*period);
		}
	}
}
