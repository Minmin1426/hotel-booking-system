package com.hotelbooking.booking.batch;

import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExcelParsingServiceTest {

    @Mock private HotelRepository hotelRepository;

    @InjectMocks
    private ExcelParsingService excelParsingService;

    @BeforeEach
    void setUp() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(hotelRepository.existsById(999L)).thenReturn(false);
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    void parseAndValidate_ValidExcel_ReturnsValidResult() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"guest_name", "email", "phone_number", "hotel_id", "check_in_date", "check_out_date", "room_type", "quantity"},
                {"John Doe", "john@test.com", "0123", "1", "2026-08-10", "2026-08-12", "Standard", "1"},
                {"Jane Doe", "jane@test.com", "0456", "1", "2026-08-15", "2026-08-17", "Deluxe", "2"}
        });

        var result = excelParsingService.parseAndValidate(1L, file);

        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getValidRows());
        assertEquals(0, result.getInvalidRows());
        assertTrue(result.getErrors().isEmpty());
    }

    // ── Header validation ────────────────────────────────────────────────────

    @Test
    void parseAndValidate_MissingHeaders_ReturnsError() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"guest_name", "email"},
                {"John", "john@test.com"}
        });

        var result = excelParsingService.parseAndValidate(1L, file);

        assertEquals(0, result.getTotalRows());
        assertEquals(1, result.getInvalidRows());
        assertTrue(result.getErrors().get(0).getMessage().contains("Missing required headers"));
    }

    // ── Row validation ─────────────────────────────────────────────────────

    @Test
    void parseAndValidate_InvalidEmail_ReturnsError() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"guest_name", "email", "phone_number", "hotel_id", "check_in_date", "check_out_date", "room_type", "quantity"},
                {"John", "not-an-email", "0123", "1", "2026-08-01", "2026-08-03", "Standard", "1"}
        });

        var result = excelParsingService.parseAndValidate(1L, file);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getValidRows());
        assertEquals(1, result.getInvalidRows());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("email format")));
    }

    @Test
    void parseAndValidate_HotelNotFound_ReturnsError() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"guest_name", "email", "phone_number", "hotel_id", "check_in_date", "check_out_date", "room_type", "quantity"},
                {"John", "john@test.com", "0123", "999", "2026-08-01", "2026-08-03", "Standard", "1"}
        });

        var result = excelParsingService.parseAndValidate(1L, file);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getValidRows());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("hotel with id 999")));
    }

    @Test
    void parseAndValidate_BlankGuestName_ReturnsError() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"guest_name", "email", "phone_number", "hotel_id", "check_in_date", "check_out_date", "room_type", "quantity"},
                {"", "john@test.com", "0123", "1", "2026-08-01", "2026-08-03", "Standard", "1"}
        });

        var result = excelParsingService.parseAndValidate(1L, file);

        assertEquals(1, result.getTotalRows());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("guest_name is required")));
    }

    @Test
    void parseAndValidate_PastCheckInDate_ReturnsError() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"guest_name", "email", "phone_number", "hotel_id", "check_in_date", "check_out_date", "room_type", "quantity"},
                {"John", "john@test.com", "0123", "1", "2020-01-01", "2020-01-03", "Standard", "1"}
        });

        var result = excelParsingService.parseAndValidate(1L, file);

        assertEquals(1, result.getTotalRows());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("check_in_date cannot be in the past")));
    }

    @Test
    void parseAndValidate_EmptyFile_ReturnsEmptyResult() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]);

        // POI may handle empty files gracefully
        var result = excelParsingService.parseAndValidate(1L, file);
        assertEquals(0, result.getTotalRows());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MockMultipartFile createExcelFile(String[][] data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings");
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                }
            }
            workbook.write(baos);
            return new MockMultipartFile(
                    "file", "bookings.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    baos.toByteArray());
        }
    }
}
