package ca.ulaval.glo2004.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void writesAndReadsBackTheSameValues() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("name", "Saw \"1/8\"\\ \n tab\t é");
        value.put("width", 3.175);
        value.put("whole", 18.0);
        value.put("on", true);
        value.put("nothing", null);
        value.put("point", List.of(12.5, -30.0));
        value.put("list", List.of(Map.of("a", 1.0), List.of()));
        value.put("empty", Map.of());

        Object back = Json.read(Json.write(value));
        assertEquals(value, back);
    }

    @Test
    void wholeNumbersAreWrittenWithoutDecimals() {
        assertEquals("18\n", Json.write(18.0));
        assertEquals("1219.2\n", Json.write(1219.2));
        assertEquals("[300, 0]\n", Json.write(List.of(300.0, 0.0)));
    }

    @Test
    void readsUsualJson() {
        Object v = Json.read(" { \"a\" : [1, 2.5e1, -3], \"b\": \"x\\u0041\\/\", \"c\": null } ");
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) v;
        assertEquals(List.of(1.0, 25.0, -3.0), map.get("a"));
        assertEquals("xA/", map.get("b"));
        assertNull(map.get("c"));
    }

    @Test
    void errorsGiveThePosition() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Json.read("{\"a\": 1,, }"));
        assertTrue(e.getMessage().contains("character 8"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> Json.read("[1, 2"));
        assertThrows(IllegalArgumentException.class, () -> Json.read("\"open"));
        assertThrows(IllegalArgumentException.class, () -> Json.read("{} extra"));
        assertThrows(IllegalArgumentException.class, () -> Json.read("tru"));
        assertThrows(IllegalArgumentException.class, () -> Json.read("1.2.3"));
    }
}
