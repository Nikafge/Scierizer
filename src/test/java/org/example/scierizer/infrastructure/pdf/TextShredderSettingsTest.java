//package org.example.scierizer.infrastructure.pdf;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class TextShredderSettingsTest {
//
//    @Test
//    void cutPaperUsesConfiguredChunkSizeAndOverlap() {
//        TextShredder textShredder = new TextShredder();
//
//        List<String> chunks = textShredder.cutPaper("abcdefghijklmnop", 2, 1);
//
//        assertEquals(List.of("abcdefgh", "efghijkl", "ijklmnop"), chunks);
//    }
//}
