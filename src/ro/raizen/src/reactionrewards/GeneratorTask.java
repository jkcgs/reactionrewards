package ro.raizen.src.reactionrewards;

public class GeneratorTask implements Runnable {
	
	private ReactionRewards plugin;
	
	public GeneratorTask(ReactionRewards plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
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
}
