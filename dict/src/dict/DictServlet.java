package dict;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DictServlet extends HttpServlet {

    private final Map<String, String> dict = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String path = "/WEB-INF/data/DICTLINE.GEN";

        try (InputStream in = getServletContext().getResourceAsStream(path)) {
            if (in == null) {
                throw new ServletException("Could not find dictionary file: " + path);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

                String line;
                int loaded = 0;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // The word is the first token
                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) continue;

                    String word = parts[0].toLowerCase();

                    // The meaning is usually near the end after the grammar codes
                    // Strategy:
                    // Find the FIRST "  " double-space boundary after the codes
                    // Then take the remainder and clean it.
                    //
                    // But since format is messy, we just take the LAST chunk after the last " G " or " O "
                    // If that fails, fallback to last 80 chars.

                    String meaning = extractMeaning(line);

                    if (meaning == null || meaning.isBlank()) continue;

                    // Keep first meaning only (per assignment: if duplicates, keep one)
                    if (!dict.containsKey(word)) {
                        dict.put(word, meaning);
                        loaded++;
                    }
                }

                System.out.println("[DictServlet] Loaded entries: " + loaded);
            }

        } catch (Exception e) {
            throw new ServletException("Failed to load dictionary: " + e.getMessage(), e);
        }
    }

    private String extractMeaning(String raw) {
        // In DICTLINE.GEN, meanings are usually at the end and separated by semicolons.
        // Example:
        // Aulus (Roman praenomen); (abb. A./Au.); [Absolvo, Antiquo => free, reject];

        // The meaning is basically the part AFTER the final codes.
        // Since codes contain tons of uppercase letters + spaces,
        // we can just find the first lowercase letter sequence after the huge spacing.

        // A simple reliable heuristic:
        // Find the first occurrence of "  " (two spaces) after char 60
        // then take everything after that and trim.
        int start = -1;
        for (int i = 60; i < raw.length() - 1; i++) {
            if (raw.charAt(i) == ' ' && raw.charAt(i + 1) == ' ') {
                start = i;
                break;
            }
        }

        String tail;
        if (start != -1) {
            tail = raw.substring(start).trim();
        } else {
            tail = raw;
        }

        // Remove leftover code blocks like: N 9 8 M N X X X C G
        // by finding the first lowercase letter OR '(' OR '['
        int cut = -1;
        for (int i = 0; i < tail.length(); i++) {
            char c = tail.charAt(i);
            if (Character.isLowerCase(c) || c == '(' || c == '[') {
                cut = i;
                break;
            }
        }

        if (cut != -1) {
            tail = tail.substring(cut).trim();
        }

        // Clean up multiple spaces
        tail = tail.replaceAll("\\s+", " ").trim();

        return tail;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String word = req.getParameter("word");
        if (word == null) word = "";
        word = word.trim().toLowerCase();

        String meaning = null;
        if (!word.isEmpty()) {
            meaning = dict.get(word);
        }

        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\"/>");
        out.println("  <title>Latin → English Dictionary</title>");
        out.println("  <style>");
        out.println("    body { font-family: system-ui, sans-serif; margin: 40px; }");
        out.println("    .box { max-width: 700px; }");
        out.println("    input { padding: 10px; width: 300px; }");
        out.println("    button { padding: 10px 16px; }");
        out.println("    .result { margin-top: 20px; padding: 12px; border: 1px solid #ddd; }");
        out.println("    .word { font-weight: 700; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"box\">");

        out.println("<h1>Latin → English Dictionary</h1>");

        out.println("<form method=\"GET\" action=\"dict\">");
        out.println("  <input name=\"word\" placeholder=\"Try: abaculus\" value=\"" + escape(word) + "\"/>");
        out.println("  <button type=\"submit\">Soumettre</button>");
        out.println("</form>");

        if (!word.isEmpty()) {
            out.println("<div class=\"result\">");
            out.println("<div class=\"word\">" + escape(word) + "</div>");

            if (meaning != null) {
                out.println("<div>" + escape(meaning) + "</div>");
            } else {
                out.println("<div>No definition found.</div>");
            }

            out.println("</div>");
        }

        out.println("<p>You can also use: <code>/dict?word=abaculus</code></p>");

        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    private String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
