//package org.example.scierizer.infrastructure.arxiv;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class ArxivClientTest {
//
//    @Test
//    void stringToListConverterUsesRequestedLimit() {
//        String apiResponse = """
//                <feed>
//                    <entry><title>First</title></entry>
//                    <entry><title>Second</title></entry>
//                    <entry><title>Third</title></entry>
//                </feed>
//                """;
//
//        List<String> papers = new ArxivClient().StringToListConverter(apiResponse, 2);
//
//        assertEquals(2, papers.size());
//        assertEquals("<entry><title>First</title></entry>", papers.get(0));
//        assertEquals("<entry><title>Second</title></entry>", papers.get(1));
//    }
//}
