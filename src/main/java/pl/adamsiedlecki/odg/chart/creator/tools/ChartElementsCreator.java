package pl.adamsiedlecki.odg.chart.creator.tools;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.openapitools.model.PresentableOnChart;
import org.springframework.stereotype.Component;
import pl.adamsiedlecki.odg.util.JFreeChartUtils;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings("checkstyle:magicnumber") // there are multiple font sizes and so on
public class ChartElementsCreator {

    public XYDataset createSampleData(List<? extends PresentableOnChart> chartDataList, int maxMinutesToConnectLines) {

        TimeSeriesCollection result = new TimeSeriesCollection();
        Map<String, List<PresentableOnChart>> map =
                chartDataList.stream().collect(Collectors.groupingBy(PresentableOnChart::getGroupName));
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        for (String tName : keys) {
            TimeSeries series = new TimeSeries(tName);
            List<PresentableOnChart> list = map.get(tName);
            list.sort(Comparator.comparing(PresentableOnChart::getTime));
            LocalDateTime previous = list.get(0).getTime();

            for (PresentableOnChart presentable : list) {
                // adding null when three is no data
                if (presentable.getTime().minusMinutes(maxMinutesToConnectLines).isAfter(previous)) {
                    LocalDateTime date = presentable.getTime().minusHours(1);
                    series.addOrUpdate(JFreeChartUtils.convert(date), null);
                }
                previous = presentable.getTime();
                series.addOrUpdate(JFreeChartUtils.convert(previous), presentable.getValue());
            }

            result.addSeries(series);
        }
        return result;
    }

    public XYPlot createXYPlot(List<? extends PresentableOnChart> presentableOnChartDataList, Font font, String dataAxisLabel, String timeAxisLabel, boolean areItemLabelsVisible, int maxMinutesToConnectLines) {
        DateAxis xAxis = new DateAxis(timeAxisLabel);
        xAxis.setTickLabelFont(font);

        NumberAxis yAxis = new NumberAxis(dataAxisLabel);
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setTickLabelFont(font);

        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();

        renderer1.setDefaultStroke(new BasicStroke(4.0f));
        renderer1.setAutoPopulateSeriesStroke(false);
        renderer1.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator());
        renderer1.setDefaultItemLabelsVisible(areItemLabelsVisible);
        renderer1.setDefaultItemLabelFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));



        XYPlot plot = new XYPlot(createSampleData(presentableOnChartDataList, maxMinutesToConnectLines), xAxis, yAxis, renderer1);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(6, 6, 20, 6));

        int groupsSize = presentableOnChartDataList.stream().collect(Collectors.groupingBy(PresentableOnChart::getGroupName)).size();
        for (int i = 0; i < groupsSize; i++) {
            renderer1.setSeriesItemLabelPaint(i, renderer1.lookupSeriesPaint(i));
        }

        return plot;
    }
}
