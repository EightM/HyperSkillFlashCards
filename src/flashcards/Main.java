package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static TreeMap<String, String> cards = new TreeMap<>();
    static Scanner sc = new Scanner(System.in);
    static ArrayList<String> logs = new ArrayList<>();
    static final String FILE_NAME = "File name:";
    static final String FILE_NOT_FOUND = "File not found.";
    static final String CARD = "The card:";
    static HashMap<String, Integer> mistakeStats = new HashMap<>();
    static String saveFileName = "";

    public static void main(String[] args) {

        if (args.length > 0 && args.length % 2 == 0) {
            for (int i = 0; i < args.length - 1; i+=2) {
                String param = args[i];

                switch (param) {
                    case "-import":
                        importCards(args[i+1]);
                        break;
                    case "-export":
                        saveFileName = args[i+1];
                        break;
                    default:
                        System.out.println("Wrong parameters");
                        break;
                }
            }
        }

        String actionMessage = "Input the action (add, remove, import, export, ask, exit, log, " +
                "hardest card, reset stats):";

        boolean exit = false;
        while (!exit) {

            System.out.println(actionMessage);
            logs.add(actionMessage);

            String action = sc.nextLine();
            logs.add(action);

            switch (action) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    removeCard();
                    break;
                case "import":
                    importCards();
                    break;
                case "export":
                    exportCards();
                    break;
                case "ask":
                    askQuestions();
                    break;
                case "log":
                    saveLogs();
                    break;
                case "hardest card":
                    askHardestCard();
                    break;
                case "reset stats":
                    resetStatistics();
                    break;
                case "exit":
                    System.out.println("Bye bye!");
                    logs.add("Bye bye!");
                    if(!"".equals(saveFileName)) {
                        exportCards(saveFileName);
                    }
                    exit = true;
                    break;
                default:
                    System.out.println("Wrong command.");
                    logs.add("Wrong command.");
            }

        }
    }

    private static void resetStatistics() {
        mistakeStats.clear();
        System.out.println("Card statistics has been reset.");
        logs.add("Card statistics has been reset.");
    }

    private static void askHardestCard() {
        if (mistakeStats.isEmpty()) {
            System.out.println("There are no cards with errors.");
            logs.add("There are no cards with errors.");
            return;
        }

        int max = mistakeStats.values().stream().max(Integer::compareTo).orElse(0);
        var maxMistakes = mistakeStats.entrySet().stream()
                .filter(entry -> entry.getValue() == max)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (maxMistakes.size() == 1) {
            var entry = maxMistakes.entrySet().stream().findFirst().get();
            System.out.printf("The hardest card is %s. You have %d errors answering it.%n", entry.getKey(), entry.getValue());
            logs.add(String.format("The hardest card is %s. You have %d errors answering it.%n", entry.getKey(), entry.getValue()));
        } else {
            String msg = "The hardest cards are ";
            msg += String.join(", ", maxMistakes.keySet());
            msg += String.format(". You gave %d errors answering them.", max);
            System.out.println(msg);
            logs.add(msg);
        }

    }

    private static void saveLogs() {
        System.out.println(FILE_NAME);
        logs.add(FILE_NAME);

        String filename = sc.nextLine();
        logs.add(filename);

        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(file)) {
            logs.forEach(pw :: println);
            System.out.println("The log has been saved.");
            logs.add("The log has been saved.");
        } catch (FileNotFoundException e) {
            System.out.println(FILE_NOT_FOUND);
            logs.add(FILE_NOT_FOUND);
        }
    }

    private static void askQuestions() {
        System.out.println("How many to ask?");
        logs.add("How many to ask?");

        int count = Integer.parseInt(sc.nextLine());
        var keys = new ArrayList<>(cards.keySet());
        Collections.shuffle(keys);

        while (count > 0) {
            Random rand = new Random(keys.size());
            String term = keys.get(rand.nextInt(keys.size()));
            System.out.printf("Print the definition of \"%s\":%n", term);
            logs.add(String.format("Print the definition of \"%s\":%n", term));

            String userDefinition = sc.nextLine();
            logs.add(userDefinition);

            if (cards.get(term).equals(userDefinition)) {
                System.out.println("Correct answer");
                logs.add("Correct answer");
            } else {
                boolean existsDefinition = cards.containsValue(userDefinition);
                mistakeStats.put("\"" + term + "\"", mistakeStats.getOrDefault("\"" + term + "\"", 0) + 1);

                if (existsDefinition) {
                    System.out.printf("Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".%n"
                            , cards.get(term), getKeyByValue(userDefinition));
                    logs.add(String.format("Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".%n"
                            , cards.get(term), getKeyByValue(userDefinition)));
                } else {
                    System.out.printf("Wrong answer. The correct one is \"%s\".%n", cards.get(term));
                    logs.add(String.format("Wrong answer. The correct one is \"%s\".%n", cards.get(term)));
                }

            }
            count--;
        }
    }

    private static void exportCards() {
        System.out.println(FILE_NAME);
        logs.add(FILE_NAME);
        String fileName = sc.nextLine();
        logs.add(fileName);

        File file = new File(fileName);
        exportData(file);
    }

    private static void exportCards(String fileName) {
        File file = new File(fileName);
        exportData(file);
    }

    private static void exportData(File file) {
        int count = 0;

        try (PrintWriter pw = new PrintWriter(file)){

            for (Map.Entry<String, String> entry : cards.entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                pw.println(key);
                pw.println(value);
                pw.println(mistakeStats.get("\"" + key + "\""));
                ++count;
            }

        } catch (FileNotFoundException e) {
            System.out.println(FILE_NOT_FOUND);
            logs.add(FILE_NOT_FOUND);
            return;
        }

        System.out.printf("%d cards have been saved.%n", count);
        logs.add(String.format("%d cards have been saved.%n", count));
    }

    private static void importCards() {

        System.out.println(FILE_NAME);
        logs.add(FILE_NAME);
        String fileName = sc.nextLine();
        logs.add(fileName);
        File file = new File(fileName);
        importData(file);
    }

    private static void importCards(String fileName) {
        File file = new File(fileName);
        importData(file);
    }

    private static void importData(File file) {
        int count = 0;

        try (Scanner scanner = new Scanner(file)){

            while (scanner.hasNextLine()) {
                String term = scanner.nextLine();
                String definition = scanner.nextLine();
                int mistakes = 0;

                try {
                    mistakes = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }


                cards.put(term, definition);
                mistakeStats.put("\"" + term + "\"", mistakes);
                count++;
            }
        } catch (FileNotFoundException e) {
            System.out.println(FILE_NOT_FOUND);
            logs.add(FILE_NOT_FOUND);
            return;
        }

        System.out.printf("%d cards have been loaded.%n", count);
        logs.add(String.format("%d cards have been loaded.%n", count));
    }

    private static void removeCard() {
        System.out.println(CARD);
        logs.add(CARD);
        String card = sc.nextLine();
        logs.add(card);
        if (!cards.containsKey(card)) {
            System.out.printf("Can't remove \"%s\": there is no such card.%n", card);
            logs.add(String.format("Can't remove \"%s\": there is no such card.%n", card));
            return;
        }

        cards.remove(card);
        mistakeStats.remove("\"" + card + "\"");
        System.out.println("The card has been removed.");
        logs.add("The card has been removed.");

    }

    private static void addCard() {
        System.out.println(CARD);
        logs.add(CARD);
        String term = sc.nextLine();
        logs.add(term);
        if (cards.containsKey(term)) {
            System.out.printf("The card \"%s\" already exists.%n", term);
            logs.add(String.format("The card \"%s\" already exists.%n", term));
            return;
        }

        System.out.println("The definition of the card:");
        logs.add("The definition of the card:");
        String definition = sc.nextLine();
        if (cards.containsValue(definition)) {
            System.out.printf("The definition \"%s\" already exists.%n", definition);
            logs.add(String.format("The definition \"%s\" already exists.%n", definition));
            return;
        }

        cards.put(term, definition);
        System.out.printf("The pair (\"%s\":\"%s\") has been added.%n", term, definition);
        logs.add(String.format("The pair (\"%s\":\"%s\") has been added.%n", term, definition));

    }

     static <T> String getKeyByValue(T value) {
        for (var entry : cards.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }

        return "";
    }

}
