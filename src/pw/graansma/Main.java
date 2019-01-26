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
import java.util.List;

public class Main {
    private static final String RANKINGS_URL = "https://fantasybaseballnerd.com/service/draft-rankings";

    private final List<Player> draft;
    private final List<Player> team;
    private final File teamFile;

    private Main(File f) {
        teamFile = f;
        draft = new ArrayList<>();
        team = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            URLConnection conn = new URL(RANKINGS_URL).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            Document doc = dBuilder.parse(conn.getInputStream());
            doc.getDocumentElement().normalize();
            NodeList players = doc.getElementsByTagName("Player");
            for (int i = 0; i < players.getLength(); i++) {
                Node node = players.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    draft.add(new Player((Element) node));
                }
            }
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
                String inputLine;
                while((inputLine = br.readLine()) != null) {
                    team.add(getPlayer(inputLine.replace("\n", "").replace("\r", "")));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {

        try(PrintWriter out = new PrintWriter(new FileOutputStream(teamFile, false))) {
            for(Player p : team) {
                out.println(p.name);
            }
            out.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Player getPlayer(String name) {
        for(Player p : draft) {
            if(p.name.equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    private Player cut() {
        int index = 0;
        double benchMark = 0;
        for(int i = 0; i < team.size(); i++) {
            Player p = team.get(i);
            double score = p.rank + p.draftPosition;
            if(score > benchMark) {
                benchMark = score;
                index = i;
            }
        }
        return team.remove(index);
    }

    // cut 15 teamFile
    // pick
    public static void main(String... args) {
	    if(args.length < 2 || args.length > 3) {
	        System.err.println("Usage: java exe cut x teamFile" +
                                "\n  or: java exe pick teamFile");
        } else {
	        Main m = new Main(new File(args[args.length -1]));

	        switch(args[0]) {
                case "cut":
                    int count = Integer.valueOf(args[1]);
                    while(m.team.size() > count) {
                        System.out.println(m.cut().name);
                    }
                    break;
                case "pick":
                    //todo have a roster file I can't pick taken players
                    // only pick players in american league
                    // pick the best ranked player that I have an open position for
                    // positions:
                    /*
                        SP x 2
                        RP x 2
                        P  x 2 (RP or SP)
                        IF x 4 (1B or 2B or SS or 3B)
                        OF x 4 (LF or CF or RF)
                        C  x 1
                        DH x 1 (any)
                     */
                    break;
            }
            m.save();
        }
    }
}
