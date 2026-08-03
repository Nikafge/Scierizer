//package org.example.summarizer.domain;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class LocalDateTransformerTest {
//
//    @Test
//    void testTransformation() {
//        LocalDate a = LocalDate.parse("2026-07-05");
//        String localDateAsString = LocalDateTransformer.convertToString(a);
//        LocalDate b = LocalDateTransformer.convertToLocalDate(localDateAsString);
//
//        assertEquals("2026-07-05", localDateAsString);
//        assertEquals(LocalDate.parse("2026-07-05", DateTimeFormatter.ofPattern("yyyy-MM-dd")), b);
//    }
//
//}