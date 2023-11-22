package pl.adamsiedlecki.odg.chart.creator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.openapitools.model.PresentableOnChart;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import pl.adamsiedlecki.odg.chart.creator.dto.ChartLabels;
import pl.adamsiedlecki.odg.chart.creator.tools.ChartElementsCreator;
import pl.adamsiedlecki.odg.exceptions.CannotCreateChartException;
import pl.adamsiedlecki.odg.util.UuidTool;
import pl.adamsiedlecki.odg.util.files.MyFilesystem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class XyChartCreator {

    private final Font font = new Font("Dialog", Font.PLAIN, 14);
    private final ChartElementsCreator elemCreator;
    private final MyFilesystem myFilesystem;


    public ByteArrayResource createChart(List<? extends PresentableOnChart> chartDataList,
                                         int width,
                                         int height,
                                         ChartLabels chartLabels,
                                         boolean areItemLabelsVisible,
                                         int maxMinutesToConnectLines,
                                         BigDecimal redValueMarkerLineLevel,
                                         boolean isPercentChart) {
        if (chartDataList.isEmpty()) {
            log.error("Cannot create chart due to no data");
            return null;
        }
        chartDataList.sort(Comparator.comparing(PresentableOnChart::getTime));

        XYPlot plot = elemCreator.createXYPlot(chartDataList, font, chartLabels.dataLabel(), chartLabels.timeLabel(), areItemLabelsVisible, maxMinutesToConnectLines);
        if (redValueMarkerLineLevel != null) {
            plot.addRangeMarker(new ValueMarker(redValueMarkerLineLevel.doubleValue(), Color.red, new BasicStroke(7.0f), Color.GREEN, new BasicStroke(2.0f), 0.2f)); // line on chart
        }
        if (isPercentChart) {
            plot.getRangeAxis().setRange(new Range(0, 100));
        }

        JFreeChart chart = new JFreeChart(chartLabels.chartLabel(),
                                          JFreeChart.DEFAULT_TITLE_FONT,
                                          plot,
                                          true);
        chart.getLegend().setItemFont(font);

        BufferedImage image = chart.createBufferedImage(width, height, BufferedImage.TYPE_INT_RGB, null);
        try {
            byte[] pngBytes = ChartUtils.encodeAsPNG(image);
            return new ByteArrayResource(pngBytes);
        } catch (IOException e) {
            log.error("Error while creating an xy chart: {}", e.getMessage());
            throw new CannotCreateChartException();
        }
    }
}
