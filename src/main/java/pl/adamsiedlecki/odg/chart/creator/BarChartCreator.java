package pl.adamsiedlecki.odg.chart.creator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openapitools.model.PresentableOnBarChart;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import pl.adamsiedlecki.odg.exceptions.CannotCreateChartException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BarChartCreator {

    private final Font font = new Font("Dialog", Font.PLAIN, 14);


    public ByteArrayResource createChart(List<? extends PresentableOnBarChart> chartDataList, int width, int height, String chartLabel, String categoriesLabel, String valuesLabel) {
        if (chartDataList.isEmpty()) {
            log.error("Cannot create bar chart due to no data");
            return null;
        }
        JFreeChart barChart = ChartFactory.createBarChart(
                chartLabel,
                categoriesLabel,
                valuesLabel,
                createDataset(chartDataList),
                PlotOrientation.VERTICAL,
                true, true, false);
        barChart.getLegend().setItemFont(font);

        BufferedImage image = barChart.createBufferedImage(width, height, BufferedImage.TYPE_INT_RGB, null);
        try {
            byte[] pngBytes = ChartUtils.encodeAsPNG(image);
            return new ByteArrayResource(pngBytes);
        } catch (IOException e) {
            log.error("Error while creating a bar chart: {}", e.getMessage());
            throw new CannotCreateChartException();
        }
    }

    private CategoryDataset createDataset(List<? extends PresentableOnBarChart> chartDataList) {
        var dataset = new DefaultCategoryDataset();
        chartDataList.forEach(presentable -> dataset.addValue(presentable.getValue(), presentable.getCategoryName(), presentable.getSubCategoryName()));
        return dataset;
    }
}
