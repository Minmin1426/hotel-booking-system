package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.batch.dto.ExcelParseResultDto;
import com.hotelbooking.booking.batch.dto.ExcelRowErrorDto;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelParsingService {

    private final HotelRepository hotelRepository;

    private static final String[] REQUIRED_HEADERS = {
            "guest_name", "email", "phone_number", "hotel_id",
            "check_in_date", "check_out_date", "room_type", "quantity"
    };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_ALT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ExcelParseResultDto parseAndValidate(Long blockBookingId, MultipartFile file) {
        List<ExcelRowErrorDto> errors = new ArrayList<>();
        int validRows = 0;
        int invalidRows = 0;
        int totalRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add(ExcelRowErrorDto.builder()
                        .rowNumber(1).field("header").message("Excel file is empty or missing header row").build());
                return ExcelParseResultDto.builder()
                        .totalRows(0).validRows(0).invalidRows(1).errors(errors)
                        .blockBookingId(blockBookingId).build();
            }

            boolean hasAllHeaders = validateHeaders(headerRow);
            if (!hasAllHeaders) {
                errors.add(ExcelRowErrorDto.builder()
                        .rowNumber(1).field("header").message("Missing required headers. Expected: guest_name, email, phone_number, hotel_id, check_in_date, check_out_date, room_type, quantity").build());
                return ExcelParseResultDto.builder()
                        .totalRows(0).validRows(0).invalidRows(1).errors(errors)
                        .blockBookingId(blockBookingId).build();
            }

            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row)) continue;

                totalRows++;
                List<String> rowErrors = validateRow(row, i);

                if (rowErrors.isEmpty()) {
                    validRows++;
                } else {
                    invalidRows++;
                    for (String errorMsg : rowErrors) {
                        errors.add(ExcelRowErrorDto.builder()
                                .rowNumber(i + 1).field("row").message(errorMsg).build());
                    }
                }
            }

        } catch (IOException e) {
            log.error("Failed to parse Excel file", e);
            errors.add(ExcelRowErrorDto.builder()
                    .rowNumber(0).field("file").message("Failed to read Excel file: " + e.getMessage()).build());
        } catch (Exception e) {
            log.error("Unexpected error parsing Excel file", e);
            errors.add(ExcelRowErrorDto.builder()
                    .rowNumber(0).field("file").message("Error parsing Excel file: " + e.getMessage()).build());
        }

        return ExcelParseResultDto.builder()
                .totalRows(totalRows).validRows(validRows).invalidRows(invalidRows)
                .errors(errors).blockBookingId(blockBookingId).build();
    }

    private boolean validateHeaders(Row headerRow) {
        for (String required : REQUIRED_HEADERS) {
            boolean found = false;
            for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && getCellValueAsString(cell).trim().equalsIgnoreCase(required)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellValueAsString(cell).trim();
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    private List<String> validateRow(Row row, int rowNum) {
        List<String> errors = new ArrayList<>();

        // guest_name
        String guestName = getCellValueAsString(row.getCell(0)).trim();
        if (guestName.isEmpty()) {
            errors.add("guest_name is required");
        } else if (guestName.length() > 100) {
            errors.add("guest_name exceeds 100 characters");
        }

        // email
        String email = getCellValueAsString(row.getCell(1)).trim();
        if (email.isEmpty()) {
            errors.add("email is required");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add("email format is invalid");
        }

        // phone_number
        String phone = getCellValueAsString(row.getCell(2)).trim();
        if (!phone.isEmpty() && phone.length() > 30) {
            errors.add("phone_number exceeds 30 characters");
        }

        // hotel_id
        Long hotelId = getCellValueAsLong(row.getCell(3));
        if (hotelId == null) {
            errors.add("hotel_id is required");
        } else if (!hotelRepository.existsById(hotelId)) {
            errors.add("hotel with id " + hotelId + " does not exist");
        }

        // check_in_date
        LocalDate checkIn = parseDate(row.getCell(4));
        if (checkIn == null) {
            errors.add("check_in_date is required and must be in yyyy-MM-dd or dd/MM/yyyy format");
        } else if (checkIn.isBefore(LocalDate.now())) {
            errors.add("check_in_date cannot be in the past");
        }

        // check_out_date
        LocalDate checkOut = parseDate(row.getCell(5));
        if (checkOut == null) {
            errors.add("check_out_date is required and must be in yyyy-MM-dd or dd/MM/yyyy format");
        } else if (checkIn != null && !checkOut.isAfter(checkIn)) {
            errors.add("check_out_date must be after check_in_date");
        }

        // room_type
        String roomType = getCellValueAsString(row.getCell(6)).trim();
        if (roomType.isEmpty()) {
            errors.add("room_type is required");
        }

        // quantity
        Integer quantity = getCellValueAsInt(row.getCell(7));
        if (quantity == null || quantity < 1) {
            errors.add("quantity must be at least 1");
        } else if (quantity > 10) {
            errors.add("quantity cannot exceed 10 per row");
        }

        return errors;
    }

    public BlockBookingRow parseRow(Row row, BlockBookingRequest request) {
        String guestName = getCellValueAsString(row.getCell(0)).trim();
        String email = getCellValueAsString(row.getCell(1)).trim();
        String phone = getCellValueAsString(row.getCell(2)).trim();
        Long hotelId = getCellValueAsLong(row.getCell(3));
        Hotel hotel = hotelId != null ? hotelRepository.findById(hotelId).orElse(null) : null;
        LocalDate checkIn = parseDate(row.getCell(4));
        LocalDate checkOut = parseDate(row.getCell(5));
        String roomType = getCellValueAsString(row.getCell(6)).trim();
        Integer quantity = getCellValueAsInt(row.getCell(7));

        return BlockBookingRow.builder()
                .blockBookingRequest(request)
                .guestName(guestName)
                .email(email)
                .phoneNumber(phone)
                .hotel(hotel)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomType(roomType)
                .quantity(quantity != null ? quantity : 1)
                .rowStatus(RowStatus.VALID)
                .build();
    }

    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            String val = getCellValueAsString(cell).trim();
            if (val.isEmpty()) return null;
            try {
                return LocalDate.parse(val, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                return LocalDate.parse(val, DATE_FORMATTER_ALT);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private Long getCellValueAsLong(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (long) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : Long.parseLong(val);
                }
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getCellValueAsInt(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : Integer.parseInt(val);
                }
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
