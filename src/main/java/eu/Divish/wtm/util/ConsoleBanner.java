package eu.Divish.wtm.util;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Large five-row, four-color DVSH startup banner.
 *
 * The default ASCII layout uses only single-column ASCII glyphs. A browser-based
 * console cannot report its active font to the server, so a Unicode block layout
 * cannot be adapted reliably at runtime. BLOCK remains available for terminals
 * that render U+2588 as exactly one character cell.
 */
public final class ConsoleBanner {
    private static final int MIN_CONTENT_WIDTH = 88;
    private static final int INFO_GAP = 5;
    private static final String[] COLORS = {"&a", "&9", "&e", "&c"};

    // D, V, S, H: a uniform 5-row raster for either ASCII '#' or Unicode '█'.
    private static final String[][] GLYPHS = {
            {"#### ", "#   #", "#   #", "#   #", "#### "},
            {"#   #", "#   #", " # # ", " # # ", "  #  "},
            {"#### ", "#    ", "###  ", "   # ", "#### "},
            {"#  #", "#  #", "####", "#  #", "#  #"}
    };

    private ConsoleBanner() {
    }

    /**
     * @param style "ascii" (recommended for web panels) or "block" (original glyphs)
     * @param information four or fewer color-coded informational lines for right column
     */
    public static void print(ConsoleCommandSender console, String style, String[] information) {
        boolean useUnicodeBlocks = "block".equalsIgnoreCase(style == null ? "" : style.trim());
        char filled = useUnicodeBlocks ? '█' : '#';

        String[] logo = new String[5];
        int logoWidth = 0;
        for (int row = 0; row < logo.length; row++) {
            StringBuilder art = new StringBuilder();
            for (int letter = 0; letter < GLYPHS.length; letter++) {
                if (letter > 0) art.append("&8  ");
                art.append(COLORS[letter]).append(GLYPHS[letter][row].replace('#', filled));
            }
            art.append("&8");
            logo[row] = art.toString();
            logoWidth = Math.max(logoWidth, visibleWidth(logo[row]));
        }

        int infoWidth = 0;
        if (information != null) {
            for (String info : information) infoWidth = Math.max(infoWidth, visibleWidth(info));
        }
        int contentWidth = Math.max(MIN_CONTENT_WIDTH, logoWidth + INFO_GAP + infoWidth);
        int rightWidth = contentWidth - logoWidth - INFO_GAP;
        String border = colors("&8+" + "-".repeat(contentWidth) + "+");
        console.sendMessage(border);

        for (int i = 0; i < logo.length; i++) {
            String left = logo[i];
            String right = information != null && i < information.length && information[i] != null
                    ? information[i] : "";
            String line = "&8|" + left
                    + " ".repeat(logoWidth - visibleWidth(left) + INFO_GAP)
                    + right + "&8" + " ".repeat(rightWidth - visibleWidth(right)) + "|";
            console.sendMessage(colors(line));
        }
        console.sendMessage(border);
    }

    private static int visibleWidth(String text) {
        if (text == null) return 0;
        // ASCII glyphs and Latin metadata have one cell per visible character in a monospace font.
        return text.replaceAll("(?i)[&§][0-9A-FK-OR]", "").length();
    }

    private static String colors(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
