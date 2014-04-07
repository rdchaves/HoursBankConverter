package rodrigo.chaves.hoursbankconverter.exporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Cell;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Blank;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang3.time.DateUtils;

import rodrigo.chaves.hoursbankconverter.model.Day;
import rodrigo.chaves.hoursbankconverter.model.Month;


public class ExcelExporter<T> implements Exporter<T> {

	private static final String COLUMN_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	protected WritableCellFormat CELL_DATE_FORMAT = new WritableCellFormat(new WritableFont(
			WritableFont.ARIAL, 10), new DateFormat("dd/MM/yyyy"));
	protected WritableCellFormat CELL_TIME_FORMAT = new WritableCellFormat(new WritableFont(
			WritableFont.ARIAL, 10), new DateFormat("HH:mm"));
	protected WritableCellFormat CELL_TIME_PER_DAY_FORMAT = new WritableCellFormat(
			new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD), new DateFormat("HH:mm"));
	protected WritableCellFormat CELL_TOTAL_TIME_FORMAT = new WritableCellFormat(new WritableFont(
			WritableFont.ARIAL, 10, WritableFont.BOLD), new DateFormat("[h]:mm"));
	protected WritableCellFormat CELL_TITLE_FORMAT = new WritableCellFormat(new WritableFont(
			WritableFont.ARIAL, 10, WritableFont.BOLD));
	protected WritableCellFormat CELL_TOTAL_TITLE_FORMAT = new WritableCellFormat(new WritableFont(
			WritableFont.ARIAL, 10, WritableFont.BOLD));
	protected WritableCellFormat CELL_FORMAT = new WritableCellFormat(new WritableFont(
			WritableFont.ARIAL, 10));

	public ExcelExporter() throws Exception {
		CELL_DATE_FORMAT.setAlignment(Alignment.CENTRE);
		CELL_DATE_FORMAT.setBackground(Colour.GRAY_25);
		CELL_DATE_FORMAT.setBorder(Border.ALL, BorderLineStyle.THIN);

		CELL_FORMAT.setAlignment(Alignment.CENTRE);
		CELL_FORMAT.setBorder(Border.ALL, BorderLineStyle.THIN);

		CELL_TIME_FORMAT.setAlignment(Alignment.CENTRE);
		CELL_TIME_FORMAT.setBorder(Border.ALL, BorderLineStyle.THIN);

		CELL_TIME_PER_DAY_FORMAT.setAlignment(Alignment.CENTRE);
		CELL_TIME_PER_DAY_FORMAT.setBackground(Colour.GRAY_25);
		CELL_TIME_PER_DAY_FORMAT.setBorder(Border.ALL, BorderLineStyle.THIN);

		CELL_TITLE_FORMAT.setAlignment(Alignment.CENTRE);
		CELL_TITLE_FORMAT.setBackground(Colour.GRAY_25);
		CELL_TITLE_FORMAT.setBorder(Border.ALL, BorderLineStyle.THIN);

		CELL_TOTAL_TITLE_FORMAT.setAlignment(Alignment.RIGHT);

		CELL_TOTAL_TIME_FORMAT.setAlignment(Alignment.CENTRE);
		CELL_TOTAL_TIME_FORMAT.setBackground(Colour.GRAY_25);
		CELL_TOTAL_TIME_FORMAT.setBorder(Border.ALL, BorderLineStyle.THIN);
	}

	public File export(List<T> checkpoints) throws Exception {

		int sheetIndex = 0;
		final Calendar monthAux = Calendar.getInstance();
		File file = File.createTempFile("Banco_de_Horas", ".xls");
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("pt", "BR"));
		wbSettings.setExcelRegionalSettings("pt_BR");

		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);

		for (Month month : generateMonths(checkpoints)) {
			int row = 1;
			int lastColumn = 2;
			WritableSheet sheet =
					workbook.createSheet(month.toString(), sheetIndex++);
			createHeaders(sheet);
			monthAux.setTime(month.getValue());
			for (int dayNumber = 1; dayNumber <= monthAux.getActualMaximum(Calendar.DAY_OF_MONTH); dayNumber++) {
				int column = 0;
				Date dayDate =
						DateUtils.parseDate(dayNumber + "/" + (monthAux.get(Calendar.MONTH) + 1)
								+ "/" + monthAux.get(Calendar.YEAR), "d/M/yyyy");
				addDate(sheet, column++, row, dayDate);
				for (Day day : month.getDays()) {
					if (DateUtils.isSameDay(dayDate, day.getValue())) {
						for (Date checkpoint : day.getCheckpoints()) {
							if (column > lastColumn) {
								lastColumn = column;
								addTitle(sheet, column, 0);
							}
							addTime(sheet, column++, row, checkpoint);
						}
					}
				}
				row++;
			}
//			for (Day day : month.getDays()) {
//				int column = 0;
//				addDate(sheet, column++, row, day.getValue());
//				for (Date checkpoint : day.getCheckpoints()) {
//					if (column > lastColumn) {
//						lastColumn = column;
//						addTitle(sheet, column, 0);
//					}
//					addTime(sheet, column++, row, checkpoint);
//				}
//				row++;
//			}

			addTitle(sheet, lastColumn + 1, 0, "HORAS/DIA");
			for (int i = 1; i < sheet.getRows(); i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 1; j < lastColumn; j += 2) {
					if (j > 1) {
						sb.append("+");
					}
					sb.append("(").append(getColumnName(j + 1)).append(i + 1);
					sb.append("-").append(getColumnName(j)).append(i + 1);
					sb.append(")");
				}
				Cell[] cells = sheet.getRow(i);
				if (cells.length < lastColumn) {
					for (int k = cells.length; k <= lastColumn; k++) {
						addTime(sheet, k, i, null);
					}
				}
				addFormula(sheet, i, lastColumn + 1, sb.toString());
			}
			addTitle(sheet, lastColumn, row, "TOTAL", CELL_TOTAL_TITLE_FORMAT);
			String lastColumnName = getColumnName(lastColumn + 1);
			addFormula(sheet, row, lastColumn + 1, "SUM(" + lastColumnName + "1:" + lastColumnName
					+ row + ")", CELL_TOTAL_TIME_FORMAT);
		}
		workbook.write();
		workbook.close();
		return file;
	}

	protected String getColumnName(int columnIndex) {
		String name = "";
		int column = columnIndex + 1;
		while (column > 0) {
			int position = column % COLUMN_LETTERS.length();
			name =
					(position == 0 ? 'Z' : COLUMN_LETTERS.charAt(position > 0 ? position - 1 : 0))
							+ name;
			column = (column - 1) / COLUMN_LETTERS.length();
		}
		return name;
	}

	protected void createHeaders(WritableSheet sheet) throws Exception {

		// Write a few headers
		addTitle(sheet, 0, 0, "DATA");
		addTitle(sheet, 1, 0);
		addTitle(sheet, 2, 0);
	}

	protected void addTitle(WritableSheet sheet, int column, int row) throws RowsExceededException,
			WriteException {
		String label = "ENTRADA";
		if ((column % 2) == 0) {
			label = "SAÍDA";
		}
		addTitle(sheet, column, row, label);
	}

	protected void addTitle(WritableSheet sheet, int column, int row, String text)
			throws RowsExceededException, WriteException {
		addTitle(sheet, column, row, text, CELL_TITLE_FORMAT);
	}

	protected void addTitle(WritableSheet sheet, int column, int row, String text,
			WritableCellFormat format) throws RowsExceededException, WriteException {

		Label label = new Label(column, row, text, format);
		sheet.addCell(label);

		// defining column width to auto
		sheet.setColumnView(column, 12);
	}

	protected void addFormula(WritableSheet sheet, int row, int column, String formula)
			throws RowsExceededException, WriteException {
		addFormula(sheet, row, column, formula, CELL_TIME_PER_DAY_FORMAT);
	}

	protected void addFormula(WritableSheet sheet, int row, int column, String formula,
			WritableCellFormat format) throws RowsExceededException, WriteException {

		Formula f = new Formula(column, row, formula, format);
		sheet.addCell(f);
	}

	protected void addDate(WritableSheet sheet, int column, int row, Date date)
			throws WriteException, RowsExceededException {

		DateTime dateTime = new DateTime(column, row, date, CELL_DATE_FORMAT, false);
		sheet.addCell(dateTime);
	}

	protected void addTime(WritableSheet sheet, int column, int row, Date date)
			throws WriteException, RowsExceededException {

		WritableCell cell = null;
		if (date != null) {
			cell = new DateTime(column, row, date, CELL_TIME_FORMAT, true);
		} else {
			cell = new Blank(column, row, CELL_TIME_FORMAT);
		}
		sheet.addCell(cell);
	}

	protected void addNumber(WritableSheet sheet, int column, int row, Integer integer)
			throws WriteException, RowsExceededException {

		jxl.write.Number number = new jxl.write.Number(column, row, integer, CELL_FORMAT);
		sheet.addCell(number);
	}

	protected void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {

		Label label = new Label(column, row, s, CELL_FORMAT);
		sheet.addCell(label);
	}

	protected List<Month> generateMonths(final List<T> checkPoints) {

		List<Month> months = new ArrayList<Month>();
		for (Day day : generateDays(checkPoints)) {
			Date date = day.getValue();
			Month month = new Month(date);
			int index = months.indexOf(month);
			if (index > -1) {
				month = months.get(index);
			} else {
				months.add(month);
			}
			month.getDays().add(day);
		}
		return months;
	}

	protected List<Day> generateDays(final List<T> checkpoints) {
		List<Day> days = new ArrayList<Day>();
		for (T checkpoint : checkpoints) {
			Date date = convertToDate(checkpoint);
			Day day = new Day(date);
			int index = days.indexOf(day);
			if (index > -1) {
				day = days.get(index);
			} else {
				days.add(day);
			}
			day.getCheckpoints().add(date);
		}
		return days;

	}

	protected Date convertToDate(T checkpoint) {
		Date result = null;
		if (checkpoint instanceof Date) {
			result = (Date) checkpoint;
		} else if (checkpoint instanceof Long) {
			result = new Date((Long) checkpoint);
		} else {
			throw new IllegalArgumentException();
		}
		return result;
	}
}