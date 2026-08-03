package org.example.scierizer.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateTransformer {

    //return String as in format "dd/MM/yyyy"
    public static String convertToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    //return LocalDate
    public static LocalDate convertToLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
