package com.lazaretov;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.xlstest.XLS.containsText;
import static org.assertj.core.api.Assertions.assertThat;

public class SelenideFileTests {

    ClassLoader cl = SelenideFileTests.class.getClassLoader();  //Необходимо чтобы использовать ресурсы из папки resources

    @Disabled
    @Test
    void downloadTest01() throws Exception {
        open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();

        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            assertThat(strContent).contains("This repository");
        }
    }

    @DisplayName("Содержание zip архива и содержание файлов в нем")
    @Test
    void zipTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/Desktop.zip"));
        try(ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("Desktop.zip"))) {
            ZipEntry entry; //Следующий файл в архиве
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("price.xlsx")) {
                    try (InputStream is = zf.getInputStream(entry)) {
                        XLS xls = new XLS(is);
                        assertThat(xls.excel
                                .getSheetAt(0)
                                .getRow(0)
                                .getCell(2)
                                .getStringCellValue()
                        ).isEqualTo("Наименование");
                    }
                }
                if (entry.getName().contains("example.csv")) {
                    try (InputStream is = zf.getInputStream(entry)) {
                        CSVReader reader = new CSVReader(new InputStreamReader(is));
                        List<String[]> content = reader.readAll();
                        String[] row = content.get(2);
                        assertThat(row[0]).isEqualTo("150000");
                        assertThat(row[2]).isEqualTo("Harold Campbell");
                    }
                }
                if (entry.getName().contains("LTO.pdf")) {
                    try (InputStream is = zf.getInputStream(entry)) {
                        PDF pdf = new PDF(is);
                        assertThat(pdf.text).contains("QA engineer");
                    }
                }
            }
        }
    }

}






