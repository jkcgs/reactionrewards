package ro.raizen.src.reactionrewards;

import java.util.Random;
import java.util.Set;

public class QuestionHandler {
	
	private ReactionRewards plugin;
	
	private String type;
	private String question;
	private String[] answers;
	private String reward_id;
	private int reward_amount;
	private String reward_name;
	private boolean expired;
	private StopWatch timer = new StopWatch();
	
	private String[] types = {"trivia", "alpha", "num"};
	private Random random;
	
	public QuestionHandler(ReactionRewards plugin) {
		this.plugin = plugin;
		random = new Random();
	}
	
	public void generateQuestion() {
		
		int alphaLength = 10, mathMaxLength = 4;
		
		if(plugin.getCfg("main").getStringList("questionTypes").size() > 0) {
			types = plugin.getCfg("main").getStringList("questionTypes").toArray(new String[0]);
		}
		if(plugin.getCfg("main").getInt("alphaLength") > 0) {
			alphaLength = plugin.getCfg("main").getInt("alphaLength");
		}
		if(plugin.getCfg("main").getInt("mathMaxLength") > 0) {
			mathMaxLength = plugin.getCfg("main").getInt("mathMaxLength");
		}
		
		//get a random question type
		type = types[random.nextInt(types.length)];
		
		switch(type) {
			case "trivia":
				generateTriviaQuestion();
				break;
			case "alpha":
				generateRandomString(alphaLength); 
				break;
			case "math":
				generateMathQuestion(mathMaxLength);
				break;
		}
		
		generateReward();
		
		expired = false;
		
		//reset and start the timer 
		timer.reset();
		timer.start();
	}
	
	private void generateRandomString(int length) {
		String alphaNum = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZ";
		String randomString = "";
			
		for(int i=0;i<length;i++) {
			randomString += alphaNum.charAt(random.nextInt(alphaNum.length()));
		}
		
		question = randomString;
		answers = new String[1];
		answers[0] = question;
	}
	
	private void generateMathQuestion(int length) {
		
		int one, two, op, l = (int) Math.pow(10, length);
		one = random.nextInt(l*2) - l;
		two = random.nextInt(l*2) - l; 
		op = random.nextInt(2); //0 - minus, 1 - plus
		if(op == 0) {
			if(two < 0) {
				question = one + " + " + Math.abs(two);
			}
			else {
				question = one + " - " + two;
			}
			answers = new String[1];
			answers[0] = Integer.toString(one - two);
			
		}
		else { 
			if(two < 0) {
				question = one + " - " + Math.abs(two);
			}
			else {
				question = one + " - " + two;
			}
			answers = new String[1];
			answers[0] = Integer.toString(one + two);
		}
	}
	
	private void generateTriviaQuestion() {
		String[] questions = plugin.getCfg("trivia").getKeys(false).toArray(new String[0]);
		question = questions[random.nextInt(questions.length)];
		answers = plugin.getCfg("trivia").getStringList(question).toArray(new String[0]);
	}
	
	private void generateReward() {
		Set<String> rewardSet = plugin.getCfg("rewards").getKeys(false);
		String[] rewards = rewardSet.toArray(new String[0]);
		reward_name = rewards[random.nextInt(rewards.length)];
		int max = plugin.getCfg("rewards").getInt(reward_name + ".maxAmount");
		if(!reward_name.equalsIgnoreCase("money")) {
			reward_id = plugin.getCfg("rewards").getString(reward_name + ".itemId");
		}
		if(max != 0) {
			reward_amount = random.nextInt(max) + 1;
		} else {
			reward_amount = random.nextInt(64) + 1;
		}
	}
	
	public String getType() {
		return type;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public String[] getAnswers() {
		return answers;
	}
	
	public String getReward() {
		return reward_name;
	}
	
	public int getRewardAmount() {
		return reward_amount;
	}
	
	public String getRewardId() {
		return reward_id;
	}
	
	public boolean isExpired() {
		return expired;
	}
	
	public void setExpired() {
		timer.stop();
		expired = true;
	}
	
	public long getTime() {
		return timer.getTime();
	}
	
}
