package be.raildelays.batch.reader;

import be.raildelays.batch.bean.BatchExcelRow;
import be.raildelays.batch.writer.ExcelRowAggregator;
import be.raildelays.domain.entities.Station;
import be.raildelays.domain.entities.TrainLine;
import be.raildelays.domain.xls.ExcelRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@RunWith(BlockJUnit4ClassRunner.class)
public class BatchExcelRowMapperTest {

    public static final long DELAY = 10L;
    public static final long TRAIN1 = 466l;
    public static final long TRAIN2 = 516L;
    public static final String SHEET_NAME = "new sheet";
    public static final int SHEET_INDEX = 0;
    public static final int ROW_INDEX = 0;

    private BatchExcelRowMapper mapper;

    private Row row;

    private Workbook workbook;

    @Before
    public void setUp() {
        workbook = new HSSFWorkbook();
        row = workbook.createSheet(SHEET_NAME).createRow(ROW_INDEX);
        row.createCell(2).setCellValue(new Date());
        row.createCell(12).setCellValue("Liège-Guillemins");
        row.createCell(18).setCellValue("Bruxelles-Central");
        row.createCell(24).setCellValue("Leuven");
        row.createCell(30).setCellValue("08");
        row.createCell(32).setCellValue("01");
        row.createCell(33).setCellValue("08");
        row.createCell(35).setCellValue("58");
        row.createCell(36).setCellValue(TRAIN1);
        row.createCell(39).setCellValue(TRAIN2);
        row.createCell(42).setCellValue("08");
        row.createCell(44).setCellValue("05");
        row.createCell(45).setCellValue("09");
        row.createCell(47).setCellValue("18");
        row.createCell(48).setCellValue(TRAIN1);
        row.createCell(51).setCellValue(TRAIN2);
        row.createCell(54).setCellValue(DELAY);

        mapper = new BatchExcelRowMapper();
    }


    @Test
    public void testMapRow() throws Exception {
        NumberFormat format = new DecimalFormat("#");

        BatchExcelRow batchExcelRow = mapper.mapRow(row, 0);

        Assert.assertNotNull(batchExcelRow);
        Assert.assertEquals(DELAY, batchExcelRow.getDelay().longValue());
        Assert.assertEquals(format.format(TRAIN1), batchExcelRow.getEffectiveTrainLine1().getName());
    }

    @Test
    public void testRoundTrip() throws Exception {

        BatchExcelRow expected = new BatchExcelRow.Builder(LocalDate.parse("2000-01-01"), null) //
                .departureStation(new Station("BRUXELLES-CENTRAL")) //
                .arrivalStation(new Station("LIEGE-GUILLEMINS")) //
                .expectedDepartureTime(LocalTime.parse("14:00")) //
                .expectedArrivalTime(LocalTime.parse("15:00")) //
                .expectedTrain1(new TrainLine.Builder(529L).build()) //
                .expectedTrain2(new TrainLine.Builder(516L).build()) //
                .effectiveDepartureTime(LocalTime.parse("14:05")) //
                .effectiveArrivalTime(LocalTime.parse("15:15")) //
                .effectiveTrain1(new TrainLine.Builder(529L).build()) //
                .effectiveTrain2(new TrainLine.Builder(516L).build()) //
                .delay(10L) //
                .build();


        ExcelRow previousRow = new ExcelRowAggregator().aggregate(expected, workbook, SHEET_INDEX, ROW_INDEX);
        BatchExcelRow batchExcelRow = mapper.mapRow(workbook.getSheetAt(SHEET_INDEX).getRow(ROW_INDEX), ROW_INDEX);

        Assert.assertNotNull(previousRow);
        Assert.assertNotEquals(expected, previousRow);
        Assert.assertNotNull(batchExcelRow);
        Assert.assertNotEquals(batchExcelRow, previousRow);
        Assert.assertEquals(expected, batchExcelRow);
    }
}