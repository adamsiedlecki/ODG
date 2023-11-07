package pl.adamsiedlecki.odg.chart.creator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.openapitools.model.PresentableOnBarChart;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import pl.adamsiedlecki.odg.chart.creator.tools.BarLabelGenerator;
import pl.adamsiedlecki.odg.exceptions.CannotCreateChartException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BarChartCreator {

    private final Font font = new Font("Dialog", Font.PLAIN, 14);


    public ByteArrayResource createChart(List<PresentableOnBarChart> chartDataList,
                                         int width,
                                         int height,
                                         String chartLabel,
                                         String categoriesLabel,
                                         String valuesLabel,
                                         String maxValueMarkerText) {
        if (chartDataList.isEmpty()) {
            log.error("Cannot create bar chart due to no data");
            return null;
        }
        var dataset = createDataset(chartDataList);
        JFreeChart barChart = ChartFactory.createBarChart(
                chartLabel,
                categoriesLabel,
                valuesLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        barChart.getLegend().setItemFont(font);

        BarRenderer renderer = new BarRenderer();
        renderer.setDefaultItemLabelGenerator(new BarLabelGenerator(chartDataList));
        renderer.setDefaultItemLabelsVisible(true);
        barChart.getCategoryPlot().setRenderer(renderer);

        if (maxValueMarkerText != null) {
            Number maximum = DatasetUtils.findMaximumRangeValue(dataset);
            ValueMarker max = new ValueMarker(maximum.floatValue());
            max.setPaint(Color.RED);
            max.setLabelFont(font);
            max.setStroke(new BasicStroke(2.0f));
            max.setAlpha(0.6f);
            max.setLabel(maxValueMarkerText);
            max.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
            barChart.getCategoryPlot().addRangeMarker(max, Layer.FOREGROUND);
        }

        BufferedImage image = barChart.createBufferedImage(width, height, BufferedImage.TYPE_INT_RGB, null);
        try {
            byte[] pngBytes = ChartUtils.encodeAsPNG(image);
            return new ByteArrayResource(pngBytes);
        } catch (IOException e) {
            log.error("Error while creating a bar chart: {}", e.getMessage());
            throw new CannotCreateChartException();
        }
    }

    private CategoryDataset createDataset(List<PresentableOnBarChart> chartDataList) {
        var dataset = new DefaultCategoryDataset();
        chartDataList.sort(Comparator.comparing(PresentableOnBarChart::getCategoryName));
        chartDataList.forEach(presentable -> dataset.addValue(presentable.getValue(),
                presentable.getCategoryName(),
                presentable.getSubCategoryName()));
        return dataset;
    }
}
