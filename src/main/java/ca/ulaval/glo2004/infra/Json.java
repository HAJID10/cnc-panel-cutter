package ca.ulaval.glo2004.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON reader and writer, so project files need no extra library.
 * <p>
 * Values are mapped to plain Java types: object = {@code Map<String, Object>} (keys keep their order),
 * array = {@code List<Object>}, string = {@code String}, number = {@code Double}, true/false = {@code Boolean},
 * null = {@code null}.
 */
public final class Json {

    private Json() {
    }

    // ------------------------------------------------------------------ writing

    /** Writes a value as indented JSON. */
    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, 0);
        return out.append('\n').toString();
    }

    private static void write(Object value, StringBuilder out, int indent) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String s) {
            writeString(s, out);
        } else if (value instanceof Boolean b) {
            out.append(b);
        } else if (value instanceof Number n) {
            writeNumber(n.doubleValue(), out);
        } else if (value instanceof Map<?, ?> map) {
            writeObject(map, out, indent);
        } else if (value instanceof List<?> list) {
            writeArray(list, out, indent);
        } else {
            throw new IllegalArgumentException("Can't write " + value.getClass().getSimpleName() + " as JSON.");
        }
    }

    private static void writeNumber(double d, StringBuilder out) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("JSON has no NaN or infinity.");
        }
        // 18.0 is written as 18; other values keep their shortest exact form (1219.2, 3.175).
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            out.append((long) d);
        } else {
            out.append(d);
        }
    }

    private static void writeObject(Map<?, ?> map, StringBuilder out, int indent) {
        if (map.isEmpty()) {
            out.append("{}");
            return;
        }
        out.append("{\n");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            pad(out, indent + 1);
            writeString(String.valueOf(entry.getKey()), out);
            out.append(": ");
            write(entry.getValue(), out, indent + 1);
            out.append(++i < map.size() ? ",\n" : "\n");
        }
        pad(out, indent);
        out.append('}');
    }

    private static void writeArray(List<?> list, StringBuilder out, int indent) {
        if (list.isEmpty()) {
            out.append("[]");
            return;
        }
        // Short arrays of numbers (points) stay on one line: [12.5, 30]
        if (list.stream().allMatch(v -> v instanceof Number)) {
            out.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                writeNumber(((Number) list.get(i)).doubleValue(), out);
            }
            out.append(']');
            return;
        }
        out.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            pad(out, indent + 1);
            write(list.get(i), out, indent + 1);
            out.append(i + 1 < list.size() ? ",\n" : "\n");
        }
        pad(out, indent);
        out.append(']');
    }

    private static void writeString(String s, StringBuilder out) {
        out.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c)); // other control characters
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    private static void pad(StringBuilder out, int indent) {
        out.append("  ".repeat(indent));
    }

    // ------------------------------------------------------------------ reading

    /** Reads a JSON text. Throws {@link IllegalArgumentException} with the position of the first error. */
    public static Object read(String text) {
        Reader reader = new Reader(text);
        Object value = reader.value();
        reader.skipSpaces();
        if (!reader.atEnd()) {
            throw reader.error("unexpected text after the end");
        }
        return value;
    }

    /** Recursive-descent parser over the text. */
    private static final class Reader {
        private final String text;
        private int pos;

        Reader(String text) {
            this.text = text;
        }

        boolean atEnd() {
            return pos >= text.length();
        }

        IllegalArgumentException error(String message) {
            return new IllegalArgumentException("Invalid JSON at character " + pos + ": " + message + ".");
        }

        void skipSpaces() {
            while (!atEnd() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        char peek() {
            skipSpaces();
            if (atEnd()) {
                throw error("unexpected end of text");
            }
            return text.charAt(pos);
        }

        void expect(char c) {
            if (peek() != c) {
                throw error("expected '" + c + "'");
            }
            pos++;
        }

        Object value() {
            char c = peek();
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        yield number();
                    }
                    throw error("unexpected character '" + c + "'");
                }
            };
        }

        Object literal(String word, Object value) {
            if (!text.startsWith(word, pos)) {
                throw error("expected " + word);
            }
            pos += word.length();
            return value;
        }

        Map<String, Object> object() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                if (peek() != '"') {
                    throw error("expected a name in quotes");
                }
                String key = string();
                expect(':');
                map.put(key, value());
                char c = peek();
                pos++;
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    pos--;
                    throw error("expected ',' or '}'");
                }
            }
        }

        List<Object> array() {
            expect('[');
            List<Object> list = new ArrayList<>();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(value());
                char c = peek();
                pos++;
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    pos--;
                    throw error("expected ',' or ']'");
                }
            }
        }

        String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (atEnd()) {
                    throw error("unfinished string");
                }
                char c = text.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (atEnd()) {
                    throw error("unfinished string");
                }
                char e = text.charAt(pos++);
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 > text.length()) {
                            throw error("bad \\u escape");
                        }
                        try {
                            sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw error("bad \\u escape");
                        }
                        pos += 4;
                    }
                    default -> throw error("unknown escape \\" + e);
                }
            }
        }

        Double number() {
            int start = pos;
            while (!atEnd() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
                pos++;
            }
            try {
                return Double.valueOf(text.substring(start, pos));
            } catch (NumberFormatException ex) {
                pos = start;
                throw error("bad number");
            }
        }
    }
}
