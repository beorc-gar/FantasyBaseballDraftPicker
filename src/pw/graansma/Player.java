package pw.graansma;

import org.w3c.dom.Element;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class Player {
    public final int id;
    public final String name;
    public final int rank;
    public final String position;
    public final double draftPosition;

    public static final String STARTER    = "SP";
    public static final String RELIEVER   = "RP";
    public static final String INFIELDER  = "IF";
    public static final String OUTFIELDER = "OF";
    public static final String PITCHER    = "P";
    public static final String CATCHER    = "C";
    public static final String HITTER     = "DH";
    //todo sp and rp

    //todo figure out if api has actual dh in it
    private static final Map<String, List<String>> positions = new HashMap<>();
    static {
        map.put(INFIELD, Arrays.asList("1B", "2B", "3B", "SS"));
        map.put(OUTFIELD, Arrays.asList("LF", "CF", "RF"));
        map.put(PITCHER, Arrays.asList("SP", "RP"));
        map.put(STARTER, Collections.singletonList("SP"));
        map.put(RELIEVER, Collections.singletonList("RP"));
        map.put(CATCHER, Collections.singletonList("C"));
        map.put(HITTER, Collections.singletonList("DH")); //todo can be all
    }

    private String getString(Element element, String tag) {
        return element.getElementsByTagName(tag).item(0).getTextContent();
    }

    public Player(Element element) {
        id = Integer.valueOf(getString(element, "playerId"));
        name = getString(element, "playerName");
        rank = Integer.valueOf(getString(element, "rank"));
        position = getString(element, "position");
        draftPosition = Double.valueOf(getString(element, "adp"));
    }

    public double getRating() {
        return rank + draftPosition;
    }

    public boolean is(String pos) {
        return positions.hasKey(pos) ? positions.get(pos).contains(position) : false;
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
