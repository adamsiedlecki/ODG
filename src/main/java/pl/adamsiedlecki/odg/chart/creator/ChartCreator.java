package pl.adamsiedlecki.odg.chart.creator;

import org.openapitools.model.PresentableOnChart;
import pl.adamsiedlecki.odg.chart.creator.dto.ChartLabels;

import java.io.File;
import java.util.List;


public interface ChartCreator {

     File createChart(List<? extends PresentableOnChart> presentableOnChartList, int width, int height, ChartLabels chartLabels, boolean areItemLabelsVisible, int maxHoursToConnectLines);

}
