package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.component.spreadsheet.CellSet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Unit test for fix for issue https://github.com/vaadin/spreadsheet/issues/550.
 */
public class CellValueChangeEventOnFormulaChangeTest {

    private Spreadsheet spreadsheet;

    @Before
    public void setup() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1);
        row.createCell(1).setCellValue(0);
        row.createCell(2).setCellFormula("A1+B1");

        spreadsheet = new Spreadsheet();
        spreadsheet.setWorkbook(workbook);
    }

    /**
     * Verify that a CellValueChangeEvent is fired when a cell's formula
     * changes, but the new formula still produces the same result as the
     * previous formula.
     */
    @Test
    public void formulaChangeResultingInSameValue() {
        AtomicReference<CellSet> changedCells = new AtomicReference<>();

        spreadsheet.addCellValueChangeListener(
                event -> changedCells.set(event.getChangedCells()));

        spreadsheet.setSelection("C1");
        // B1 is 0, so the result doesn't change
        spreadsheet.getCellValueManager().onCellValueChange(3, 1, "=A1+2*B1");

        assertEquals("There should be one changed cell", 1,
                changedCells.get().getCellCount());
        assertTrue("The changed cells should include C1 with sheet name",
                changedCells.get()
                        .containsCell(new CellReference("Sheet0!C1")));
        assertTrue("The changed cells should include C1 without sheet name",
                changedCells.get().containsCell(new CellReference("C1")));
    }

}
