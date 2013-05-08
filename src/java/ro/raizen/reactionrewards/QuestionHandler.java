package ro.raizen.reactionrewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class QuestionHandler {
    private StopWatch       timer = new StopWatch();
    private String[]        types = { "trivia", "alpha", "num", "equation" };
    private ReactionRewards plugin;
    private String          type;
    private String          question;
    private String[]        answers;
    private String          reward_id;
    private int             reward_amount;
    private String          reward_name;
    private boolean         expired;
    private Random          random;
    
    private static int rand(int Min, int Max){ // number between this
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }
    private static int op(){ // 0 - 1
        return rand(0,1);
    }
    private static int pI(String var){
        return Integer.parseInt(var);
    }

    public QuestionHandler(ReactionRewards plugin) {
        this.plugin = plugin;
        random      = new Random();
    }

    public void generateQuestion() {
        int alphaLength   = 10,
            mathMaxLength = 4;

        if (plugin.getCfg("main").getStringList("questionTypes").size() > 0) {
            types = plugin.getCfg("main").getStringList("questionTypes").toArray(new String[0]);
        }

        if (plugin.getCfg("main").getInt("alphaLength") > 0) {
            alphaLength = plugin.getCfg("main").getInt("alphaLength");
        }

        if (plugin.getCfg("main").getInt("mathMaxLength") > 0) {
            mathMaxLength = plugin.getCfg("main").getInt("mathMaxLength");
        }

        // get a random question type
        type = types[random.nextInt(types.length)];

        switch (type) {
            case "trivia" :
                generateTriviaQuestion();
                break;

            case "alpha" :
                generateRandomString(alphaLength);
                break;

            case "math" :
                generateMathQuestion(mathMaxLength);
                break;
                
            case "equation" :
                generateEquation();
                break;
        }

        generateReward();
        expired = false;

        // reset and start the timer
        timer.reset();
        timer.start();
    }

    private void generateRandomString(int length) {
        String alphaNum     = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZ";
        String randomString = "";

        for (int i = 0; i < length; i++) {
            randomString += alphaNum.charAt(random.nextInt(alphaNum.length()));
        }

        question   = randomString;
        answers    = new String[1];
        answers[0] = question;
    }

    private void generateMathQuestion(int length) {
        int[] mult = {-1, 1}; // basic maths
        int[] numbers = {0, 0};
        int sign, op = op(); //0 - minus, 1 - plus
        // save numbers to an array
        for (int i=0; i<2; i++){
            sign = mult[random.nextInt(2)]; // positive or negative?
            numbers[i] = rand(1, (int)Math.pow(10,length+1)-1) * sign; // save number on array
        }

        if(op==0) {
            question = String.format("%s - %s", numbers[0], numbers[1]);
            answers = new String[1];
            answers[0] = Integer.toString(numbers[0] - numbers[1]);
        } else {
            question = String.format("%s + %s", numbers[0], numbers[1]);
            answers = new String[1];
            answers[0] = Integer.toString(numbers[0] + numbers[1]);
        }
    }

    private void generateTriviaQuestion() {
        String[] questions = plugin.getCfg("trivia").getKeys(false).toArray(new String[0]);

        question = questions[random.nextInt(questions.length)];
        answers  = plugin.getCfg("trivia").getStringList(question).toArray(new String[0]);
    }
    
    private void generateEquation(){
        int[] sign = {1, -1};
        String[] operator = {"+", "-"}, pos = new String[5];
        String[] operators = {operator[op()], "="};
        ArrayList<String> opList = new ArrayList(Arrays.asList(operators));
        Collections.shuffle(opList, new Random(System.nanoTime()));
        
        // TODO: Make this configurable
        int a = rand(50, 500) * sign[op()];
        int b = rand(50, 500) * sign[op()];
        
        String[] vars = {a+"", b+"", "x"};
        ArrayList<String> varsList = new ArrayList(Arrays.asList(vars));
        Collections.shuffle(varsList, new Random(System.nanoTime()));
        
        for (int i = 0; i<5; i+=2){
            if(i>0){
                // i-1, tomará 1 y 3
                // 1=0, 3=1
                int x = (i-1 == 1)? 0 : 1;
                pos[i-1] = opList.get(x);
            }
            pos[i] = varsList.get(i/2);
        }
        
        question = "";
        for(int i = 0; i<5; i++)
            question = question + pos[i] + " ";
        
        // 0, 2, 4 = var,const; 1, 3 = operador o "="
        int answer = 0;
        
        int d,e,f; // la comprobación de índices dependerá de la posición de 'x'
        
        // para formatos: a +/- b = x, x = a +/- b
        if ((pos[1] == "=" && pos[0] == "x") || (pos[3] == "=" && pos[4] == "x")){
            if (pos[0] == "x") { // x está al principio?
                d=3; e=2; f=4; 
            } else { // o al final?
                d=1; e=0; f=2; 
            }
            answer = (pos[d] == "+") ? pI(pos[e]) + pI(pos[f]) : pI(pos[e]) - pI(pos[f]);
        } else {
            if (pos[1] == "=") { // el signo igual está a la izquierda
                d=4;e=0;f=2;
            } else { // o a la derecha?
                d=0;e=4;f=0;
            }
            int constant = (pos[2] == "x") ? pI(pos[d]) : pI(pos[2]);
            
            // y si al lado izquierdo de la constante, teniendo x al principio, y el "=" a la derecha? entonces es su inverso positivo
            if (pos[0+f] == "x" && pos[1+f] == "-")
                constant = constant * -1;
            
            answer = pI(pos[e]) + constant * -1;
            
            // y si al lado izquierdo de la x hay un "-"? significa que la respuesta es su inverso aditivo
            if (pos[1+f] == "-" && pos[2+f] == "x")
                answer = answer * -1;
        }
        answers = new String[1];
        answers[0] = answer + "";
    }

    private void generateReward() {
        Set<String> rewardSet = plugin.getCfg("rewards").getKeys(false);
        String[]    rewards   = rewardSet.toArray(new String[0]);

        reward_name = rewards[random.nextInt(rewards.length)];

        int max = plugin.getCfg("rewards").getInt(reward_name + ".maxAmount");

        if (!reward_name.equalsIgnoreCase("money")) {
            reward_id = plugin.getCfg("rewards").getString(reward_name + ".itemId");
        }

        if (max != 0) {
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