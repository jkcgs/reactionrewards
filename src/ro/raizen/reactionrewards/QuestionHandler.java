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
    
    private static int rand(int Min, int Max){ // random number between this
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }
    private static int randSign(int Min, int Max){ // number between this, and random sign
        int[] i = {1, -1};
        return rand(Min, Max) * i[rand(0,1)];
    }
    
    private static int pI(String var){ // alias for parseInt
        return Integer.parseInt(var);
    }

    public QuestionHandler(ReactionRewards plugin) {
        this.plugin = plugin;
        random      = new Random();
    }

    public void generateQuestion() {
        int alphaLength   = 10,
            mathMinNumber = 10,
            mathMaxNumber = 1000,
            eqMinNumber = 10,
            eqMaxNumber = 200;

        if (plugin.getCfg("main").getStringList("questionTypes").size() > 0) {
            types = plugin.getCfg("main").getStringList("questionTypes").toArray(new String[0]);
        }

        if (plugin.getCfg("main").getInt("alphaLength") > 0) {
            alphaLength = plugin.getCfg("main").getInt("alphaLength");
        }

        if (plugin.getCfg("main").getInt("mathMinNumber") > 0) {
            mathMinNumber = plugin.getCfg("main").getInt("mathMinNumber");
        }
        
        if (plugin.getCfg("main").getInt("mathMaxNumber") > 0) {
            mathMaxNumber = plugin.getCfg("main").getInt("mathMaxNumber");
        }
        
        if (plugin.getCfg("main").getInt("eqqhMinNumber") > 0) {
            eqMinNumber = plugin.getCfg("main").getInt("eqMinNumber");
        }
        
        if (plugin.getCfg("main").getInt("eqMaxNumber") > 0) {
            eqMaxNumber = plugin.getCfg("main").getInt("eqMaxNumber");
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
                generateMathQuestion(mathMinNumber, mathMaxNumber);
                break;
                
            case "equation" :
                generateEquation(eqMinNumber, eqMaxNumber);
                break;
        }

        generateReward();
        expired = false;

        // reset and start the timer
        timer.reset();
        timer.start();
    }

    private void generateRandomString(int length) {
        String alphaNum     = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZ",
               randomString = "";

        for (int i = 0; i < length; i++)
            randomString += alphaNum.charAt(random.nextInt(alphaNum.length()));

        question   = randomString;
        answers    = new String[1];
        answers[0] = question;
    }

    private void generateMathQuestion(int min, int max) {
        int[] numbers = {0, 0};
        for (int i=0; i<2; i++)
            numbers[i] = randSign(min, max); // save number on array
        
        int op = rand(0,1); // operator, 0 = "-", 1 = "+"
        
        question = String.format("%s %s %s", numbers[0], (op==0)?"-":"+", numbers[1]); // 2nd parameter: operator
        answers = new String[1];
        answers[0] = Integer.toString(numbers[0] + numbers[1] * ((op==0)?-1:1) ); // inverse additive if minus
    }

    private void generateTriviaQuestion() {
        String[] questions = plugin.getCfg("trivia").getKeys(false).toArray(new String[0]);

        question = questions[random.nextInt(questions.length)];
        answers  = plugin.getCfg("trivia").getStringList(question).toArray(new String[0]);
    }
    
    private void generateEquation(int min, int max){
        String[] pos = new String[5],
                 operators = {(rand(0,1)==0)?"-":"+", "="};
        ArrayList<String> opList = new ArrayList(Arrays.asList(operators));
        Collections.shuffle(opList, new Random(System.nanoTime()));
        
        String[] vars = {randSign(min, max)+"", randSign(min, max)+"", "x"};
        ArrayList<String> varsList = new ArrayList(Arrays.asList(vars));
        Collections.shuffle(varsList, new Random(System.nanoTime()));
        
        for (int i = 0; i<5; i+=2){
            if(i>0){
                // i-1, will take 1 y 3
                // 1=0, 3=1
                int x = (i-1 == 1)? 0 : 1;
                pos[i-1] = opList.get(x);
            }
            pos[i] = varsList.get(i/2);
        }
        
        question = "";
        for(int i = 0; i<5; i++)
            question = question + pos[i] + " ";
        
        // 0, 2, 4 = var,const; 1, 3 = operator or "="
        int answer,
            d,e,f; // la comprobación de índices dependerá de la posición de 'x'
        
        // para formatos: a +/- b = x, x = a +/- b
        if ((pos[1].equals("=") && pos[0].equals("x")) || (pos[3].equals("=") && pos[4].equals("x"))){
            if (pos[0].equals("x")) { // x is at beggining?
                d=3; e=2; f=4; 
            } else { // or at the end?
                d=1; e=0; f=2; 
            }
            answer = (pos[d].equals("+")) ? pI(pos[e]) + pI(pos[f]) : pI(pos[e]) - pI(pos[f]);
        } else {
            if (pos[1].equals("=")) { // the "=" sign is to the left...
                d=4;e=0;f=2;
            } else { // or to the right?
                d=0;e=4;f=0;
            }
            int constant = (pos[2].equals("x")) ? pI(pos[d]) : pI(pos[2]);
            
            // y si al lado izquierdo de la constante, teniendo x al principio, y el "=" a la derecha? entonces es su inverso positivo
            if (pos[0+f].equals("x") && pos[1+f].equals("-"))
                constant = constant * -1;
            
            answer = pI(pos[e]) + constant * -1;
            
            // y si al lado izquierdo de la x hay un "-"? significa que la respuesta es su inverso aditivo
            if (pos[1+f].equals("-") && pos[2+f].equals("x"))
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