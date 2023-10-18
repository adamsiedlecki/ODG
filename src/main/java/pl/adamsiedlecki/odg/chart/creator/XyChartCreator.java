package pl.adamsiedlecki.odg.chart.creator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.openapitools.model.PresentableOnChart;
import org.springframework.stereotype.Service;
import pl.adamsiedlecki.odg.chart.creator.dto.ChartLabels;
import pl.adamsiedlecki.odg.chart.creator.tools.ChartElementsCreator;
import pl.adamsiedlecki.odg.exceptions.CannotCreateChartException;
import pl.adamsiedlecki.odg.util.UuidTool;
import pl.adamsiedlecki.odg.util.files.MyFilesystem;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class XyChartCreator {

    private final Font font = new Font("Dialog", Font.PLAIN, 14);
    private final ChartElementsCreator elemCreator;
    private final MyFilesystem myFilesystem;


    public File createChart(List<? extends PresentableOnChart> chartDataList, int width, int height, ChartLabels chartLabels, boolean areItemLabelsVisible, int maxMinutesToConnectLines) {
        if (chartDataList.isEmpty()) {
            log.error("Cannot create chart due to no data");
            return new File("");
        }
        chartDataList.sort(Comparator.comparing(PresentableOnChart::getTime));

        XYPlot plot = elemCreator.createXYPlot(chartDataList, font, chartLabels.dataLabel(), chartLabels.timeLabel(), areItemLabelsVisible, maxMinutesToConnectLines);

        JFreeChart chart = new JFreeChart(chartLabels.chartLabel(),
                                          JFreeChart.DEFAULT_TITLE_FONT,
                                          plot,
                                          true);
        chart.getLegend().setItemFont(font);

        String path = myFilesystem.getChartsPath() + UuidTool.getRandom() + ".jpg";
        File destination = new File(path);
        try {
            ChartUtils.saveChartAsJPEG(destination, chart, width, height);
            log.info("Chart created: {}", destination.getName());
        } catch (IOException e) {
            log.error("Error while creating a chart: {}", e.getMessage());
            throw new CannotCreateChartException();
        }
        return destination;
    }
}
