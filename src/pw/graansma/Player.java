package pw.graansma;

import org.w3c.dom.Element;

public class Player {
    public final int id;
    public final String name;
    public final int rank;
    public final String position;
    public final double draftPosition;
    public final String league;

    public static final String AMERICAN_LEAGUE = "American";
    public static final String NATIONAL_LEAGUE = "National";

    private String getString(Element element, String tag) {
        return element.getElementsByTagName(tag).item(0).getTextContent();
    }

    public Player(Element element) {
        id = Integer.valueOf(getString(element, "playerId"));
        name = getString(element, "playerName");
        rank = Integer.valueOf(getString(element, "rank"));
        position = getString(element, "position");
        draftPosition = Double.valueOf(getString(element, "adp"));
        league = getString(element, "league");
    }

    @Override
    public boolean equals(Object o) {
        String name = null;
        if(o instanceof String) {
            name = (String)o;
        } else if(o instanceof Player) {
            name = ((Player) o).name;
        }
        return this.name != null && this.name.equalsIgnoreCase(name);
    }
}
