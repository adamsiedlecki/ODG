package pl.adamsiedlecki.odg.chart.creator.tools;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import org.openapitools.model.PresentableOnBarChart;

import java.util.List;
@Slf4j
public class BarLabelGenerator extends StandardCategoryItemLabelGenerator {

    private List<? extends PresentableOnBarChart> chartDataList;

    public BarLabelGenerator(List<? extends PresentableOnBarChart> chartDataList) {
        this.chartDataList = chartDataList;
    }

    @Override
    public String generateLabel(CategoryDataset dataset, int row, int column) {
        String result =  generateLabelString(dataset, row, column);
        log.info("result: {}, row: {} column: {}", result, row, column);
        return result; //TODO custom label
    }
}
