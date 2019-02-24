package pw.graansma;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final String RANKINGS_URL = "https://fantasybaseballnerd.com/service/draft-rankings";

    private final List<Player> league;
    private final List<Player> team;
    private final List<Player> unavailable;
    private final File teamFile;
    private final File unavailableFile;

    private Main() {
        teamFile = file("team.txt");
        unavailableFile = file("unavailable.txt");

        league = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            URLConnection conn = new URL(RANKINGS_URL).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            Document doc = dBuilder.parse(conn.getInputStream());
            doc.getDocumentElement().normalize();
            NodeList players = doc.getElementsByTagName("Player");
            for(int i = 0; i < players.getLength(); i++) {
                Node node = players.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    league.add(new Player((Element) node));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        team = fromFile(teamFile);
        unavailable = fromFile(unavailableFile);
    }

    private List<Player> fromFile(File file) {
        List<Player> players = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String inputLine;
            while((inputLine = br.readLine()) != null) {
                players.add(getPlayer(inputLine.replace("\n", "").replace("\r", "")));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return players;
    }

    private File file(String name) {
        File file = new File(name);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void save(List<Player> players, File file) {
        try(PrintWriter out = new PrintWriter(new FileOutputStream(file, false))) {
            for(Player p : players) {
                out.println(p.name);
            }
            out.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Player getPlayer(String name) {
        for(Player p : league) {
            if(p.name.equalsIgnoreCase(name)) {
                return p;
            }
        }
        System.err.println("No such player: " + name);
        return null;
    }

    private Map<String, Integer> teamStructure() {
        Map<String, Integer> structure = new HashMap<>();
        structure.put(Player.STARTER, 2);
        structure.put(Player.RELIEVER, 2);
        structure.put(Player.PITCHER, 2);
        structure.put(Player.INFIELDER, 4);
        structure.put(Player.OUTFIELDER, 4);
        structure.put(Player.CATCHER, 1);
        structure.put(Player.HITTER, 1);
        return structure;
    }

    private Player pick() {
        List<Player> available = new ArrayList<>(league);
        available.removeAll(unavailable);
        available.removeAll(team);
        Map<String, Integer> neededPositions = teamStructure();

        for(Player p : team) {
            for(String pos : neededPositions.keySet()) {
                if(p.is(pos)) {
                    neededPositions.put(pos, neededPositions.get(pos) - 1);
                    if(neededPositions.get(pos) == 0) {
                        neededPositions.remove(pos);
                    }
                    break;
                }
            }
        }

        int index = 0;
        double benchMark = Double.MAX_VALUE;
        for(int i = 0; i < available.size(); i++) {
            Player p = available.get(i);
            for(String pos : neededPositions.keySet()) {
                if(p.is(pos)) {
                    double score = p.getRating();
                    if(score < benchMark) {
                        benchMark = score;
                        index = i;
                    }
                    break;
                }
            }
        }
        team.add(available.get(index));
        return available.get(index);
    }

    private Player cut() {
        int index = 0;
        double benchMark = 0;
        for(int i = 0; i < team.size(); i++) {
            Player p = team.get(i);
            double score = p.getRating();
            if(score > benchMark) {
                benchMark = score;
                index = i;
            }
        }
        return team.remove(index);
    }

    // cut 10
    // pick
    // rule-out [-reset] "Player One" "Player Two" ...
    // my-team "Player One" "Player Two" ...
    public static void main(String... args) {
        if(args.length < 1) {
            System.err.println(
                    "Usage: java Main cut x" +
                            "\n  or: java Main pick" +
                            "\n  or: java Main rule-out <player>..." +
                            "\n  or: java Main my-team <player>..."
            );
        } else {
            Main m = new Main();

            switch(args[0]) {
                case "cut":
                    int count = Integer.valueOf(args[1]);
                    while(m.team.size() > count) {
                        System.out.println(m.cut().name);
                    }
                    m.save(m.team, m.teamFile);
                    break;
                case "pick":
                    Player picked = m.pick();
                    System.out.println(picked.toString());
                    m.save(m.team, m.teamFile);
                    break;
                case "rule-out":
                    int index = 1;
                    if(args[1].equals("-reset")) {
                        m.unavailable.clear();
                        index = 2;
                    }
                    for(int i = index; i < args.length; i++) {
                        m.unavailable.add(m.getPlayer(args[i]));
                    }
                    m.save(m.unavailable, m.unavailableFile);
                    break;
                case "my-team":
                    m.team.clear();
                    for(int i = 1; i < args.length; i++) {
                        m.team.add(m.getPlayer(args[i]));
                    }
                    m.save(m.team, m.teamFile);
                    break;

            }
        }
    }
}
